package pe.freshmart.notificacion;

import org.springframework.jdbc.core.JdbcTemplate;

public final class Promociones {
    private Promociones() {}

    public static void sembrar(JdbcTemplate jdbc, Integer usuarioId) {
        jdbc.update("INSERT INTO notificacion (id_usuario, tipo, titulo, mensaje) VALUES (?,?,?,?)",
            usuarioId, "PROMOCION", "Ofertas para tu primera compra", "Explora el catálogo y encuentra productos destacados para tu hogar.");
    }
}
