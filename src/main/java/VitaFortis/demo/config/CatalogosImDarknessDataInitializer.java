package VitaFortis.demo.config;

import VitaFortis.demo.v1.entity.Produto;
import VitaFortis.demo.v1.entity.ProdutoImagem;
import VitaFortis.demo.v1.repository.ProdutoImagemRepository;
import VitaFortis.demo.v1.repository.ProdutoRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Component
@Order(20)
@ConditionalOnProperty(
        name = "vita-fortis.catalogos-im-darkness.importar",
        havingValue = "true",
        matchIfMissing = true
)
public class CatalogosImDarknessDataInitializer implements CommandLineRunner {

    private static final String CATALOGO = "catalogos/im-darkness/catalogo-im-darkness.json";
    private final ProdutoRepository produtos;
    private final ProdutoImagemRepository imagens;
    private final ObjectMapper objectMapper;

    public CatalogosImDarknessDataInitializer(
            ProdutoRepository produtos,
            ProdutoImagemRepository imagens,
            ObjectMapper objectMapper
    ) {
        this.produtos = produtos;
        this.imagens = imagens;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        try (InputStream arquivo = new ClassPathResource(CATALOGO).getInputStream()) {
            List<ProdutoCatalogo> catalogo = objectMapper.readValue(arquivo, new TypeReference<>() {});
            for (ProdutoCatalogo item : catalogo) {
                persistirImagem(item);
                if (produtos.findByCodigoIgnoreCase(item.codigo()).isPresent()) continue;

                Produto produto = new Produto();
                produto.setCodigo(item.codigo());
                produto.setNome(item.nome());
                produto.setDescricao(item.descricao());
                produto.setMarca(item.marca());
                produto.setUnidade(item.unidade());
                produto.setCategoria(item.categoria());
                produto.setSubcategoria(item.subcategoria());
                produto.setObjetivos(item.objetivos());
                produto.setImagemUrl("/uploads/produtos/" + item.imagemNome());
                produto.setImagemContentType("image/webp");
                produto.setPreco(new BigDecimal("1.00"));
                produto.setQuantidadeEstoque(1);
                produto.setAtivo(true);
                produtos.save(produto);
            }
        }
    }

    private void persistirImagem(ProdutoCatalogo item) throws Exception {
        if (imagens.existsById(item.imagemNome())) return;
        byte[] conteudo = new ClassPathResource(item.imagemResource()).getContentAsByteArray();
        if (conteudo.length == 0 || conteudo.length > 5 * 1024 * 1024) {
            throw new IllegalStateException("Imagem inválida para " + item.codigo());
        }
        imagens.save(new ProdutoImagem(item.imagemNome(), "image/webp", conteudo));
    }

    private record ProdutoCatalogo(
            String codigo,
            String nome,
            String descricao,
            String marca,
            String unidade,
            String categoria,
            String subcategoria,
            Set<String> objetivos,
            String imagemResource,
            String imagemNome
    ) {}
}
