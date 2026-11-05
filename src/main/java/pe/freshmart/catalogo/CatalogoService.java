package pe.freshmart.catalogo;

import static pe.freshmart.catalogo.CatalogoDtos.*;

import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.freshmart.common.ApiException;

@Service @Transactional(readOnly = true)
public class CatalogoService {
    private final ProductoRepository productos; private final StockService stock; private final JdbcTemplate jdbc;
    public CatalogoService(ProductoRepository p, StockService s, JdbcTemplate j) { productos = p; stock = s; jdbc = j; }

    public List<CategoriaDto> categorias() { // RF-06 / HU-04
        return jdbc.query("""
            SELECT c.id_categoria, c.nombre, c.descripcion, COUNT(p.id_producto)
            FROM categoria c LEFT JOIN producto p ON p.id_categoria = c.id_categoria AND p.activo = TRUE
            GROUP BY c.id_categoria, c.nombre, c.descripcion ORDER BY c.id_categoria""",
            (rs, n) -> new CategoriaDto(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getLong(4)));
    }

    /** RF-06 (por categoría) y RF-07 / HU-05 (palabras clave en nombre, descripción o categoría). Paginado (RNF-01). */
    public PaginaDto<ProductoDto> productos(Integer categoriaId, String q, Integer tiendaId, int pagina, int tamanio) {
        String like = (q == null || q.isBlank()) ? "%" : "%" + q.trim().toLowerCase() + "%";
        var page = productos.buscar(categoriaId, like,
            PageRequest.of(Math.max(pagina, 0), Math.min(Math.max(tamanio, 1), 50), Sort.by("nombre")));
        var stocks = stock.stocks(page.getContent().stream().map(Producto::getId).toList(), tiendaId);
        return new PaginaDto<>(page.getContent().stream().map(p -> dto(p, stocks.getOrDefault(p.getId(), 0))).toList(),
            page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    public ProductoDto detalle(Integer id, Integer tiendaId) {
        var p = productos.findByIdAndActivoTrue(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "El producto no existe o ya no está disponible."));
        return dto(p, stock.stock(id, tiendaId));
    }

    private ProductoDto dto(Producto p, int stk) {
        return new ProductoDto(p.getId(), p.getNombre(), p.getDescripcion(), p.getPrecio(), p.getCategoria().getId(),
            p.getCategoria().getNombre(), p.getCalificacion(), p.getImagenUrl(), stk > 0, stk);
    }
}
