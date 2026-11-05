package pe.freshmart.auth;

import static org.springframework.http.HttpStatus.*;
import static pe.freshmart.auth.AuthDtos.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.freshmart.common.ApiException;
import pe.freshmart.notificacion.Promociones;
import pe.freshmart.usuario.*;

@Service
public class AuthService {
    private final Repos.Usuarios usuarios; 
    private final Repos.Roles roles; 
    private final Repos.Tokens tokens;
    private final PasswordEncoder encoder; 
    private final JwtService jwt; 
    private final MailService mail;
    private final JdbcTemplate jdbc; 
    private final String front;

    public AuthService(Repos.Usuarios u, Repos.Roles r, Repos.Tokens t, PasswordEncoder e, JwtService j, MailService m,
                       JdbcTemplate jdbc, @Value("${app.frontend-url}") String front) {
        this.usuarios = u; this.roles = r; this.tokens = t; this.encoder = e; this.jwt = j; this.mail = m; this.jdbc = jdbc; this.front = front;
    }

    @Transactional
    public void registrar(RegistroRequest r) { // RF-01 / HU-01
        if (!r.password().equals(r.confirmarPassword()))
            throw new ApiException(BAD_REQUEST, "Las contraseñas no coinciden. Vuelve a escribirlas.");
        String correo = r.correo().trim().toLowerCase();
        if (usuarios.existsByCorreoIgnoreCase(correo))
            throw new ApiException(CONFLICT, "Ya existe una cuenta con ese correo. Inicia sesión o recupera tu contraseña.");
        var u = new Usuario();
        u.setNombres(r.nombres().trim()); u.setApellidos(r.apellidos().trim()); u.setCorreo(correo);
        u.setFechaNacimiento(r.fechaNacimiento()); u.setPasswordHash(encoder.encode(r.password()));
        u.setRol(roles.findByNombre("CLIENTE").orElseThrow());
        usuarios.save(u);
        jdbc.update("INSERT INTO configuracion_usuario (id_usuario) VALUES (?)", u.getId());
        jdbc.update("INSERT INTO notificacion (id_usuario, tipo, titulo, mensaje) VALUES (?,?,?,?)", u.getId(), "SISTEMA",
            "¡Bienvenido a Freshmart!", "Tu cuenta fue creada. Elige tus productos y recógelos en la tienda que prefieras.");
        Promociones.sembrar(jdbc, u.getId());
        mail.enviar(correo, "Bienvenido a Freshmart", "Hola " + u.getNombres() + ", tu cuenta fue creada correctamente.");
    }

    public LoginResponse login(LoginRequest r) { // RF-02 / HU-02
        var u = usuarios.findByCorreoIgnoreCase(r.correo().trim())
            .filter(x -> "ACTIVO".equals(x.getEstado()))
            .filter(x -> encoder.matches(r.password(), x.getPasswordHash()))
            .orElseThrow(() -> new ApiException(UNAUTHORIZED, "Correo o contraseña incorrectos. Revisa tus datos o recupera tu contraseña."));
        return new LoginResponse(jwt.generar(u), UsuarioDto.de(u));
    }

    @Transactional
    public void solicitarRecuperacion(String correo) { // RF-03 / HU-03. Respuesta igual exista o no el correo (evita enumerar cuentas)
        usuarios.findByCorreoIgnoreCase(correo.trim()).filter(x -> "ACTIVO".equals(x.getEstado())).ifPresent(u -> {
            byte[] b = new byte[32]; new SecureRandom().nextBytes(b);
            String token = Base64.getUrlEncoder().withoutPadding().encodeToString(b);
            var t = new TokenRecuperacion();
            t.setIdUsuario(u.getId()); t.setTokenHash(sha256(token)); t.setVenceEn(LocalDateTime.now().plusMinutes(30));
            tokens.save(t);
            mail.enviar(u.getCorreo(), "Recupera tu contraseña de Freshmart",
                "Ingresa a este enlace para crear una nueva contraseña (vence en 30 minutos):\n" + front + "/restablecer?token=" + token);
        });
    }

    @Transactional
    public void restablecer(RestablecerRequest r) {
        if (!r.password().equals(r.confirmarPassword()))
            throw new ApiException(BAD_REQUEST, "Las contraseñas no coinciden. Vuelve a escribirlas.");
        var t = tokens.findByTokenHash(sha256(r.token()))
            .filter(x -> !x.isUsado() && x.getVenceEn().isAfter(LocalDateTime.now()))
            .orElseThrow(() -> new ApiException(BAD_REQUEST, "El enlace no es válido o ya venció. Solicita uno nuevo."));
        var u = usuarios.findById(t.getIdUsuario()).orElseThrow();
        u.setPasswordHash(encoder.encode(r.password()));
        t.setUsado(true);
        jdbc.update("UPDATE usuario SET sesiones_desde = ? WHERE id_usuario = ?", new java.sql.Timestamp(System.currentTimeMillis() / 1000 * 1000), u.getId()); // cierra otras sesiones
    }

    static String sha256(String s) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8))); }
        catch (Exception e) { throw new IllegalStateException(e); }
    }
}
