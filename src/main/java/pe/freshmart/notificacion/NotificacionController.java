package pe.freshmart.notificacion;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notificaciones")
public class NotificacionController {
    private final NotificacionService notificaciones;

    public NotificacionController(NotificacionService notificaciones) { this.notificaciones = notificaciones; }

    @GetMapping
    public NotificacionDtos.PaginaNotificaciones listar(@AuthenticationPrincipal Integer uid,
            @RequestParam(defaultValue = "false") boolean soloNoLeidas,
            @RequestParam(defaultValue = "0") int pagina, @RequestParam(defaultValue = "20") int tamanio) {
        return notificaciones.listar(uid, soloNoLeidas, pagina, tamanio);
    }

    @PatchMapping("/{id}/leida")
    public void marcarLeida(@AuthenticationPrincipal Integer uid, @PathVariable long id) {
        notificaciones.marcarLeida(uid, id);
    }

    @PutMapping("/leidas")
    public int marcarTodas(@AuthenticationPrincipal Integer uid) { return notificaciones.marcarTodas(uid); }
}
