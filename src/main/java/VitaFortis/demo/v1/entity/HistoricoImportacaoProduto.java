package VitaFortis.demo.v1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "historico_importacao_produto")
@Getter @Setter
public class HistoricoImportacaoProduto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 255)
    private String nomeArquivo;
    @Column(nullable = false, length = 64)
    private String hashArquivo;
    @Column(nullable = false, length = 160)
    private String responsavel;
    @Column(nullable = false)
    private LocalDateTime criadoEm;
    @Column(nullable = false, length = 20)
    private String resultado;
    private int inseridos;
    private int atualizados;
    private int rejeitados;
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] arquivo;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String detalhes;
}
