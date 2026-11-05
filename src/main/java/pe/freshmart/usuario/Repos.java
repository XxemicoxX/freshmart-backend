package pe.freshmart.usuario;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public final class Repos {
    private Repos() {}

    public interface Usuarios extends JpaRepository<Usuario, Integer> {
        Optional<Usuario> findByCorreoIgnoreCase(String correo);
        boolean existsByCorreoIgnoreCase(String correo);
    }

    public interface Roles extends JpaRepository<Rol, Integer> {
        Optional<Rol> findByNombre(String nombre);
    }

    public interface Tokens extends JpaRepository<TokenRecuperacion, Long> {
        Optional<TokenRecuperacion> findByTokenHash(String tokenHash);
    }
}