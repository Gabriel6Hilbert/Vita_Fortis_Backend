package VitaFortis.demo.v1.repository;

import VitaFortis.demo.v1.entity.Cupom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface CupomRepository extends JpaRepository<Cupom, Long> {

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select c from Cupom c where c.id = :id")
    Optional<Cupom> buscarParaUso(@org.springframework.data.repository.query.Param("id") Long id);

    Optional<Cupom> findByCodigoIgnoreCase(String codigo);
    boolean existsByCodigoIgnoreCase(String codigo);
    List<Cupom> findAllByColaboradorIdOrderByCodigoAsc(Long colaboradorId);
}
