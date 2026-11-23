package VitaFortis.demo.v1.service;

import VitaFortis.demo.v1.dto.ProdutoRequestDto;
import VitaFortis.demo.v1.dto.ProdutoResponseDto;
import VitaFortis.demo.v1.entity.Produto;
import VitaFortis.demo.v1.mapper.ProdutoMapper;
import VitaFortis.demo.v1.repository.HistoricoImportacaoProdutoRepository;
import VitaFortis.demo.v1.repository.MovimentacaoEstoqueRepository;
import VitaFortis.demo.v1.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProdutoKitServiceTest {
    @Mock ProdutoRepository produtos;
    @Mock ProdutoMapper mapper;
    @Mock MovimentacaoEstoqueRepository movimentacoes;
    @Mock CatalogoCadastroService cadastros;
    @Mock HistoricoImportacaoProdutoRepository importacoes;
    ProdutoService service;

    @BeforeEach
    void configurar() {
        service = new ProdutoService(produtos, mapper, movimentacoes, cadastros, importacoes);
    }

    @Test
    void salvaKitComoProdutoUnicoComEstoqueEValorComparativoProprios() {
        ProdutoRequestDto request = request();
        Produto kit = entity(true, "150.00", "210.00");
        kit.getAtributos().put("quantidadeItens", "3");
        kit.getAtributos().put("conteudoKit", "Whey, creatina e coqueteleira");
        when(mapper.toEntity(request)).thenReturn(kit);
        when(produtos.save(kit)).thenReturn(kit);
        when(mapper.toResponseDto(kit)).thenReturn(new ProdutoResponseDto());

        service.create(request);

        assertThat(kit.getQuantidadeEstoque()).isEqualTo(5);
        assertThat(kit.getValorItensSeparados()).isEqualByComparingTo("210.00");
    }

    @Test
    void rejeitaComparacaoQueNaoRepresentaEconomiaReal() {
        ProdutoRequestDto request = request();
        Produto kit = entity(true, "150.00", "150.00");
        kit.getAtributos().put("conteudoKit", "Whey e creatina");
        when(mapper.toEntity(request)).thenReturn(kit);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("itens separados");
    }

    @Test
    void removeCamposDeKitQuandoItemVoltaASerProduto() {
        ProdutoRequestDto request = request();
        Produto produto = entity(false, "150.00", "210.00");
        produto.getAtributos().put("quantidadeItens", "3");
        produto.getAtributos().put("conteudoKit", "Whey e creatina");
        when(mapper.toEntity(request)).thenReturn(produto);
        when(produtos.save(produto)).thenReturn(produto);
        when(mapper.toResponseDto(produto)).thenReturn(new ProdutoResponseDto());

        service.create(request);

        assertThat(produto.getValorItensSeparados()).isNull();
        assertThat(produto.getAtributos()).doesNotContainKeys("quantidadeItens", "conteudoKit");
    }

    private ProdutoRequestDto request() {
        ProdutoRequestDto request = new ProdutoRequestDto();
        request.setCodigo("KIT-TESTE");
        return request;
    }

    private Produto entity(boolean kit, String preco, String separado) {
        Produto produto = new Produto();
        produto.setCodigo("KIT-TESTE");
        produto.setNome("Kit teste");
        produto.setDescricao("Descrição");
        produto.setCategoria("KITS");
        produto.setPreco(new BigDecimal(preco));
        produto.setQuantidadeEstoque(5);
        produto.setKit(kit);
        produto.setValorItensSeparados(new BigDecimal(separado));
        produto.setAtributos(new HashMap<>());
        return produto;
    }
}
