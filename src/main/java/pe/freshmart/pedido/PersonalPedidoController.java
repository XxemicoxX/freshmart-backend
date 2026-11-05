package pe.freshmart.pedido;

import static pe.freshmart.pedido.PedidoDtos.*;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.freshmart.catalogo.CatalogoDtos.PaginaDto;

@RestController
@RequestMapping("/api/v1/personal/pedidos")
public class PersonalPedidoController {
    private final PedidoService pedidos;

    public PersonalPedidoController(PedidoService pedidos) { this.pedidos = pedidos; }

    @GetMapping
    public PaginaDto<PedidoResumenDto> listar(@AuthenticationPrincipal Integer staffId,
            @RequestParam(required = false) EstadoPedido estado,
            @RequestParam(defaultValue = "0") int pagina, @RequestParam(defaultValue = "20") int tamanio) {
        return pedidos.personalListar(staffId, estado, pagina, tamanio);
    }

    @GetMapping("/{id}")
    public PedidoDto detalle(@AuthenticationPrincipal Integer staffId, @PathVariable int id) {
        return pedidos.personalDetalle(staffId, id);
    }

    @PatchMapping("/{id}/estado")
    public PedidoDto cambiarEstado(@AuthenticationPrincipal Integer staffId, @PathVariable int id,
                                   @RequestBody EstadoRequest request) {
        return pedidos.personalCambiarEstado(staffId, id, request.estado());
    }
}
