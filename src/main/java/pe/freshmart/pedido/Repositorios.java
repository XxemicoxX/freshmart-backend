package pe.freshmart.pedido;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public final class Repositorios {
    private Repositorios() {}

    public interface Pedidos extends JpaRepository<Pedido, Integer> {
        List<Pedido> findByIdUsuarioAndEstado(Integer idUsuario, EstadoPedido estado);

        @EntityGraph(attributePaths = {"tienda", "detalles"})
        @Query("select p from Pedido p where p.idUsuario = :usuario and p.estado in :estados order by p.fechaPedido desc")
        Page<Pedido> historial(@Param("usuario") Integer usuario, @Param("estados") Set<EstadoPedido> estados, Pageable pageable);

        @EntityGraph(attributePaths = {"tienda", "detalles"})
        @Query("select p from Pedido p where p.tienda.id = :tienda and p.estado in :estados order by p.fechaPedido desc")
        Page<Pedido> porTienda(@Param("tienda") Integer tienda, @Param("estados") Set<EstadoPedido> estados, Pageable pageable);
    }

    public interface Pagos extends JpaRepository<Pago, Long> {
        Optional<Pago> findFirstByIdPedidoAndEstadoOrderByIdDesc(Integer idPedido, EstadoPago estado);
        Optional<Pago> findFirstByIdPedidoOrderByIdDesc(Integer idPedido);
    }

    public interface Comprobantes extends JpaRepository<Comprobante, Integer> {
        Optional<Comprobante> findByIdPago(Long idPago);
    }
}
