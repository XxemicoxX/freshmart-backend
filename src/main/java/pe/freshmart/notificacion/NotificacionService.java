package pe.freshmart.notificacion;

import static pe.freshmart.notificacion.NotificacionDtos.*;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;
import pe.freshmart.auth.MailService;
import pe.freshmart.common.ApiException;

/** RF-13 / RF-15 / RF-16. */
@Service
public class NotificacionService {
    private static final String FILTRO = """
         FROM notificacion n LEFT JOIN configuracion_usuario c ON c.id_usuario = n.id_usuario
         WHERE n.id_usuario = ? AND (n.tipo <> 'PROMOCION' OR COALESCE(c.notif_promociones, TRUE))""";
    private final JdbcTemplate jdbc; private final MailService mail;
    public NotificacionService(JdbcTemplate jdbc, MailService mail) { this.jdbc = jdbc; this.mail = mail; }

    /** Notificación de pedido: en la app si el usuario la tiene activa; por correo si es obligatoria o él la activó. */
    public void pedido(Integer usuarioId, String titulo, String mensaje, boolean correoObligatorio) {
        if (usuarioId == null) return;
        boolean[] preferencias = jdbc.query("SELECT notif_pedidos, notif_correo FROM configuracion_usuario WHERE id_usuario = ?",
            (ResultSetExtractor<boolean[]>) rs -> rs.next() ? new boolean[] {rs.getBoolean(1), rs.getBoolean(2)} : new boolean[] {true, false}, usuarioId);
        boolean[] cfg = preferencias == null ? new boolean[] {true, false} : preferencias;
        if (cfg[0]) jdbc.update("INSERT INTO notificacion (id_usuario, tipo, titulo, mensaje) VALUES (?,?,?,?)", usuarioId, "PEDIDO", titulo, mensaje);
        if (correoObligatorio || cfg[1]) {
            String correo = jdbc.queryForObject("SELECT correo FROM usuario WHERE id_usuario = ?", String.class, usuarioId);
            mail.enviar(correo, "Freshmart: " + titulo, mensaje);
        }
    }

    public PaginaNotificaciones listar(int uid, boolean soloNoLeidas, int pagina, int tamanio) {
        int size = Math.min(Math.max(tamanio, 1), 50), page = Math.max(pagina, 0);
        String extra = soloNoLeidas ? " AND n.leida = FALSE" : "";
        long total = jdbc.queryForObject("SELECT COUNT(*)" + FILTRO + extra, Long.class, uid);
        long noLeidas = jdbc.queryForObject("SELECT COUNT(*)" + FILTRO + " AND n.leida = FALSE", Long.class, uid);
        var lista = jdbc.query("SELECT n.id, n.tipo, n.titulo, n.mensaje, n.leida, n.fecha, n.enlace" + FILTRO + extra
            + " ORDER BY n.fecha DESC, n.id DESC LIMIT ? OFFSET ?",
            (rs, i) -> new NotificacionDto(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getBoolean(5),
                rs.getTimestamp(6).toLocalDateTime(), rs.getString(7)), uid, size, page * size);
        return new PaginaNotificaciones(lista, noLeidas, page, size, total, (int) Math.ceil(total / (double) size));
    }

    public void marcarLeida(int uid, long id) {
        if (jdbc.update("UPDATE notificacion SET leida = TRUE WHERE id = ? AND id_usuario = ?", id, uid) == 0)
            throw new ApiException(HttpStatus.NOT_FOUND, "No encontramos esa notificación.");
    }

    public int marcarTodas(int uid) { return jdbc.update("UPDATE notificacion SET leida = TRUE WHERE id_usuario = ? AND leida = FALSE", uid); }
}
