package VitaFortis.demo.v1.service;

import VitaFortis.demo.v1.dto.CashbackSaldoDto;
import VitaFortis.demo.v1.dto.ColaboradorResumoDto;
import VitaFortis.demo.v1.entity.Cupom;
import VitaFortis.demo.v1.entity.MovimentoCashback;
import VitaFortis.demo.v1.entity.Pedido;
import VitaFortis.demo.v1.entity.Usuario;
import VitaFortis.demo.v1.enums.TipoMovimentoCashback;
import VitaFortis.demo.v1.enums.TipoUsuario;
import VitaFortis.demo.v1.repository.MovimentoCashbackRepository;
import VitaFortis.demo.v1.repository.UsuarioRepository;
import VitaFortis.demo.v1.repository.CupomRepository;
import VitaFortis.demo.v1.repository.PedidoRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class CashbackService {
    private static final BigDecimal CEM = new BigDecimal("100");

    private final MovimentoCashbackRepository movimentos;
    private final UsuarioRepository usuarios;
    private final CupomRepository cupons;
    private final PedidoRepository pedidos;
    private final PedidoServiceHolder pedidoServiceHolder;

    public CashbackService(MovimentoCashbackRepository movimentos, UsuarioRepository usuarios,
                           CupomRepository cupons, PedidoRepository pedidos, PedidoServiceHolder pedidoServiceHolder) {
        this.movimentos = movimentos;
        this.usuarios = usuarios;
        this.cupons = cupons;
        this.pedidos = pedidos;
        this.pedidoServiceHolder = pedidoServiceHolder;
    }

    @Transactional
    public void creditarPedido(Pedido pedido) {
        Cupom cupom = pedido.getCupomUtilizado();
        if (cupom == null || cupom.getColaborador() == null || cupom.getPercentualCashback() == null) return;
        if (movimentos.existsByPedidoIdAndTipo(pedido.getId(), TipoMovimentoCashback.CREDITO)) return;
        BigDecimal baseElegivel = pedido.getSubtotal().subtract(pedido.getDesconto()).max(BigDecimal.ZERO);
        BigDecimal valor = baseElegivel.multiply(cupom.getPercentualCashback())
                .divide(CEM, 2, RoundingMode.HALF_UP);
        registrar(cupom.getColaborador(), pedido, TipoMovimentoCashback.CREDITO, valor,
                "Cashback do pedido confirmado", "SISTEMA");
    }

    @Transactional
    public void estornarPedido(Pedido pedido) {
        Cupom cupom = pedido.getCupomUtilizado();
        if (cupom == null || cupom.getColaborador() == null) return;
        if (!movimentos.existsByPedidoIdAndTipo(pedido.getId(), TipoMovimentoCashback.CREDITO)
                || movimentos.existsByPedidoIdAndTipo(pedido.getId(), TipoMovimentoCashback.ESTORNO)) return;
        BigDecimal base = pedido.getSubtotal().subtract(pedido.getDesconto()).max(BigDecimal.ZERO);
        BigDecimal valor = base.multiply(cupom.getPercentualCashback()).divide(CEM, 2, RoundingMode.HALF_UP).negate();
        registrar(cupom.getColaborador(), pedido, TipoMovimentoCashback.ESTORNO, valor,
                "Estorno por cancelamento do pedido", "SISTEMA");
    }

    @Transactional
    public CashbackSaldoDto ajustar(Long colaboradorId, BigDecimal novoSaldo, String justificativa, String responsavel) {
        Usuario colaborador = colaborador(colaboradorId);
        BigDecimal anterior = saldo(colaborador);
        registrar(colaborador, null, TipoMovimentoCashback.AJUSTE, novoSaldo.subtract(anterior), justificativa, responsavel);
        return new CashbackSaldoDto(colaboradorId, saldo(colaborador));
    }

    @Transactional
    public CashbackSaldoDto baixar(Long colaboradorId, BigDecimal valor, String justificativa, String responsavel) {
        Usuario colaborador = colaborador(colaboradorId);
        if (saldo(colaborador).compareTo(valor) < 0) throw new IllegalArgumentException("Saldo de cashback insuficiente");
        registrar(colaborador, null, TipoMovimentoCashback.BAIXA, valor.negate(), justificativa, responsavel);
        return new CashbackSaldoDto(colaboradorId, saldo(colaborador));
    }

    @Transactional(readOnly = true)
    public CashbackSaldoDto meuSaldo(String email) {
        Usuario colaborador = usuarios.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new AccessDeniedException("Usuario nao autenticado"));
        if (colaborador.getTipo() != TipoUsuario.COLABORADOR) throw new AccessDeniedException("Acesso exclusivo de colaborador");
        return new CashbackSaldoDto(colaborador.getId(), saldo(colaborador));
    }

    @Transactional(readOnly = true)
    public ColaboradorResumoDto meuResumo(String email) {
        Usuario colaborador = usuarios.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new AccessDeniedException("Usuario nao autenticado"));
        if (colaborador.getTipo() != TipoUsuario.COLABORADOR) throw new AccessDeniedException("Acesso exclusivo de colaborador");
        var listaPedidos = pedidos.findAllByCupomUtilizadoColaboradorIdOrderByDataPedidoDesc(colaborador.getId());
        var listaMovimentos = movimentos.findAllByColaboradorIdOrderByCriadoEmDesc(colaborador.getId());
        BigDecimal vendas = listaPedidos.stream().filter(p -> p.getStatusPagamento() == VitaFortis.demo.v1.enums.StatusPagamento.APROVADO)
                .map(Pedido::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal confirmado = listaMovimentos.stream().filter(m -> m.getTipo() == TipoMovimentoCashback.CREDITO)
                .map(MovimentoCashback::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal estornado = listaMovimentos.stream().filter(m -> m.getTipo() == TipoMovimentoCashback.ESTORNO)
                .map(MovimentoCashback::getValor).reduce(BigDecimal.ZERO, BigDecimal::add).abs();
        var desempenho = cupons.findAllByColaboradorIdOrderByCodigoAsc(colaborador.getId()).stream().map(c -> {
            var doCupom = listaPedidos.stream().filter(p -> p.getCupomUtilizado() != null && p.getCupomUtilizado().getId().equals(c.getId())).toList();
            BigDecimal total = doCupom.stream().filter(p -> p.getStatusPagamento() == VitaFortis.demo.v1.enums.StatusPagamento.APROVADO)
                    .map(Pedido::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal cashback = doCupom.stream().filter(p -> p.getStatusPagamento() == VitaFortis.demo.v1.enums.StatusPagamento.APROVADO)
                    .map(p -> p.getSubtotal().subtract(p.getDesconto()).multiply(c.getPercentualCashback()).divide(CEM, 2, RoundingMode.HALF_UP))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return new ColaboradorResumoDto.CupomDesempenhoDto(c.getId(), c.getCodigo(), c.isAtivo(), c.getPercentualCashback(), doCupom.size(), total, cashback);
        }).toList();
        var extrato = listaMovimentos.stream().map(m -> new ColaboradorResumoDto.MovimentoDto(m.getId(), m.getTipo().name(), m.getValor(),
                m.getSaldoAnterior(), m.getSaldoNovo(), m.getJustificativa(), m.getPedido() == null ? null : m.getPedido().getId(), m.getCriadoEm())).toList();
        return new ColaboradorResumoDto(colaborador.getId(), saldo(colaborador), confirmado, estornado, vendas,
                listaPedidos.size(), desempenho, extrato, listaPedidos.stream().map(pedidoServiceHolder::toDto).toList());
    }

    /** Quebra o ciclo de dependencias entre PedidoService e CashbackService. */
    @org.springframework.stereotype.Component
    public static class PedidoServiceHolder {
        private PedidoService pedidoService;
        @org.springframework.context.annotation.Lazy
        public PedidoServiceHolder(PedidoService pedidoService) { this.pedidoService = pedidoService; }
        public VitaFortis.demo.v1.dto.PedidoResponseDto toDto(Pedido pedido) { return pedidoService.toDto(pedido); }
    }

    private void registrar(Usuario colaborador, Pedido pedido, TipoMovimentoCashback tipo, BigDecimal valor,
                           String justificativa, String responsavel) {
        BigDecimal anterior = saldo(colaborador);
        BigDecimal novo = anterior.add(valor).setScale(2, RoundingMode.HALF_UP);
        if (novo.signum() < 0) throw new IllegalArgumentException("Movimento deixaria o cashback negativo");
        colaborador.setSaldoCashback(novo);
        usuarios.save(colaborador);
        MovimentoCashback movimento = new MovimentoCashback();
        movimento.setColaborador(colaborador);
        movimento.setPedido(pedido);
        movimento.setTipo(tipo);
        movimento.setValor(valor.setScale(2, RoundingMode.HALF_UP));
        movimento.setSaldoAnterior(anterior);
        movimento.setSaldoNovo(novo);
        movimento.setJustificativa(justificativa.trim());
        movimento.setResponsavel(responsavel);
        movimentos.save(movimento);
    }

    private Usuario colaborador(Long id) {
        Usuario usuario = usuarios.findById(id).orElseThrow(() -> new IllegalArgumentException("Colaborador nao encontrado"));
        if (usuario.getTipo() != TipoUsuario.COLABORADOR) throw new IllegalArgumentException("Usuario nao e colaborador");
        return usuario;
    }

    private BigDecimal saldo(Usuario usuario) {
        return usuario.getSaldoCashback() == null ? BigDecimal.ZERO.setScale(2) : usuario.getSaldoCashback();
    }
}
