package VitaFortis.demo.v1.repository;

import VitaFortis.demo.v1.entity.AuditoriaSolicitacaoPrivacidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditoriaSolicitacaoPrivacidadeRepository extends JpaRepository<AuditoriaSolicitacaoPrivacidade, Long> {
    List<AuditoriaSolicitacaoPrivacidade> findAllBySolicitacaoIdOrderByCriadaEmAsc(Long solicitacaoId);
}
