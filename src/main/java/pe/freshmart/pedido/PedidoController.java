package pe.freshmart.pedido;

import static pe.freshmart.pedido.PedidoDtos.*;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.freshmart.catalogo.CatalogoDtos.PaginaDto;

@RestController
@RequestMapping("/api/v1/pedidos")
public class PedidoController {
    private final PedidoService pedidos;

    public PedidoController(PedidoService pedidos) { this.pedidos = pedidos; }

    @PostMapping
    public PedidoDto crear(@AuthenticationPrincipal Integer uid) { return pedidos.crear(uid); }

    @PostMapping("/{id}/pagar")
    public ResultadoPagoDto pagar(@AuthenticationPrincipal Integer uid, @PathVariable int id,
                                  @RequestBody PagoRequest request) {
        return pedidos.pagar(uid, id, request);
    }

    @GetMapping("/historial")
    public PaginaDto<PedidoResumenDto> historial(@AuthenticationPrincipal Integer uid,
            @RequestParam(defaultValue = "0") int pagina, @RequestParam(defaultValue = "20") int tamanio) {
        return pedidos.historial(uid, pagina, tamanio);
    }

    @GetMapping("/historial/resumen")
    public ResumenHistorialDto resumen(@AuthenticationPrincipal Integer uid) { return pedidos.resumenHistorial(uid); }

    @GetMapping("/{id}")
    public PedidoDto detalle(@AuthenticationPrincipal Integer uid, @PathVariable int id) { return pedidos.detalle(uid, id); }

    @DeleteMapping("/{id}")
    public PedidoDto cancelar(@AuthenticationPrincipal Integer uid, @PathVariable int id) { return pedidos.cancelar(uid, id); }

    @GetMapping("/{id}/boleta")
    public ResponseEntity<byte[]> boleta(@AuthenticationPrincipal Integer uid, @PathVariable int id) {
        BoletaArchivo archivo = pedidos.boleta(uid, id);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment().filename(archivo.nombre() + ".pdf").build().toString())
            .body(archivo.contenido());
    }
}
