package VitaFortis.demo.v1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "RECUPERACAO_SENHA", indexes = {
        @Index(name = "IX_RECUPERACAO_TOKEN", columnList = "TOKEN_HASH", unique = true)
})
public class RecuperacaoSenha {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RECUPERACAO_ID")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "USUARIO_ID", nullable = false)
    private Usuario usuario;

    @Column(name = "TOKEN_HASH", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "EXPIRA_EM", nullable = false)
    private LocalDateTime expiraEm;

    @Column(name = "CRIADO_EM", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "UTILIZADO_EM")
    private LocalDateTime utilizadoEm;
}
