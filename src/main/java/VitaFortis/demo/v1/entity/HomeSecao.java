package VitaFortis.demo.v1.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "home_secao", uniqueConstraints = @UniqueConstraint(name = "uk_home_secao_chave", columnNames = "chave"))
public class HomeSecao {
    public enum Modo { MANUAL, DINAMICO }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "home_secao_id")
    private Long id;

    @Column(nullable = false, length = 40)
    private String chave;

    @Column(nullable = false)
    private boolean ativa = true;

    @Column(nullable = false)
    private int ordem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Modo modo = Modo.DINAMICO;

    @Size(max = 120)
    @Column(length = 120)
    private String titulo;

    @Size(max = 500)
    @Column(length = 500)
    private String subtitulo;

    @Size(max = 500)
    @Column(name = "media_url", length = 500)
    private String mediaUrl;

    @Size(max = 1000)
    @Column(length = 1000)
    private String texto;

    @Min(1)
    @Max(20)
    @Column(nullable = false)
    private int limite = 4;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "home_secao_produto", joinColumns = @JoinColumn(name = "home_secao_id"))
    @OrderColumn(name = "posicao")
    @Column(name = "produto_id", nullable = false)
    private List<Long> produtoIds = new ArrayList<>();
}
