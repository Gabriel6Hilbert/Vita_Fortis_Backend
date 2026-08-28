package VitaFortis.demo.v1.repository;

import VitaFortis.demo.v1.entity.MovimentoCashback;
import VitaFortis.demo.v1.enums.TipoMovimentoCashback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimentoCashbackRepository extends JpaRepository<MovimentoCashback, Long> {
    boolean existsByPedidoIdAndTipo(Long pedidoId, TipoMovimentoCashback tipo);
    List<MovimentoCashback> findAllByColaboradorIdOrderByCriadoEmDesc(Long colaboradorId);
}
