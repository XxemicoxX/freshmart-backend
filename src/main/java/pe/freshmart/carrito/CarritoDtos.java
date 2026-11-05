package pe.freshmart.carrito;

import java.math.BigDecimal;
import java.util.List;
import pe.freshmart.tienda.TiendaController.TiendaDto;

public final class CarritoDtos {
    private CarritoDtos() {}
    public record ItemDto(Integer productoId, String nombre, String imagenUrl, BigDecimal precio,
                          int cantidad, BigDecimal subtotal, int stock) {}
    public record CarritoDto(List<ItemDto> items, int cantidadTotal, TiendaDto tienda, BigDecimal subtotal,
                             BigDecimal impuestos, BigDecimal total, BigDecimal igv, String moneda) {}
    public record CantidadRequest(int cantidad) {}
}
