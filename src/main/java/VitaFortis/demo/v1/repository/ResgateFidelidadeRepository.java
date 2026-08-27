package VitaFortis.demo.v1.repository;

import VitaFortis.demo.v1.entity.RegateFidelidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResgateFidelidadeRepository extends JpaRepository<RegateFidelidade, Long> {
    List<RegateFidelidade> findAllByUsuarioIdOrderByDataResgateDesc(Long usuarioId);
}
