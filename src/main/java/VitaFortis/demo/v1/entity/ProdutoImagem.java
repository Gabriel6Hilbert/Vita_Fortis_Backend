package VitaFortis.demo.v1.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "produto_imagem")
public class ProdutoImagem {
    @Id
    @Column(name = "nome", length = 41, nullable = false, updatable = false)
    private String nome;

    @Column(name = "content_type", length = 20, nullable = false, updatable = false)
    private String contentType;

    // BLOB simples do MySQL comporta apenas 64 KB; MEDIUMBLOB preserva imagens de até 5 MB.
    @Lob
    @Column(name = "conteudo", nullable = false, updatable = false, columnDefinition = "MEDIUMBLOB")
    private byte[] conteudo;

    @Column(name = "criada_em", nullable = false, updatable = false)
    private Instant criadaEm;

    protected ProdutoImagem() {}

    public ProdutoImagem(String nome, String contentType, byte[] conteudo) {
        this.nome = nome;
        this.contentType = contentType;
        this.conteudo = conteudo;
        this.criadaEm = Instant.now();
    }

    public String getNome() { return nome; }
    public String getContentType() { return contentType; }
    public byte[] getConteudo() { return conteudo; }
}
