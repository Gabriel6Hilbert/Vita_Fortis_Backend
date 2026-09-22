package VitaFortis.demo.v1.repository;

import VitaFortis.demo.v1.entity.SolicitacaoPrivacidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SolicitacaoPrivacidadeRepository extends JpaRepository<SolicitacaoPrivacidade, Long> {
    List<SolicitacaoPrivacidade> findAllByUsuarioIdOrderByCriadaEmDesc(Long usuarioId);
    Optional<SolicitacaoPrivacidade> findByIdAndUsuarioId(Long id, Long usuarioId);
    List<SolicitacaoPrivacidade> findAllByOrderByCriadaEmDesc();
}
