package VitaFortis.demo.v1.entity;

import VitaFortis.demo.v1.enums.TipoResgate;
import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "RESGATE_FIDELIDADE")
public class RegateFidelidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RESGATE_ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "PONTOS_UTILIZADOS")
    private int pontosUtilizados;

    @Column(name = "DATA_RESGATE")
    private LocalDate dataResgate;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO_RESGATE")
    private TipoResgate tipoResgate;

    @ManyToOne
    @JoinColumn(name = "CUPOM_ID", nullable = true)
    private Cupom cupomResgatado;

    @ManyToOne
    @JoinColumn(name = "PRODUTO_ID", nullable = true)
    private Produto produtoResgatado;

    @AssertTrue(message="Informe cupom ou produto para resgatar")
    public boolean isAlvoValido(){ return cupomResgatado != null ^ produtoResgatado != null; }

}
