package pe.freshmart.catalogo;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    Optional<Producto> findByIdAndActivoTrue(Integer id);

    @Query(value = "select p from Producto p join p.categoria c where p.activo = true "
        + "and (:categoriaId is null or c.id = :categoriaId) "
        + "and (lower(p.nombre) like :like or lower(coalesce(p.descripcion, '')) like :like or lower(c.nombre) like :like)",
        countQuery = "select count(p) from Producto p join p.categoria c where p.activo = true "
        + "and (:categoriaId is null or c.id = :categoriaId) "
        + "and (lower(p.nombre) like :like or lower(coalesce(p.descripcion, '')) like :like or lower(c.nombre) like :like)")
    Page<Producto> buscar(@Param("categoriaId") Integer categoriaId, @Param("like") String like, Pageable pageable);
}
