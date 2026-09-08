package VitaFortis.demo.v1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Getter @Setter @Entity
@Table(name="MOVIMENTACAO_ESTOQUE", indexes=@Index(name="IX_MOV_ESTOQUE_PRODUTO_DATA", columnList="PRODUTO_ID,CRIADO_EM"))
public class MovimentacaoEstoque {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="MOVIMENTACAO_ID") private Long id;
    @ManyToOne(optional=false,fetch=FetchType.LAZY) @JoinColumn(name="PRODUTO_ID",nullable=false) private Produto produto;
    @Column(name="QUANTIDADE_ANTERIOR",nullable=false) private int quantidadeAnterior;
    @Column(name="QUANTIDADE_NOVA",nullable=false) private int quantidadeNova;
    @Column(name="VARIACAO",nullable=false) private int variacao;
    @Column(name="MOTIVO",nullable=false,length=255) private String motivo;
    @Column(name="RESPONSAVEL",nullable=false,length=160) private String responsavel;
    @CreationTimestamp @Column(name="CRIADO_EM",nullable=false,updatable=false) private LocalDateTime criadoEm;
}
