package VitaFortis.demo.v1.repository;

import VitaFortis.demo.v1.entity.Cupom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface CupomRepository extends JpaRepository<Cupom, Long> {

    Optional<Cupom> findByCodigoIgnoreCase(String codigo);
    boolean existsByCodigoIgnoreCase(String codigo);
    List<Cupom> findAllByColaboradorIdOrderByCodigoAsc(Long colaboradorId);
}
