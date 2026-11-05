package pe.freshmart.notificacion;

import java.time.LocalDateTime;
import java.util.List;

public final class NotificacionDtos {
    private NotificacionDtos() {}

    public record NotificacionDto(long id, String tipo, String titulo, String mensaje, boolean leida,
                                  LocalDateTime fecha, String enlace) {}
    public record PaginaNotificaciones(List<NotificacionDto> notificaciones, long noLeidas, int pagina,
                                       int tamanio, long totalElementos, int totalPaginas) {}
}
