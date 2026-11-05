package pe.freshmart.notificacion;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FaqRepository extends JpaRepository<PreguntaFrecuente, Integer> {
    List<PreguntaFrecuente> findAllByOrderByCategoriaAscIdAsc();
}
