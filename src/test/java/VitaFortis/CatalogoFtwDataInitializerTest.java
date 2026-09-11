package VitaFortis;

import VitaFortis.demo.v1.repository.ProdutoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "vita-fortis.catalogo-ftw.importar=true")
class CatalogoFtwDataInitializerTest {

    @Autowired
    private ProdutoRepository produtos;

    @Test
    void importaOs179ProdutosAtivosComPrecoEstoqueEImagemLocal() {
        var catalogo = produtos.findAll().stream()
                .filter(produto -> produto.getCodigo() != null && produto.getCodigo().startsWith("PA"))
                .toList();

        assertThat(catalogo).hasSize(179);
        assertThat(catalogo).allSatisfy(produto -> {
            assertThat(produto.isAtivo()).isTrue();
            assertThat(produto.getPreco()).isPositive();
            assertThat(produto.getQuantidadeEstoque()).isPositive();
            assertThat(produto.getDescricao()).isNotBlank();
            assertThat(produto.getImagemUrl()).startsWith("/assets/");
        });
    }
}
