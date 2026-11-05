package pe.freshmart.pedido;

import static org.springframework.http.HttpStatus.*;
import static pe.freshmart.pedido.PedidoDtos.*;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.freshmart.carrito.CarritoRepository;
import pe.freshmart.catalogo.CatalogoDtos.PaginaDto;
import pe.freshmart.catalogo.StockService;
import pe.freshmart.common.ApiException;
import pe.freshmart.common.PrecioService;
import pe.freshmart.notificacion.NotificacionService;
import pe.freshmart.tienda.TiendaController.TiendaDto;
import pe.freshmart.usuario.Repos;
import pe.freshmart.usuario.Usuario;

/** RF-04, RF-10, RF-11, RF-12 y gestión de estados. Reglas de negocio centralizadas en el backend. */
@Service @Transactional
public class PedidoService {
    private static final Set<EstadoPedido> CON_PAGO = EnumSet.of(EstadoPedido.PAGO_APROBADO, EstadoPedido.CONFIRMADO,
        EstadoPedido.EN_PREPARACION, EstadoPedido.DISPONIBLE_RECOJO, EstadoPedido.ENTREGADO);
    private static final Set<EstadoPedido> COLA_TIENDA = EnumSet.of(EstadoPedido.CONFIRMADO, EstadoPedido.EN_PREPARACION, EstadoPedido.DISPONIBLE_RECOJO);
    private static final Set<EstadoPedido> PERMITIDOS_PERSONAL = EnumSet.of(EstadoPedido.EN_PREPARACION,
        EstadoPedido.DISPONIBLE_RECOJO, EstadoPedido.ENTREGADO, EstadoPedido.CANCELADO);
    private static final SecureRandom RND = new SecureRandom();

    private final Repositorios.Pedidos pedidos; private final Repositorios.Pagos pagos; private final Repositorios.Comprobantes comprobantes;
    private final CarritoRepository carritos; private final Repos.Usuarios usuarios; private final StockService stock;
    private final PrecioService precios; private final PagoSimulador simulador; private final BoletaPdfService pdf;
    private final NotificacionService notificaciones; private final JdbcTemplate jdbc;

    public PedidoService(Repositorios.Pedidos pe, Repositorios.Pagos pa, Repositorios.Comprobantes co, CarritoRepository ca, Repos.Usuarios us,
                         StockService st, PrecioService pr, PagoSimulador si, BoletaPdfService pd, NotificacionService no, JdbcTemplate jdbc) {
        pedidos = pe; pagos = pa; comprobantes = co; carritos = ca; usuarios = us; stock = st; precios = pr; simulador = si; pdf = pd; notificaciones = no; this.jdbc = jdbc;
    }

    // ---------- Cliente ----------

    /** Paso previo al pago: congela el carrito como pedido PENDIENTE_PAGO con importes calculados aquí. */
    public PedidoDto crear(int uid) {
        var c = carritos.findByIdUsuario(uid).filter(x -> !x.getItems().isEmpty())
            .orElseThrow(() -> new ApiException(CONFLICT, "Tu carrito está vacío. Agrega productos para continuar."));
        if (c.getTienda() == null) throw new ApiException(CONFLICT, "Elige la tienda donde recogerás tu pedido.");
        int tid = c.getTienda().getId();
        var stocks = stock.stocks(c.getItems().stream().map(i -> i.getProducto().getId()).toList(), tid);
        var faltantes = c.getItems().stream().filter(i -> stocks.getOrDefault(i.getProducto().getId(), 0) < i.getCantidad())
            .map(i -> i.getProducto().getNombre()).toList();
        if (!faltantes.isEmpty())
            throw new ApiException(CONFLICT, "En " + c.getTienda().getNombre() + " no hay stock suficiente de: " + String.join(", ", faltantes) + ". Ajusta tu carrito o elige otra tienda.");

        pedidos.findByIdUsuarioAndEstado(uid, EstadoPedido.PENDIENTE_PAGO).forEach(prev -> cambiar(prev, EstadoPedido.CANCELADO, uid));

        var p = new Pedido();
        p.setCodigo("TMP-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        p.setIdUsuario(uid); p.setTienda(c.getTienda()); p.setFechaPedido(LocalDateTime.now()); p.setEstado(EstadoPedido.PENDIENTE_PAGO);
        BigDecimal sub = BigDecimal.ZERO;
        for (var i : c.getItems()) {
            var d = new DetallePedido();
            d.setPedido(p); d.setProductoId(i.getProducto().getId()); d.setNombreProducto(i.getProducto().getNombre());
            d.setCantidad(i.getCantidad()); d.setPrecioUnitario(i.getProducto().getPrecio());
            d.setSubtotal(i.getProducto().getPrecio().multiply(BigDecimal.valueOf(i.getCantidad())));
            p.getDetalles().add(d); sub = sub.add(d.getSubtotal());
        }
        var t = precios.calcular(sub);
        p.setSubtotal(t.subtotal()); p.setImpuestos(t.impuestos()); p.setTotal(t.total());
        pedidos.saveAndFlush(p);
        p.setCodigo("ORD-" + (1000 + p.getId()));
        registrarEstado(p, EstadoPedido.PENDIENTE_PAGO, uid);
        return dto(p, null);
    }

    /** RF-10: el pedido solo se confirma si el pago quedó APROBADO. Un rechazo deja el pedido pendiente, nunca pagado. */
    public ResultadoPagoDto pagar(int uid, int pedidoId, PagoRequest req) {
        var p = propio(uid, pedidoId);
        if (p.getEstado() != EstadoPedido.PENDIENTE_PAGO) throw new ApiException(CONFLICT, "Este pedido ya no está pendiente de pago.");
        var r = simulador.procesar(req);

        if (r.aprobado()) { // descuento atómico de stock; si falta, se revierte toda la transacción
            for (var d : p.getDetalles()) {
                int n = jdbc.update("UPDATE inventario SET stock = stock - ? WHERE id_producto = ? AND id_tienda = ? AND stock >= ?",
                    d.getCantidad(), d.getProductoId(), p.getTienda().getId(), d.getCantidad());
                if (n == 0) throw new ApiException(CONFLICT, "«" + d.getNombreProducto() + "» ya no tiene stock suficiente en " + p.getTienda().getNombre() + ". No se realizó ningún cobro; actualiza tu carrito.");
            }
        }
        var pago = new Pago();
        pago.setIdPedido(p.getId()); pago.setFechaPago(LocalDateTime.now()); pago.setMetodo(req.metodo()); pago.setMonto(p.getTotal());
        pago.setEstado(r.aprobado() ? EstadoPago.APROBADO : EstadoPago.RECHAZADO); pago.setReferencia(r.referencia());
        pago.setCodigoTransaccion("TXN-" + Year.now() + "-" + String.format("%06d", RND.nextInt(1_000_000)));
        pagos.saveAndFlush(pago);

        if (!r.aprobado()) return new ResultadoPagoDto(false, r.mensaje(), dto(p, null));

        var comp = new Comprobante();
        comp.setIdPago(pago.getId()); comp.setSerie("B001"); comp.setNumero(String.format("%08d", pago.getId()));
        comp.setFechaEmision(LocalDateTime.now()); comp.setMontoTotal(p.getTotal());
        comprobantes.save(comp);
        cambiar(p, EstadoPedido.PAGO_APROBADO, uid);
        cambiar(p, EstadoPedido.CONFIRMADO, uid);
        carritos.findByIdUsuario(uid).ifPresent(c -> c.getItems().clear());
        notificaciones.pedido(uid, "Pedido confirmado " + p.getCodigo(),
            "Tu pago fue aprobado y tu pedido " + p.getCodigo() + " fue confirmado. Total: S/ " + p.getTotal() + ". Te avisaremos cuando esté listo para recoger en " + p.getTienda().getNombre() + ".", true);
        return new ResultadoPagoDto(true, "¡Pago aprobado! Tu pedido " + p.getCodigo() + " fue confirmado.", dto(p, null));
    }

    @Transactional(readOnly = true)
    public PaginaDto<PedidoResumenDto> historial(int uid, int pagina, int tamanio) { // RF-04
        var pg = pedidos.historial(uid, CON_PAGO, PageRequest.of(Math.max(pagina, 0), Math.min(Math.max(tamanio, 1), 50)));
        return new PaginaDto<>(pg.getContent().stream().map(p -> resumen(p, null)).toList(), pg.getNumber(), pg.getSize(), pg.getTotalElements(), pg.getTotalPages());
    }

    @Transactional(readOnly = true)
    public ResumenHistorialDto resumenHistorial(int uid) {
        return jdbc.queryForObject("""
            SELECT COUNT(*), COALESCE(SUM(total), 0), COALESCE(SUM(CASE WHEN estado = 'ENTREGADO' THEN 1 ELSE 0 END), 0)
            FROM pedido WHERE id_usuario = ? AND estado NOT IN ('PENDIENTE_PAGO', 'CANCELADO')""",
            (rs, n) -> new ResumenHistorialDto(rs.getLong(1), rs.getBigDecimal(2), rs.getLong(3)), uid);
    }

    @Transactional(readOnly = true)
    public PedidoDto detalle(int uid, int id) { return dto(propio(uid, id), null); }

    public PedidoDto cancelar(int uid, int id) {
        var p = propio(uid, id);
        if (p.getEstado() != EstadoPedido.PENDIENTE_PAGO)
            throw new ApiException(CONFLICT, "Solo puedes cancelar pedidos pendientes de pago. Si ya pagaste, comunícate con la tienda.");
        cambiar(p, EstadoPedido.CANCELADO, uid);
        return dto(p, null);
    }

    /** RF-12: solo existe boleta si hay un pago aprobado registrado. */
    @Transactional(readOnly = true)
    public BoletaArchivo boleta(int uid, int id) {
        var p = propio(uid, id);
        var pago = pagos.findFirstByIdPedidoAndEstadoOrderByIdDesc(p.getId(), EstadoPago.APROBADO)
            .orElseThrow(() -> new ApiException(CONFLICT, "La boleta se genera cuando el pago es aprobado."));
        var comp = comprobantes.findByIdPago(pago.getId()).orElseThrow(() -> new ApiException(CONFLICT, "La boleta de este pedido aún no está disponible."));
        var u = usuarios.findById(uid).orElseThrow();
        return new BoletaArchivo("boleta-" + p.getCodigo(), pdf.generar(p, pago, comp, u.getNombres() + " " + u.getApellidos(), u.getCorreo(), precios.getIgv()));
    }

    // ---------- Personal de tienda ----------

    @Transactional(readOnly = true)
    public PaginaDto<PedidoResumenDto> personalListar(int staffId, EstadoPedido estado, int pagina, int tamanio) {
        var estados = estado == null ? COLA_TIENDA : EnumSet.of(estado);
        var pg = pedidos.porTienda(tiendaPersonal(staffId), estados, PageRequest.of(Math.max(pagina, 0), Math.min(Math.max(tamanio, 1), 50)));
        var nombres = usuarios.findAllById(pg.getContent().stream().map(Pedido::getIdUsuario).filter(Objects::nonNull).collect(Collectors.toSet()))
            .stream().collect(Collectors.toMap(Usuario::getId, u -> u.getNombres() + " " + u.getApellidos()));
        return new PaginaDto<>(pg.getContent().stream().map(p -> resumen(p, nombres.getOrDefault(p.getIdUsuario(), "Cliente anonimizado"))).toList(),
            pg.getNumber(), pg.getSize(), pg.getTotalElements(), pg.getTotalPages());
    }

    @Transactional(readOnly = true)
    public PedidoDto personalDetalle(int staffId, int id) { var p = deTienda(staffId, id); return dto(p, nombreCliente(p)); }

    public PedidoDto personalCambiarEstado(int staffId, int id, EstadoPedido nuevo) {
        var p = deTienda(staffId, id);
        if (!PERMITIDOS_PERSONAL.contains(nuevo) || !p.getEstado().puedePasarA(nuevo))
            throw new ApiException(CONFLICT, "No se puede pasar el pedido de «" + p.getEstado().getEtiqueta() + "» a «" + nuevo.getEtiqueta() + "».");
        if (nuevo == EstadoPedido.CANCELADO) { // devuelve stock y marca el pago como reembolsado (simulado)
            for (var d : p.getDetalles())
                jdbc.update("UPDATE inventario SET stock = stock + ? WHERE id_producto = ? AND id_tienda = ?", d.getCantidad(), d.getProductoId(), p.getTienda().getId());
            pagos.findFirstByIdPedidoAndEstadoOrderByIdDesc(p.getId(), EstadoPago.APROBADO).ifPresent(g -> g.setEstado(EstadoPago.REEMBOLSADO));
        }
        cambiar(p, nuevo, staffId);
        notificaciones.pedido(p.getIdUsuario(), "Tu pedido " + p.getCodigo() + ": " + nuevo.getEtiqueta(), mensajeEstado(p, nuevo), false);
        return dto(p, nombreCliente(p));
    }

    // ---------- Internos ----------

    private Pedido propio(int uid, int id) {
        return pedidos.findById(id).filter(p -> Integer.valueOf(uid).equals(p.getIdUsuario()))
            .orElseThrow(() -> new ApiException(NOT_FOUND, "No encontramos ese pedido en tu cuenta."));
    }

    private int tiendaPersonal(int staffId) {
        Integer t = usuarios.findById(staffId).map(Usuario::getIdTienda).orElse(null);
        if (t == null) throw new ApiException(FORBIDDEN, "Tu usuario no tiene una tienda asignada.");
        return t;
    }

    private Pedido deTienda(int staffId, int id) {
        int t = tiendaPersonal(staffId);
        return pedidos.findById(id).filter(p -> p.getTienda().getId() == t)
            .orElseThrow(() -> new ApiException(NOT_FOUND, "El pedido no existe en tu tienda."));
    }

    private String nombreCliente(Pedido p) {
        return p.getIdUsuario() == null ? "Cliente anonimizado"
            : usuarios.findById(p.getIdUsuario()).map(u -> u.getNombres() + " " + u.getApellidos()).orElse("Cliente anonimizado");
    }

    private void cambiar(Pedido p, EstadoPedido nuevo, Integer actor) { p.setEstado(nuevo); registrarEstado(p, nuevo, actor); }

    private void registrarEstado(Pedido p, EstadoPedido e, Integer actor) {
        jdbc.update("INSERT INTO historial_estado_pedido (id_pedido, estado, id_usuario_actor) VALUES (?,?,?)", p.getId(), e.name(), actor);
    }

    private String mensajeEstado(Pedido p, EstadoPedido e) {
        return switch (e) {
            case EN_PREPARACION -> "Estamos preparando tu pedido " + p.getCodigo() + ".";
            case DISPONIBLE_RECOJO -> "¡Tu pedido " + p.getCodigo() + " está listo! Recógelo en " + p.getTienda().getNombre() + " con tu documento de identidad.";
            case ENTREGADO -> "Entregamos tu pedido " + p.getCodigo() + ". ¡Gracias por comprar en Freshmart!";
            case CANCELADO -> "La tienda canceló tu pedido " + p.getCodigo() + ". Tu pago será reembolsado.";
            default -> "El estado de tu pedido " + p.getCodigo() + " cambió a " + e.getEtiqueta() + ".";
        };
    }

    private PedidoResumenDto resumen(Pedido p, String cliente) {
        return new PedidoResumenDto(p.getId(), p.getCodigo(), p.getFechaPedido(), p.getEstado().name(), p.getEstado().getEtiqueta(),
            p.getTienda().getNombre(), cliente, p.getDetalles().stream().mapToInt(DetallePedido::getCantidad).sum(),
            p.getDetalles().stream().map(DetallePedido::getNombreProducto).toList(), p.getTotal(), p.getEstado().tienePago());
    }

    private PedidoDto dto(Pedido p, String cliente) {
        var items = p.getDetalles().stream().map(d -> new ItemPedidoDto(d.getProductoId(), d.getNombreProducto(), d.getCantidad(), d.getPrecioUnitario(), d.getSubtotal())).toList();
        var pago = pagos.findFirstByIdPedidoOrderByIdDesc(p.getId()).map(g -> new PagoDto(g.getMetodo().name(), g.getEstado().name(),
            g.getCodigoTransaccion(), g.getReferencia(), g.isSimulado(), g.getFechaPago())).orElse(null);
        var seg = jdbc.query("SELECT estado, fecha FROM historial_estado_pedido WHERE id_pedido = ? ORDER BY id", (rs, n) -> {
            var e = EstadoPedido.valueOf(rs.getString(1));
            return new SeguimientoDto(e.name(), e.getEtiqueta(), rs.getTimestamp(2).toLocalDateTime());
        }, p.getId());
        var t = p.getTienda();
        return new PedidoDto(p.getId(), p.getCodigo(), p.getFechaPedido(), p.getEstado().name(), p.getEstado().getEtiqueta(), TiendaDto.de(t), items,
            p.getSubtotal(), p.getImpuestos(), p.getTotal(), "PEN", pago, seg, indicaciones(p), cliente, p.getEstado().tienePago());
    }

    private String indicaciones(Pedido p) {
        var t = p.getTienda();
        return switch (p.getEstado()) {
            case PENDIENTE_PAGO -> "Completa el pago para confirmar tu pedido.";
            case PAGO_APROBADO, CONFIRMADO -> "Pedido confirmado. Te avisaremos cuando esté disponible para recojo en " + t.getNombre() + " (" + t.getDireccion() + "). Horario: " + t.getHorarioAtencion() + ".";
            case EN_PREPARACION -> "Estamos preparando tu pedido. Te avisaremos cuando esté listo para recoger.";
            case DISPONIBLE_RECOJO -> "¡Tu pedido está listo! Acércate a " + t.getNombre() + " (" + t.getDireccion() + ") y presenta el código " + p.getCodigo() + " con tu documento de identidad.";
            case ENTREGADO -> "Pedido entregado. ¡Gracias por comprar en Freshmart!";
            case CANCELADO -> "Este pedido fue cancelado.";
        };
    }
}
