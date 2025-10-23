package VitaFortis.demo.v1.repository;

import VitaFortis.demo.v1.entity.Cupom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CupomRepository extends JpaRepository<Cupom, Long> {

    Optional<Cupom> findByCodigoIgnoreCase(String codigo);
}
