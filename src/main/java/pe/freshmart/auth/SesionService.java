package pe.freshmart.auth;

import io.jsonwebtoken.Claims;
import java.sql.Timestamp;
import java.time.Instant;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/** RF-20 / RNF-02: un JWT es válido solo si no fue revocado, su cuenta sigue ACTIVA y no es anterior a "sesiones_desde". */
@Service
public class SesionService {
    private final JdbcTemplate jdbc;
    public SesionService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public boolean valida(Claims c) {
        Integer uid = Integer.valueOf(c.getSubject());
        Boolean ok = jdbc.query("""
            SELECT u.estado, u.sesiones_desde, (SELECT COUNT(*) FROM token_revocado r WHERE r.jti = ?)
            FROM usuario u WHERE u.id_usuario = ?""",
            (ResultSetExtractor<Boolean>) rs -> {
                if (!rs.next() || !"ACTIVO".equals(rs.getString(1)) || rs.getInt(3) > 0) return false;
                Timestamp desde = rs.getTimestamp(2);
                return desde == null || c.getIssuedAt() == null || c.getIssuedAt().getTime() >= desde.getTime();
            }, c.getId(), uid);
        return Boolean.TRUE.equals(ok);
    }

    public void revocar(Claims c) {
        jdbc.update("INSERT IGNORE INTO token_revocado (jti, id_usuario, vence_en) VALUES (?,?,?)",
            c.getId(), Integer.valueOf(c.getSubject()), Timestamp.from(c.getExpiration().toInstant()));
    }

    public void invalidarTodas(int usuarioId) {
        jdbc.update("UPDATE usuario SET sesiones_desde = ? WHERE id_usuario = ?", new Timestamp(System.currentTimeMillis() / 1000 * 1000), usuarioId);
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void limpiarVencidos() { jdbc.update("DELETE FROM token_revocado WHERE vence_en < ?", Timestamp.from(Instant.now())); }
}
