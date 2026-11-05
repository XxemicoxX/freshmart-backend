package pe.freshmart.usuario;

import java.time.LocalDate;

public final class UsuarioDtos {
    private UsuarioDtos() {}

    public record PerfilDto(Integer id, String nombres, String apellidos, String correo, LocalDate fechaNacimiento,
                            String telefono, String rol, String estado) {}
    public record PerfilRequest(String nombres, String apellidos, String correo, LocalDate fechaNacimiento,
                                String telefono, String passwordActual) {}
    public record PasswordRequest(String passwordActual, String passwordNueva, String confirmar) {}
    public record PasswordResponse(String mensaje, String token) {}
    public record ConfiguracionDto(boolean notifPedidos, boolean notifPromociones, boolean notifCorreo, String tema) {}
    public record EliminarCuentaRequest(String password) {}
}
