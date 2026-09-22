package VitaFortis.demo.v1.service;

import VitaFortis.demo.v1.entity.*;
import VitaFortis.demo.v1.enums.*;
import VitaFortis.demo.v1.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CashbackEscopoRegrasTest {
    @Mock MovimentoCashbackRepository movimentos; @Mock UsuarioRepository usuarios;
    @Mock CupomRepository cupons; @Mock PedidoRepository pedidos;
    @Mock CashbackService.PedidoServiceHolder holder;
    CashbackService service;

    @BeforeEach void setup() { service = new CashbackService(movimentos, usuarios, cupons, pedidos, holder); }

    @Test void escolhaPixOuDescontoFicaPersistida() {
        Usuario colaborador = colaborador();
        when(usuarios.findByEmail("colab@teste.com")).thenReturn(Optional.of(colaborador));

        var pix = service.escolherDestino("colab@teste.com", DestinoCashback.PIX);
        assertEquals(DestinoCashback.PIX, pix.destinoCashback());
        verify(usuarios).save(colaborador);

        var desconto = service.escolherDestino("colab@teste.com", DestinoCashback.DESCONTO);
        assertEquals(DestinoCashback.DESCONTO, desconto.destinoCashback());
        verify(usuarios, times(2)).save(colaborador);
    }

    @Test void resumoDoColaboradorExpoeSomenteSaldoEDestinoSemConsultarPedidosDeTerceiros() {
        Usuario colaborador = colaborador(); colaborador.setDestinoCashback(DestinoCashback.PIX);
        when(usuarios.findByEmail("colab@teste.com")).thenReturn(Optional.of(colaborador));

        var resumo = service.meuResumo("colab@teste.com");

        assertEquals(new BigDecimal("30.00"), resumo.saldo());
        assertEquals(3, resumo.getClass().getRecordComponents().length);
        verifyNoInteractions(pedidos, cupons, movimentos);
    }

    @Test void cashbackUsaPercentualPersistidoDoCupomSobrePrecoTotalDosProdutos() {
        Usuario colaborador = colaborador();
        Cupom cupom = new Cupom(); cupom.setColaborador(colaborador); cupom.setPercentualCashback(new BigDecimal("10"));
        Pedido pedido = new Pedido(); pedido.setId(9L); pedido.setCupomUtilizado(cupom);
        pedido.setSubtotal(new BigDecimal("100.00")); pedido.setDesconto(new BigDecimal("10.00"));
        pedido.setFrete(new BigDecimal("50.00"));
        when(movimentos.existsByPedidoIdAndTipo(9L, TipoMovimentoCashback.CREDITO)).thenReturn(false);
        ArgumentCaptor<MovimentoCashback> captor = ArgumentCaptor.forClass(MovimentoCashback.class);

        service.creditarPedido(pedido);

        verify(movimentos).save(captor.capture());
        assertEquals(new BigDecimal("10.00"), captor.getValue().getValor());
        assertEquals(new BigDecimal("40.00"), colaborador.getSaldoCashback());
    }

    private Usuario colaborador() {
        Usuario usuario = new Usuario(); usuario.setId(7L); usuario.setTipo(TipoUsuario.COLABORADOR);
        usuario.setSaldoCashback(new BigDecimal("30.00")); return usuario;
    }
}
