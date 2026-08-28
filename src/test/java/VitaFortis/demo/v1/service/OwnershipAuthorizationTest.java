package VitaFortis.demo.v1.service;

import VitaFortis.demo.v1.entity.Usuario;
import VitaFortis.demo.v1.enums.TipoUsuario;
import VitaFortis.demo.v1.integration.PagamentoGateway;
import VitaFortis.demo.v1.mapper.CarrinhoItemMapper;
import VitaFortis.demo.v1.mapper.CarrinhoMapper;
import VitaFortis.demo.v1.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OwnershipAuthorizationTest {
    @Mock CarrinhoRepository carrinhos; @Mock CarrinhoItemRepository itens; @Mock ProdutoRepository produtos;
    @Mock CarrinhoMapper carrinhoMapper; @Mock CarrinhoItemMapper itemMapper; @Mock UsuarioRepository usuarios;
    @Mock CupomRepository cupons; @Mock PedidoRepository pedidos; @Mock EnderecoService enderecos;
    @Mock FreteService fretes; @Mock PagamentoGateway pagamentos; @Mock CashbackService cashback;
    CarrinhoService carrinhoService; PedidoService pedidoService;

    @BeforeEach void setup() {
        carrinhoService = new CarrinhoService(carrinhos, itens, produtos, carrinhoMapper, itemMapper, usuarios, cupons);
        pedidoService = new PedidoService(pedidos, usuarios, produtos, cupons, enderecos, fretes, pagamentos, cashback);
    }

    @Test void clienteNaoAcessaCarrinhoDeOutroUsuario() {
        Usuario atual = usuario(1L, TipoUsuario.CLIENTE);
        when(usuarios.findByEmail("cliente@teste.com")).thenReturn(Optional.of(atual));
        assertThrows(AccessDeniedException.class, () -> carrinhoService.obterCarrinho(2L, "cliente@teste.com"));
    }

    @Test void colaboradorNaoAcessaPedidosDeCliente() {
        Usuario atual = usuario(3L, TipoUsuario.COLABORADOR);
        when(usuarios.findByEmail("colaborador@teste.com")).thenReturn(Optional.of(atual));
        assertThrows(AccessDeniedException.class, () -> pedidoService.listarUsuario(2L, "colaborador@teste.com"));
    }

    private Usuario usuario(Long id, TipoUsuario tipo) {
        Usuario usuario = new Usuario(); usuario.setId(id); usuario.setTipo(tipo); usuario.setAtivo(true); return usuario;
    }
}
