package pe.freshmart.catalogo;

import java.math.BigDecimal;
import java.util.List;

public final class CatalogoDtos {
    private CatalogoDtos() {}
    public record CategoriaDto(Integer id, String nombre, String descripcion, long productos) {}
    public record ProductoDto(Integer id, String nombre, String descripcion, BigDecimal precio, Integer categoriaId,
                              String categoria, BigDecimal calificacion, String imagenUrl, boolean disponible, int stock) {}
    public record PaginaDto<T>(List<T> contenido, int pagina, int tamanio, long totalElementos, int totalPaginas) {}
}
