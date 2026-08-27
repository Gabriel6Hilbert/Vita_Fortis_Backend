package VitaFortis.demo.v1.entity;

import VitaFortis.demo.v1.enums.CategoriaProduto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ProdutoTest {
    @Test
    void calculaDescontoPercentual() {
        Produto produto = produto("100.00");
        produto.setDescontoPercentual(new BigDecimal("15.00"));
        assertThat(produto.getValorDesconto()).isEqualByComparingTo("15.00");
        assertThat(produto.getPrecoFinal()).isEqualByComparingTo("85.00");
    }

    @Test
    void calculaDescontoFixoSemUsarPercentual() {
        Produto produto = produto("100.00");
        produto.setDescontoValor(new BigDecimal("12.50"));
        assertThat(produto.getValorDesconto()).isEqualByComparingTo("12.50");
        assertThat(produto.getPrecoFinal()).isEqualByComparingTo("87.50");
    }

    @Test
    void limitaDescontoFixoAoPreco() {
        Produto produto = produto("50.00");
        produto.setDescontoValor(new BigDecimal("80.00"));
        assertThat(produto.getValorDesconto()).isEqualByComparingTo("50.00");
        assertThat(produto.getPrecoFinal()).isEqualByComparingTo("0.00");
    }

    private Produto produto(String preco) {
        Produto produto = new Produto();
        produto.setCodigo("TESTE-001");
        produto.setNome("Whey"); produto.setDescricao("Produto teste");
        produto.setPreco(new BigDecimal(preco)); produto.setQuantidadeEstoque(10);
        produto.setCategoria(CategoriaProduto.PROTEINAS); produto.setAtivo(true);
        return produto;
    }
}
