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
import java.math.BigDecimal;
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
                Produto produto = produtos.findByCodigoIgnoreCase(item.codigo()).orElseGet(Produto::new);
                if (produto.getId() == null) {
                    produto.setCodigo(item.codigo());
                    produto.setNome(item.nome());
                    produto.setDescricao(item.descricao());
                    produto.setMarca(item.marca());
                    produto.setUnidade(item.unidade());
                    produto.setCategoria(item.categoria());
                    produto.setSubcategoria(item.subcategoria());
                    produto.setImagemUrl(item.imagemUrl());
                    produto.setLancamento(item.lancamento());
                }
                if (produto.getPreco() == null) produto.setPreco(precoCatalogo(item.codigo()));
                if (produto.getQuantidadeEstoque() == null || produto.getQuantidadeEstoque() <= 0) {
                    produto.setQuantidadeEstoque(12 + Math.floorMod(item.codigo().hashCode(), 37));
                }
                produto.setAtivo(true);
                produtos.save(produto);
            }
        }
    }

    private BigDecimal precoCatalogo(String codigo) {
        int centavos = 3990 + Math.floorMod(codigo.hashCode(), 26000);
        return BigDecimal.valueOf(centavos, 2);
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
