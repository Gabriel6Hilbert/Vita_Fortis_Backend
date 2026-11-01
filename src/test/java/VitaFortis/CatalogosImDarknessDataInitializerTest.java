package VitaFortis;

import VitaFortis.demo.v1.repository.ProdutoImagemRepository;
import VitaFortis.demo.v1.repository.ProdutoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "vita-fortis.catalogos-im-darkness.importar=true")
class CatalogosImDarknessDataInitializerTest {

    @Autowired ProdutoRepository produtos;
    @Autowired ProdutoImagemRepository imagens;
    @Autowired VitaFortis.demo.config.CatalogosImDarknessDataInitializer inicializador;

    @Test
    void importa37ProdutosAtivosComPrecoEstoqueEImagensPersistidasNoBanco() {
        var catalogo = produtos.findAll().stream()
                .filter(produto -> produto.getCodigo().startsWith("IM-2026-")
                        || produto.getCodigo().startsWith("DARK-2026-"))
                .toList();

        assertThat(catalogo).hasSize(37);
        assertThat(catalogo).allSatisfy(produto -> {
            assertThat(produto.isAtivo()).isTrue();
            assertThat(produto.getPreco()).isEqualByComparingTo("1.00");
            assertThat(produto.getQuantidadeEstoque()).isEqualTo(1);
            assertThat(produto.getDescricao()).isNotBlank();
            assertThat(produto.getImagemUrl()).startsWith("/uploads/produtos/").endsWith(".webp");
            assertThat(imagens.existsById(produto.getImagemUrl().replace("/uploads/produtos/", ""))).isTrue();
        });
    }

    @Test
    void segundaExecucaoNaoSobrescreveProdutoRevisado() throws Exception {
        inicializador.run();
        var produto = produtos.findAll().stream()
                .filter(item -> item.getCodigo().startsWith("IM-2026-")
                        || item.getCodigo().startsWith("DARK-2026-"))
                .findFirst()
                .orElseThrow();
        String codigo = produto.getCodigo();
        produto.setPreco(new java.math.BigDecimal("7.50"));
        produto.setQuantidadeEstoque(9);
        produtos.saveAndFlush(produto);

        inicializador.run();

        var revisado = produtos.findByCodigoIgnoreCase(codigo).orElseThrow();
        assertThat(revisado.getPreco()).isEqualByComparingTo("7.50");
        assertThat(revisado.getQuantidadeEstoque()).isEqualTo(9);
    }
}
