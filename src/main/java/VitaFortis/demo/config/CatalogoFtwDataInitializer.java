package VitaFortis.demo.config;

import VitaFortis.demo.v1.entity.Produto;
import VitaFortis.demo.v1.enums.CategoriaProduto;
import VitaFortis.demo.v1.repository.ProdutoRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;

@Component
@Order(10)
@ConditionalOnProperty(name = "vita-fortis.catalogo-ftw.importar", havingValue = "true")
public class CatalogoFtwDataInitializer implements CommandLineRunner {

    private final ProdutoRepository produtos;
    private final ObjectMapper objectMapper;

    public CatalogoFtwDataInitializer(ProdutoRepository produtos, ObjectMapper objectMapper) {
        this.produtos = produtos;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        try (InputStream arquivo = new ClassPathResource("catalogo-ftw.json").getInputStream()) {
            List<ProdutoCatalogo> catalogo = objectMapper.readValue(arquivo, new TypeReference<>() {});
            for (ProdutoCatalogo item : catalogo) {
                if (produtos.existsByCodigoIgnoreCase(item.codigo())) continue;

                Produto produto = new Produto();
                produto.setCodigo(item.codigo());
                produto.setNome(item.nome());
                produto.setDescricao(item.descricao());
                produto.setMarca(item.marca());
                produto.setUnidade(item.unidade());
                produto.setCategoria(item.categoria());
                produto.setSubcategoria(item.subcategoria());
                produto.setImagemUrl(item.imagemUrl());
                produto.setLancamento(item.lancamento());
                produto.setPreco(null);
                produto.setQuantidadeEstoque(0);
                produto.setAtivo(false);
                produtos.save(produto);
            }
        }
    }

    private record ProdutoCatalogo(
            String codigo,
            String nome,
            String descricao,
            String marca,
            String unidade,
            CategoriaProduto categoria,
            String subcategoria,
            String imagemUrl,
            boolean lancamento
    ) {}
}
