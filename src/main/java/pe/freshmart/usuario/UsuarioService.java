package pe.freshmart.usuario;

import static org.springframework.http.HttpStatus.*;
import static pe.freshmart.usuario.UsuarioDtos.*;

import io.jsonwebtoken.Claims;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.freshmart.auth.JwtService;
import pe.freshmart.auth.SesionService;
import pe.freshmart.common.ApiException;

/** RF-14 perfil, RF-16 configuración, RF-19 eliminación de cuenta (anonimización). */
@Service @Transactional
public class UsuarioService {
    private final Repos.Usuarios usuarios; private final PasswordEncoder enc; private final JwtService jwt;
    private final SesionService sesiones; private final JdbcTemplate jdbc; private final ConfiguracionRepository configs;

    public UsuarioService(Repos.Usuarios u, PasswordEncoder e, JwtService j, SesionService s, JdbcTemplate jdbc, ConfiguracionRepository c) {
        usuarios = u; enc = e; jwt = j; sesiones = s; this.jdbc = jdbc; configs = c;
    }

    @Transactional(readOnly = true)
    public PerfilDto perfil(int uid) { return dto(cargar(uid)); }

    public PerfilDto actualizar(int uid, PerfilRequest r) {
        var u = cargar(uid);
        String correo = r.correo().trim().toLowerCase();
        if (!correo.equalsIgnoreCase(u.getCorreo())) { // cambiar el correo exige reconfirmar la contraseña
            if (r.passwordActual() == null || !enc.matches(r.passwordActual(), u.getPasswordHash()))
                throw new ApiException(BAD_REQUEST, "Para cambiar tu correo, ingresa tu contraseña actual correcta.");
            if (usuarios.existsByCorreoIgnoreCase(correo)) throw new ApiException(CONFLICT, "Ya existe una cuenta con ese correo. Usa otro correo.");
            u.setCorreo(correo);
        }
        u.setNombres(r.nombres().trim()); u.setApellidos(r.apellidos().trim()); u.setFechaNacimiento(r.fechaNacimiento());
        u.setTelefono(r.telefono() == null || r.telefono().isBlank() ? null : r.telefono());
        return dto(u);
    }

    public PasswordResponse cambiarPassword(int uid, PasswordRequest r) {
        var u = cargar(uid);
        if (!enc.matches(r.passwordActual(), u.getPasswordHash())) throw new ApiException(BAD_REQUEST, "La contraseña actual es incorrecta.");
        if (!r.passwordNueva().equals(r.confirmar())) throw new ApiException(BAD_REQUEST, "Las contraseñas nuevas no coinciden. Vuelve a escribirlas.");
        if (r.passwordNueva().equals(r.passwordActual())) throw new ApiException(BAD_REQUEST, "La nueva contraseña debe ser distinta a la actual.");
        u.setPasswordHash(enc.encode(r.passwordNueva()));
        sesiones.invalidarTodas(uid); // cierra las demás sesiones; se devuelve un token nuevo para esta
        return new PasswordResponse("Tu contraseña fue actualizada. Cerramos tus otras sesiones por seguridad.", jwt.generar(u));
    }

    @Transactional(readOnly = true)
    public ConfiguracionDto configuracion(int uid) {
        var c = configs.findById(uid).orElseGet(() -> { var n = new ConfiguracionUsuario(); n.setIdUsuario(uid); return n; });
        return new ConfiguracionDto(c.isNotifPedidos(), c.isNotifPromociones(), c.isNotifCorreo(), c.getTema());
    }

    public ConfiguracionDto guardarConfiguracion(int uid, ConfiguracionDto d) {
        var c = configs.findById(uid).orElseGet(() -> { var n = new ConfiguracionUsuario(); n.setIdUsuario(uid); return n; });
        c.setNotifPedidos(d.notifPedidos()); c.setNotifPromociones(d.notifPromociones()); c.setNotifCorreo(d.notifCorreo()); c.setTema(d.tema());
        configs.save(c);
        return d;
    }

    /** Reglas: contraseña + confirmación explícita; sin pedidos en curso; pedidos/pagos se conservan anonimizados (obligación contable). */
    public void eliminar(int uid, EliminarCuentaRequest r, Claims tokenActual) {
        var u = cargar(uid);
        if (!"CLIENTE".equals(u.getRol().getNombre())) throw new ApiException(FORBIDDEN, "Las cuentas del personal las administra Freshmart.");
        if (!enc.matches(r.password(), u.getPasswordHash())) throw new ApiException(BAD_REQUEST, "La contraseña es incorrecta. No se eliminó tu cuenta.");
        Integer enCurso = jdbc.queryForObject("SELECT COUNT(*) FROM pedido WHERE id_usuario = ? AND estado IN ('CONFIRMADO','EN_PREPARACION','DISPONIBLE_RECOJO')", Integer.class, uid);
        if (enCurso != null && enCurso > 0)
            throw new ApiException(CONFLICT, "Tienes " + enCurso + " pedido(s) en curso. Recógelos o espera a que la tienda los cierre antes de eliminar tu cuenta.");

        jdbc.update("INSERT INTO historial_estado_pedido (id_pedido, estado, id_usuario_actor) SELECT id_pedido, 'CANCELADO', ? FROM pedido WHERE id_usuario = ? AND estado = 'PENDIENTE_PAGO'", uid, uid);
        jdbc.update("UPDATE pedido SET estado = 'CANCELADO' WHERE id_usuario = ? AND estado = 'PENDIENTE_PAGO'", uid);
        jdbc.update("DELETE FROM carrito WHERE id_usuario = ?", uid);
        jdbc.update("DELETE FROM notificacion WHERE id_usuario = ?", uid);
        jdbc.update("DELETE FROM configuracion_usuario WHERE id_usuario = ?", uid);
        jdbc.update("DELETE FROM token_recuperacion WHERE id_usuario = ?", uid);

        u.setNombres("Cliente"); u.setApellidos("anonimizado"); u.setCorreo("eliminado-" + uid + "@freshmart.invalid");
        u.setPasswordHash(enc.encode(UUID.randomUUID().toString())); u.setTelefono(null); u.setFechaNacimiento(null); u.setEstado("ELIMINADO");
        sesiones.revocar(tokenActual);
        sesiones.invalidarTodas(uid);
    }

    private Usuario cargar(int uid) { return usuarios.findById(uid).orElseThrow(() -> new ApiException(NOT_FOUND, "No encontramos tu cuenta.")); }

    private PerfilDto dto(Usuario u) {
        return new PerfilDto(u.getId(), u.getNombres(), u.getApellidos(), u.getCorreo(), u.getFechaNacimiento(), u.getTelefono(), u.getRol().getNombre(), u.getEstado());
    }
}
