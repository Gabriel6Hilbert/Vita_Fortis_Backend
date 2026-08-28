package VitaFortis.demo.v1.repository;

import VitaFortis.demo.v1.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findAllByUsuarioIdOrderByDataPedidoDesc(Long usuarioId);
    List<Pedido> findByDataPedidoBetween(LocalDateTime inicio, LocalDateTime fim);
    List<Pedido> findAllByCupomUtilizadoColaboradorIdOrderByDataPedidoDesc(Long colaboradorId);

    @Query("select distinct p from Pedido p " +
            "left join fetch p.cupomUtilizado c left join fetch c.colaborador " +
            "left join fetch p.items i left join fetch i.produto " +
            "left join fetch p.historicoStatus where p.id = :id")
    Optional<Pedido> buscarComCupomEColaborador(@Param("id") Long id);
}
