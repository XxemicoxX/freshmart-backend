package pe.freshmart.tienda;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TiendaRepository extends JpaRepository<Tienda, Integer> {
	Optional<Tienda> findByIdAndActivaTrue(Integer id);
	List<Tienda> findAllByActivaTrueOrderByNombre();
}
