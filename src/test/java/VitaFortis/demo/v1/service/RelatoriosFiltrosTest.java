package VitaFortis.demo.v1.service;

import VitaFortis.demo.v1.dto.RelatorioFiltroDto;
import VitaFortis.demo.v1.entity.*;
import VitaFortis.demo.v1.enums.*;
import VitaFortis.demo.v1.repository.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RelatoriosFiltrosTest {
    final PedidoRepository pedidos=mock(PedidoRepository.class);
    final ProdutoRepository produtos=mock(ProdutoRepository.class);
    final UsuarioRepository usuarios=mock(UsuarioRepository.class);
    final CupomRepository cupons=mock(CupomRepository.class);
    final MovimentoCashbackRepository movimentos=mock(MovimentoCashbackRepository.class);
    final RelatorioService service=new RelatorioService(pedidos,produtos,usuarios,cupons,movimentos);
    final LocalDate dia=LocalDate.of(2026,9,14);
    final RelatorioFiltroDto periodo=RelatorioFiltroDto.periodo(dia,dia);

    Usuario usuario(long id,TipoUsuario tipo) { var u=new Usuario();u.setId(id);u.setTipo(tipo);u.setNome("Pessoa "+id);u.setEmail("p"+id+"@teste.invalid");return u; }
    Pedido pedido(long id,StatusPagamento status) {var p=new Pedido();p.setId(id);p.setUsuario(usuario(1,TipoUsuario.CLIENTE));p.setDataPedido(dia.atTime(12,0));p.setStatusPagamento(status);p.setStatus(StatusCompra.PENDENTE);p.setFormaRecebimento(FormaRecebimento.ENTREGA);p.setFormaPagamento("PIX");p.setTotal(new BigDecimal("100"));p.setItems(List.of());return p;}
    String csv(TipoRelatorio tipo,RelatorioFiltroDto filtro){return new String(service.gerar(tipo,filtro),StandardCharsets.UTF_8).replace("\uFEFF","");}
    void periodoPedidos(List<Pedido> lista){when(pedidos.findByDataPedidoBetween(dia.atStartOfDay(),dia.atTime(LocalTime.MAX))).thenReturn(lista);}

    @Test void vendasECuponsIgnoramPendentesEEstornados() {
        var cupom=new Cupom();cupom.setId(8L);cupom.setCodigo("COLAB");cupom.setTipo(CupomTipo.PERCENTUAL);cupom.setDesconto(BigDecimal.TEN);cupom.setColaborador(usuario(5,TipoUsuario.COLABORADOR));
        var aprovado=pedido(1,StatusPagamento.APROVADO);var pendente=pedido(2,StatusPagamento.PENDENTE);var estornado=pedido(3,StatusPagamento.ESTORNADO);
        for(var p:List.of(aprovado,pendente,estornado))p.setCupomUtilizado(cupom);
        periodoPedidos(List.of(aprovado,pendente,estornado));when(cupons.findAll()).thenReturn(List.of(cupom));
        assertEquals(2,CsvDados.ler(csv(TipoRelatorio.VENDAS,periodo),';').size());
        assertEquals(4,CsvDados.ler(csv(TipoRelatorio.PEDIDOS,periodo),';').size());
        var rows=CsvDados.ler(csv(TipoRelatorio.CUPONS,periodo),';');assertEquals("1",rows.get(1).get(6));assertEquals("100",rows.get(1).get(7));
        var outro=new RelatorioFiltroDto(dia,dia,null,99L,null,null,null,null,null,null);
        assertEquals(1,CsvDados.ler(csv(TipoRelatorio.CUPONS,outro),';').size());
    }
    @Test void filtrosDePedidoEDeCadastroSelecionamRegistros() {
        periodoPedidos(List.of(pedido(1,StatusPagamento.APROVADO)));
        var filtro=new RelatorioFiltroDto(dia,dia,"inexistente",null,null,null,null,FormaRecebimento.ENTREGA,"PIX",null);
        assertEquals(1,CsvDados.ler(csv(TipoRelatorio.PEDIDOS,filtro),';').size());
        when(usuarios.findAll()).thenReturn(List.of(usuario(1,TipoUsuario.CLIENTE),usuario(2,TipoUsuario.ADMIN),usuario(3,TipoUsuario.COLABORADOR)));
        assertEquals(2,CsvDados.ler(csv(TipoRelatorio.CLIENTES,periodo),';').size());
        var baixo=new Produto();baixo.setCodigo("BAIXO");baixo.setNome("Baixo");baixo.setCategoria("PROTEINAS");baixo.setQuantidadeEstoque(2);
        var alto=new Produto();alto.setCodigo("ALTO");alto.setNome("Alto");alto.setCategoria("PROTEINAS");alto.setQuantidadeEstoque(20);
        when(produtos.findAll()).thenReturn(List.of(baixo,alto));
        var estoque=new RelatorioFiltroDto(dia,dia,null,null,true,"PROTEINAS",null,null,null,5);
        assertEquals(2,CsvDados.ler(csv(TipoRelatorio.PRODUTOS,estoque),';').size());
    }
    @Test void cashbackRespeitaPeriodoEColaborador() {
        var lista=new ArrayList<MovimentoCashback>();
        for(int i=0;i<3;i++){var m=new MovimentoCashback();m.setColaborador(usuario(i==2?6:5,TipoUsuario.COLABORADOR));m.setCriadoEm(i==1?dia.plusDays(1).atStartOfDay():dia.atStartOfDay());m.setTipo(TipoMovimentoCashback.CREDITO);m.setValor(BigDecimal.TEN);m.setSaldoAnterior(BigDecimal.ZERO);m.setSaldoNovo(BigDecimal.TEN);lista.add(m);}
        when(movimentos.findAll()).thenReturn(lista);
        var filtro=new RelatorioFiltroDto(dia,dia,null,5L,null,null,null,null,null,null);
        assertEquals(2,CsvDados.ler(csv(TipoRelatorio.CASHBACK,filtro),';').size());
        assertThrows(IllegalArgumentException.class,()->service.gerar(TipoRelatorio.CASHBACK,RelatorioFiltroDto.periodo(dia,dia.minusDays(1))));
    }
    @Test void metricasUsamPeriodoFechadoEPagamentoAprovado() {
        when(usuarios.findByEmail("admin")).thenReturn(Optional.of(usuario(9,TipoUsuario.ADMIN)));
        periodoPedidos(List.of(pedido(1,StatusPagamento.APROVADO),pedido(2,StatusPagamento.PENDENTE),pedido(3,StatusPagamento.ESTORNADO)));
        var baixo=new Produto();baixo.setId(1L);baixo.setQuantidadeEstoque(0);baixo.setNome("Sem estoque");
        var alto=new Produto();alto.setId(2L);alto.setQuantidadeEstoque(6);
        when(produtos.findAll()).thenReturn(List.of(alto,baixo));
        var result=new MetricasService(pedidos,usuarios,produtos).obter(dia,dia,"admin");
        assertEquals(0,new BigDecimal("100").compareTo(result.faturamento()));assertEquals(1,result.pedidos());assertEquals(1,result.estoqueBaixo().size());
        verify(pedidos).findByDataPedidoBetween(dia.atStartOfDay(),dia.atTime(LocalTime.MAX));
        verify(pedidos).findByDataPedidoBetween(dia.minusDays(1).atStartOfDay(),dia.minusDays(1).atTime(LocalTime.MAX));
    }
}
