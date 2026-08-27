package VitaFortis.demo.v1.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Getter @Setter @Entity
@Table(name="AVALIACAO_PRODUTO", uniqueConstraints=@UniqueConstraint(columnNames={"PRODUTO_ID","USUARIO_ID"}))
public class AvaliacaoProduto {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="AVALIACAO_ID") private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="PRODUTO_ID") private Produto produto;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="USUARIO_ID") private Usuario usuario;
    @Min(1) @Max(5) @Column(nullable=false) private int nota;
    @Size(max=1000) @Column(length=1000) private String comentario;
    @Column(nullable=false) private boolean aprovado = true;
    @CreationTimestamp @Column(nullable=false, updatable=false) private LocalDateTime criadoEm;
}
