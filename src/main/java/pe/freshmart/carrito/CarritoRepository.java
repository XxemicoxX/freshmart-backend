package pe.freshmart.carrito;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarritoRepository extends JpaRepository<Carrito, Integer> {
    @EntityGraph(attributePaths = {"items", "items.producto", "items.producto.categoria", "tienda"})
    Optional<Carrito> findByIdUsuario(Integer idUsuario);
}
