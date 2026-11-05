package pe.freshmart.carrito;

import static org.springframework.http.HttpStatus.*;
import static pe.freshmart.carrito.CarritoDtos.*;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.freshmart.catalogo.Producto;
import pe.freshmart.catalogo.ProductoRepository;
import pe.freshmart.catalogo.StockService;
import pe.freshmart.common.ApiException;
import pe.freshmart.common.PrecioService;
import pe.freshmart.tienda.TiendaController.TiendaDto;
import pe.freshmart.tienda.TiendaRepository;

/** RF-05, RF-08, RF-09 / HU-08. El backend recalcula siempre precios e impuestos; el cliente nunca los envía. */
@Service @Transactional
public class CarritoService {
    private final CarritoRepository carritos; private final ProductoRepository productos; private final TiendaRepository tiendas;
    private final StockService stock; private final PrecioService precios;

    public CarritoService(CarritoRepository c, ProductoRepository p, TiendaRepository t, StockService s, PrecioService pr) {
        carritos = c; productos = p; tiendas = t; stock = s; precios = pr;
    }

    public CarritoDto ver(int uid) { return dto(obtener(uid)); }

    public CarritoDto agregar(int uid, int productoId, int cantidad) {
        var c = obtener(uid);
        var p = productos.findByIdAndActivoTrue(productoId)
            .orElseThrow(() -> new ApiException(NOT_FOUND, "El producto no existe o ya no está disponible."));
        var existente = c.getItems().stream().filter(i -> i.getProducto().getId().equals(productoId)).findFirst();
        int nueva = existente.map(DetalleCarrito::getCantidad).orElse(0) + cantidad;
        validarCantidad(c, p, nueva);
        if (existente.isPresent()) existente.get().setCantidad(nueva);
        else { var d = new DetalleCarrito(); d.setCarrito(c); d.setProducto(p); d.setCantidad(nueva); c.getItems().add(d); }
        return dto(c);
    }

    public CarritoDto actualizar(int uid, int productoId, int cantidad) {
        var c = obtener(uid);
        var item = c.getItems().stream().filter(i -> i.getProducto().getId().equals(productoId)).findFirst()
            .orElseThrow(() -> new ApiException(NOT_FOUND, "Ese producto no está en tu carrito."));
        validarCantidad(c, item.getProducto(), cantidad);
        item.setCantidad(cantidad);
        return dto(c);
    }

    public CarritoDto eliminar(int uid, int productoId) {
        var c = obtener(uid);
        c.getItems().removeIf(i -> i.getProducto().getId().equals(productoId));
        return dto(c);
    }

    public CarritoDto vaciar(int uid) { var c = obtener(uid); c.getItems().clear(); return dto(c); }

    /** Pantalla 9: la tienda queda guardada en el carrito durante todo el proceso de compra. */
    public CarritoDto seleccionarTienda(int uid, int tiendaId) {
        var c = obtener(uid);
        var t = tiendas.findByIdAndActivaTrue(tiendaId)
            .orElseThrow(() -> new ApiException(NOT_FOUND, "La tienda no existe o no está disponible."));
        var stocks = stock.stocks(c.getItems().stream().map(i -> i.getProducto().getId()).toList(), tiendaId);
        var faltantes = c.getItems().stream().filter(i -> stocks.getOrDefault(i.getProducto().getId(), 0) < i.getCantidad())
            .map(i -> i.getProducto().getNombre()).toList();
        if (!faltantes.isEmpty())
            throw new ApiException(CONFLICT, "En " + t.getNombre() + " no hay stock suficiente de: " + String.join(", ", faltantes)
                + ". Ajusta tu carrito o elige otra tienda.");
        c.setTienda(t);
        return dto(c);
    }

    private Carrito obtener(int uid) {
        return carritos.findByIdUsuario(uid).orElseGet(() -> { var c = new Carrito(); c.setIdUsuario(uid); return carritos.save(c); });
    }

    private void validarCantidad(Carrito c, Producto p, int cantidad) {
        if (cantidad > 99) throw new ApiException(BAD_REQUEST, "Máximo 99 unidades por producto.");
        Integer tiendaId = c.getTienda() == null ? null : c.getTienda().getId();
        int disp = stock.stock(p.getId(), tiendaId);
        String donde = tiendaId == null ? "por ahora" : "en la tienda seleccionada";
        if (disp <= 0) throw new ApiException(CONFLICT, "«" + p.getNombre() + "» no está disponible " + donde + ".");
        if (cantidad > disp) throw new ApiException(CONFLICT, "Solo quedan " + disp + " unidades de «" + p.getNombre() + "» " + donde + ".");
    }

    private CarritoDto dto(Carrito c) {
        Integer tiendaId = c.getTienda() == null ? null : c.getTienda().getId();
        var stocks = stock.stocks(c.getItems().stream().map(i -> i.getProducto().getId()).toList(), tiendaId);
        List<ItemDto> items = c.getItems().stream().map(i -> {
            var p = i.getProducto();
            return new ItemDto(p.getId(), p.getNombre(), p.getImagenUrl(), p.getPrecio(), i.getCantidad(),
                p.getPrecio().multiply(BigDecimal.valueOf(i.getCantidad())), stocks.getOrDefault(p.getId(), 0));
        }).toList();
        var t = precios.calcular(items.stream().map(ItemDto::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add));
        return new CarritoDto(items, items.stream().mapToInt(ItemDto::cantidad).sum(),
            c.getTienda() == null ? null : TiendaDto.de(c.getTienda()), t.subtotal(), t.impuestos(), t.total(), precios.getIgv(), "PEN");
    }
}
