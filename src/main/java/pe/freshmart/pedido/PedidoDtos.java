package pe.freshmart.pedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import pe.freshmart.tienda.TiendaController.TiendaDto;

public final class PedidoDtos {
    private PedidoDtos() {}

    public record PagoRequest(MetodoPago metodo, String numeroTarjeta, String vencimiento, String cvv,
                              String celular, String codigoYape) {}
    public record ItemPedidoDto(Integer productoId, String nombreProducto, int cantidad,
                                BigDecimal precioUnitario, BigDecimal subtotal) {}
    public record PagoDto(String metodo, String estado, String codigoTransaccion, String referencia,
                          boolean simulado, LocalDateTime fechaPago) {}
    public record SeguimientoDto(String estado, String etiqueta, LocalDateTime fecha) {}
    public record PedidoDto(Integer id, String codigo, LocalDateTime fechaPedido, String estado, String etiqueta,
                            TiendaDto tienda, List<ItemPedidoDto> items, BigDecimal subtotal, BigDecimal impuestos,
                            BigDecimal total, String moneda, PagoDto pago, List<SeguimientoDto> seguimiento,
                            String indicaciones, String cliente, boolean pagado) {}
    public record ResultadoPagoDto(boolean aprobado, String mensaje, PedidoDto pedido) {}
    public record PedidoResumenDto(Integer id, String codigo, LocalDateTime fechaPedido, String estado,
                                   String etiqueta, String tienda, String cliente, int cantidad,
                                   List<String> productos, BigDecimal total, boolean pagado) {}
    public record ResumenHistorialDto(long cantidadPedidos, BigDecimal totalComprado, long pedidosEntregados) {}
    public record BoletaArchivo(String nombre, byte[] contenido) {}
    public record EstadoRequest(EstadoPedido estado) {}
}
