package VitaFortis.demo.v1.entity;

import VitaFortis.demo.v1.enums.StatusSolicitacaoPrivacidade;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "AUDITORIA_SOLICITACAO_PRIVACIDADE")
public class AuditoriaSolicitacaoPrivacidade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AUDITORIA_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "SOLICITACAO_ID", nullable = false)
    private SolicitacaoPrivacidade solicitacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS_ANTERIOR", nullable = false, length = 20)
    private StatusSolicitacaoPrivacidade statusAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS_NOVO", nullable = false, length = 20)
    private StatusSolicitacaoPrivacidade statusNovo;

    @Column(name = "DECISAO", nullable = false, length = 500)
    private String decisao;

    @Column(name = "JUSTIFICATIVA", nullable = false, length = 1000)
    private String justificativa;

    @Column(name = "RESPONSAVEL", nullable = false, length = 255)
    private String responsavel;

    @CreationTimestamp
    @Column(name = "CRIADA_EM", nullable = false, updatable = false)
    private LocalDateTime criadaEm;
}
