package VitaFortis.demo.v1.service;

import VitaFortis.demo.v1.entity.*;
import VitaFortis.demo.v1.dto.*;
import VitaFortis.demo.v1.enums.*;
import VitaFortis.demo.v1.integration.PagamentoGateway;
import VitaFortis.demo.v1.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoPagamentoEstoqueTest {
    @Mock PedidoRepository pedidos; @Mock UsuarioRepository usuarios; @Mock ProdutoRepository produtos;
    @Mock CupomRepository cupons; @Mock EnderecoService enderecos; @Mock FreteService fretes;
    @Mock PagamentoGateway pagamentos; @Mock CashbackService cashback;
    PedidoService service;

    @BeforeEach void setup() {
        service = new PedidoService(pedidos, usuarios, produtos, cupons, enderecos, fretes,
                pagamentos, cashback, new CupomRegrasService(pedidos));
    }

    @Test void pagamentoRecusadoNaoBaixaEstoque() {
        Pedido pedido = pedidoPendente();
        when(pedidos.buscarComCupomEColaborador(1L)).thenReturn(Optional.of(pedido));
        when(pedidos.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.confirmarPagamento(1L, false, null);

        verify(produtos, never()).debitarEstoque(anyLong(), anyInt());
        verify(produtos, never()).creditarEstoque(anyLong(), anyInt());
    }

    @Test void pedidoPendenteValidaDisponibilidadeSemBaixarEstoque() {
        Usuario cliente = new Usuario(); cliente.setId(2L); cliente.setTipo(TipoUsuario.CLIENTE);
        Produto produto = new Produto(); produto.setId(10L); produto.setNome("Produto");
        produto.setPreco(new BigDecimal("15.00")); produto.setQuantidadeEstoque(5); produto.setAtivo(true);
        ItemCompraRequestDto item = new ItemCompraRequestDto(); item.setProdutoId(10L); item.setQuantidade(2);
        PedidoRequestDto dto = new PedidoRequestDto(); dto.setUsuarioId(2L); dto.setItens(List.of(item));
        dto.setFormaRecebimento(FormaRecebimento.RETIRADA); dto.setFormaPagamento("PIX");
        when(usuarios.findByEmail("cliente@teste.com")).thenReturn(Optional.of(cliente));
        when(produtos.findByIdAndAtivoTrue(10L)).thenReturn(Optional.of(produto));
        when(pedidos.save(any())).thenAnswer(invocation -> { Pedido p = invocation.getArgument(0); p.setId(1L); return p; });
        when(pagamentos.iniciar(1L, new BigDecimal("30.00"), "PIX"))
                .thenReturn(new PagamentoGateway.PagamentoPendente("ref-1"));

        service.criar(dto, "cliente@teste.com");

        verify(produtos, never()).debitarEstoque(anyLong(), anyInt());
        verify(produtos, never()).alterarTotalVendido(anyLong(), anyInt());
    }

    @Test void pagamentoConfirmadoBaixaEstoqueUmaUnicaVez() {
        Pedido pedido = pedidoPendente();
        when(pedidos.buscarComCupomEColaborador(1L)).thenReturn(Optional.of(pedido));
        when(pedidos.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(produtos.debitarEstoque(10L, 2)).thenReturn(1);

        service.confirmarPagamento(1L, true, null);
        assertThrows(IllegalArgumentException.class, () -> service.confirmarPagamento(1L, true, null));

        verify(produtos, times(1)).debitarEstoque(10L, 2);
        verify(produtos, times(1)).alterarTotalVendido(10L, 2);
        verify(cashback, times(1)).creditarPedido(pedido);
    }

    private Pedido pedidoPendente() {
        Produto produto = new Produto(); produto.setId(10L); produto.setNome("Produto");
        ItemCompra item = new ItemCompra(); item.setProduto(produto); item.setQuantidade(2);
        Usuario cliente = new Usuario(); cliente.setId(2L);
        Pedido pedido = new Pedido(); pedido.setId(1L); pedido.setUsuario(cliente);
        pedido.setStatus(StatusCompra.PENDENTE); pedido.setStatusPagamento(StatusPagamento.PENDENTE);
        pedido.setFormaRecebimento(FormaRecebimento.RETIRADA); pedido.setFormaPagamento("PIX");
        pedido.setSubtotal(BigDecimal.TEN); pedido.setDesconto(BigDecimal.ZERO); pedido.setFrete(BigDecimal.ZERO); pedido.setTotal(BigDecimal.TEN);
        pedido.setItems(List.of(item)); pedido.setHistoricoStatus(new LinkedHashSet<>());
        return pedido;
    }
}
