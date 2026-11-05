package pe.freshmart.auth;

import java.time.LocalDate;
import pe.freshmart.usuario.Usuario;

public final class AuthDtos {
    private AuthDtos() {}
    public record RegistroRequest(String nombres, String apellidos, String correo, LocalDate fechaNacimiento,
                                  String password, String confirmarPassword) {}
    public record LoginRequest(String correo, String password) {}
    public record RecuperarRequest(String correo) {}
    public record RestablecerRequest(String token, String password, String confirmarPassword) {}
    public record UsuarioDto(Integer id, String nombres, String apellidos, String correo, String rol) {
        public static UsuarioDto de(Usuario usuario) {
            return new UsuarioDto(usuario.getId(), usuario.getNombres(), usuario.getApellidos(),
                usuario.getCorreo(), usuario.getRol().getNombre());
        }
    }
    public record LoginResponse(String token, UsuarioDto usuario) {}
}
