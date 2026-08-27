package VitaFortis.demo.v1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Getter @Setter @Entity
@Table(name="FAVORITO", uniqueConstraints=@UniqueConstraint(columnNames={"USUARIO_ID","PRODUTO_ID"}))
public class Favorito {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="FAVORITO_ID") private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="USUARIO_ID") private Usuario usuario;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="PRODUTO_ID") private Produto produto;
    @CreationTimestamp @Column(nullable=false, updatable=false) private LocalDateTime criadoEm;
}
