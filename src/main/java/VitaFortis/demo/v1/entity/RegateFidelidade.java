package VitaFortis.demo.v1.entity;

import VitaFortis.demo.v1.enums.TipoResgate;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "Resgate_Fidelidade")
public class RegateFidelidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    private int pontosUtilizados;
    private LocalDate dataResgate;

    @Enumerated(EnumType.STRING)
    private TipoResgate tipoResgate;

    @ManyToOne
    @JoinColumn(name = "cupom_id", nullable = true)
    private Cupom cupomResgatado;

    @ManyToOne
    @JoinColumn(name = "produto_id", nullable = true)
    private Produto produtoResgatado;
}
