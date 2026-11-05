package pe.freshmart.usuario;

import static pe.freshmart.usuario.UsuarioDtos.*;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.freshmart.auth.JwtService;

@RestController
@RequestMapping("/api/v1/usuario")
public class UsuarioController {
    private final UsuarioService usuarios;
    private final JwtService jwt;

    public UsuarioController(UsuarioService usuarios, JwtService jwt) {
        this.usuarios = usuarios;
        this.jwt = jwt;
    }

    @GetMapping("/perfil")
    public PerfilDto perfil(@AuthenticationPrincipal Integer uid) { return usuarios.perfil(uid); }

    @PutMapping("/perfil")
    public PerfilDto actualizar(@AuthenticationPrincipal Integer uid, @RequestBody PerfilRequest request) {
        return usuarios.actualizar(uid, request);
    }

    @PatchMapping("/password")
    public PasswordResponse cambiarPassword(@AuthenticationPrincipal Integer uid, @RequestBody PasswordRequest request) {
        return usuarios.cambiarPassword(uid, request);
    }

    @GetMapping("/configuracion")
    public ConfiguracionDto configuracion(@AuthenticationPrincipal Integer uid) { return usuarios.configuracion(uid); }

    @PutMapping("/configuracion")
    public ConfiguracionDto guardarConfiguracion(@AuthenticationPrincipal Integer uid, @RequestBody ConfiguracionDto request) {
        return usuarios.guardarConfiguracion(uid, request);
    }

    @DeleteMapping
    public void eliminar(@AuthenticationPrincipal Integer uid, @RequestHeader("Authorization") String authorization,
                         @RequestBody EliminarCuentaRequest request) {
        Claims claims = jwt.validar(authorization.substring("Bearer ".length()));
        usuarios.eliminar(uid, request, claims);
    }
}
