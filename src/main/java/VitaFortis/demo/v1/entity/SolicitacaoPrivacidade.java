package VitaFortis.demo.v1.entity;

import VitaFortis.demo.v1.enums.StatusSolicitacaoPrivacidade;
import VitaFortis.demo.v1.enums.TipoSolicitacaoPrivacidade;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "SOLICITACAO_PRIVACIDADE", uniqueConstraints = @UniqueConstraint(columnNames = "PROTOCOLO"))
public class SolicitacaoPrivacidade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SOLICITACAO_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USUARIO_ID", nullable = false)
    private Usuario usuario;

    @Column(name = "PROTOCOLO", nullable = false, length = 40)
    private String protocolo;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO", nullable = false, length = 20)
    private TipoSolicitacaoPrivacidade tipo;

    @Column(name = "DESCRICAO", nullable = false, length = 1000)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private StatusSolicitacaoPrivacidade status;

    @Column(name = "DECISAO", length = 500)
    private String decisao;

    @Column(name = "JUSTIFICATIVA_ADMIN", length = 1000)
    private String justificativaAdmin;

    @Column(name = "RESPONSAVEL", length = 255)
    private String responsavel;

    @Column(name = "PROCESSADA_EM")
    private LocalDateTime processadaEm;

    @CreationTimestamp
    @Column(name = "CRIADA_EM", nullable = false, updatable = false)
    private LocalDateTime criadaEm;

    @UpdateTimestamp
    @Column(name = "ATUALIZADA_EM", nullable = false)
    private LocalDateTime atualizadaEm;
}
