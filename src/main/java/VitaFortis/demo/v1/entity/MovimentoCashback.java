package VitaFortis.demo.v1.entity;

import VitaFortis.demo.v1.enums.TipoMovimentoCashback;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "MOVIMENTO_CASHBACK")
public class MovimentoCashback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MOVIMENTO_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "COLABORADOR_ID", nullable = false)
    private Usuario colaborador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PEDIDO_ID")
    private Pedido pedido;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO", nullable = false, length = 20)
    private TipoMovimentoCashback tipo;

    @Column(name = "VALOR", nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(name = "SALDO_ANTERIOR", nullable = false, precision = 12, scale = 2)
    private BigDecimal saldoAnterior;

    @Column(name = "SALDO_NOVO", nullable = false, precision = 12, scale = 2)
    private BigDecimal saldoNovo;

    @Column(name = "JUSTIFICATIVA", nullable = false, length = 300)
    private String justificativa;

    @Column(name = "RESPONSAVEL", nullable = false, length = 255)
    private String responsavel;

    @CreationTimestamp
    @Column(name = "CRIADO_EM", nullable = false, updatable = false)
    private LocalDateTime criadoEm;
}
