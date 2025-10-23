package VitaFortis.demo.v1.entity;

import VitaFortis.demo.v1.enums.CupomTipo;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "CUPOM",
        indexes = @Index(name = "ID_CUPOM_CODIGO", columnList = "CODIGO", unique = true)
)
public class Cupom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CUPOM_ID")
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Column(name = "CODIGO", nullable = false, length = 50)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(name="TIPO", nullable=false, length=20)
    private CupomTipo tipo;

    @Size(max = 255)
    @Column(name = "DESCRICAO", length = 255)
    private String descricao;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "DESCONTO", nullable = false, precision = 12, scale = 2)
    private BigDecimal desconto;

    @Column(name = "ATIVO", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private boolean ativo = true;

    @Column(name="MIN_SUBTOTAL", precision=12, scale=2)
    private BigDecimal minSubtotal;

    @CreationTimestamp
    @Column(name = "DATA_CADASTRO", nullable = false, updatable = false)
    private LocalDateTime dataCadastro;

    @Column(name = "DATA_VENCIMENTO")
    private LocalDateTime dataVencimento; 

    @AssertTrue(message = "DATA_VENCIMENTO deve ser posterior à DATA_CADASTRO")
    private boolean isVencimentoValido() {
        return dataVencimento == null || dataCadastro == null || dataVencimento.isAfter(dataCadastro);
    }
}
