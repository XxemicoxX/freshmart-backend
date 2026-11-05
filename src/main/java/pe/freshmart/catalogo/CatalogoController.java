package pe.freshmart.catalogo;

import static pe.freshmart.catalogo.CatalogoDtos.*;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/catalogo")
public class CatalogoController {
    private final CatalogoService catalogo;

    public CatalogoController(CatalogoService catalogo) { this.catalogo = catalogo; }

    @GetMapping("/categorias")
    public List<CategoriaDto> categorias() { return catalogo.categorias(); }

    @GetMapping("/productos")
    public PaginaDto<ProductoDto> productos(@RequestParam(required = false) Integer categoriaId,
            @RequestParam(required = false) String q, @RequestParam(required = false) Integer tiendaId,
            @RequestParam(defaultValue = "0") int pagina, @RequestParam(defaultValue = "20") int tamanio) {
        return catalogo.productos(categoriaId, q, tiendaId, pagina, tamanio);
    }

    @GetMapping("/productos/{id}")
    public ProductoDto detalle(@PathVariable Integer id, @RequestParam(required = false) Integer tiendaId) {
        return catalogo.detalle(id, tiendaId);
    }
}
