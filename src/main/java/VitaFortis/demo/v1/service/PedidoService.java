package VitaFortis.demo.v1.service;

import VitaFortis.demo.v1.dto.*;
import VitaFortis.demo.v1.entity.*;
import VitaFortis.demo.v1.enums.*;
import VitaFortis.demo.v1.integration.PagamentoGateway;
import VitaFortis.demo.v1.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Service
public class PedidoService {
    private static final BigDecimal CEM = new BigDecimal("100");

    private final PedidoRepository pedidos;
    private final UsuarioRepository usuarios;
    private final ProdutoRepository produtos;
    private final CupomRepository cupons;
    private final EnderecoService enderecos;
    private final FreteService fretes;
    private final PagamentoGateway pagamentos;
    private final CashbackService cashback;

    public PedidoService(PedidoRepository pedidos, UsuarioRepository usuarios, ProdutoRepository produtos,
                         CupomRepository cupons, EnderecoService enderecos, FreteService fretes,
                         PagamentoGateway pagamentos, CashbackService cashback) {
        this.pedidos = pedidos;
        this.usuarios = usuarios;
        this.produtos = produtos;
        this.cupons = cupons;
        this.enderecos = enderecos;
        this.fretes = fretes;
        this.pagamentos = pagamentos;
        this.cashback = cashback;
    }

    @Transactional
    public PedidoResponseDto criar(PedidoRequestDto dto, String emailAutenticado) {
        Usuario usuario = usuarioAutenticado(emailAutenticado);
        if (!usuario.getId().equals(dto.getUsuarioId())) {
            throw new AccessDeniedException("Nao e permitido criar pedido para outro usuario");
        }
        Pedido pedido = novoPedido(usuario, dto);
        BigDecimal subtotal = adicionarItens(pedido, dto.getItens());
        Cupom cupom = obterCupom(dto.getCupomId(), subtotal);
        BigDecimal desconto = calcularDesconto(cupom, subtotal);
        aplicarRecebimento(pedido, dto);

        pedido.setCupomUtilizado(cupom);
        pedido.setSubtotal(subtotal);
        pedido.setDesconto(desconto);
        pedido.setTotal(subtotal.subtract(desconto).add(pedido.getFrete()).setScale(2, RoundingMode.HALF_UP));
        adicionarHistorico(pedido, StatusCompra.PENDENTE, "Pedido criado e aguardando pagamento");

        Pedido salvo = pedidos.save(pedido);
        PagamentoGateway.PagamentoPendente pagamento = pagamentos.iniciar(
                salvo.getId(), salvo.getTotal(), salvo.getFormaPagamento());
        salvo.setReferenciaPagamento(pagamento.referencia());
        return toDto(pedidos.save(salvo));
    }

    @Transactional
    public PedidoResponseDto alterarStatus(Long id, StatusCompra novoStatus, String observacao) {
        Pedido pedido = buscar(id);
        validarTransicao(pedido.getStatus(), novoStatus);
        if (novoStatus == StatusCompra.PAGAMENTO_APROVADO) {
            if (pedido.getStatusPagamento() == StatusPagamento.APROVADO) {
                throw new IllegalArgumentException("Pagamento do pedido ja foi aprovado");
            }
            pedido.setStatusPagamento(StatusPagamento.APROVADO);
            pedido.getItems().forEach(item ->
                    produtos.alterarTotalVendido(item.getProduto().getId(), item.getQuantidade()));
            cashback.creditarPedido(pedido);
        }
        if (novoStatus == StatusCompra.CANCELADO) {
            cancelar(pedido);
        }
        pedido.setStatus(novoStatus);
        adicionarHistorico(pedido, novoStatus, observacao);
        return toDto(pedidos.save(pedido));
    }

    @Transactional
    public PedidoResponseDto confirmarPagamento(Long id, boolean aprovado, String observacao) {
        Pedido pedido = buscar(id);
        if (pedido.getStatusPagamento() != StatusPagamento.PENDENTE || pedido.getStatus() != StatusCompra.PENDENTE) {
            throw new IllegalArgumentException("Pagamento deste pedido ja foi processado");
        }
        String texto = observacao == null || observacao.isBlank()
                ? (aprovado ? "Pagamento simulado aprovado pelo administrador" : "Pagamento simulado recusado pelo administrador")
                : observacao.trim();
        return alterarStatus(id, aprovado ? StatusCompra.PAGAMENTO_APROVADO : StatusCompra.CANCELADO, texto);
    }

    @Transactional(readOnly = true)
    public List<PedidoResponseDto> listarUsuario(Long usuarioId, String emailAutenticado) {
        Usuario usuario = usuarioAutenticado(emailAutenticado);
        if (!usuario.getId().equals(usuarioId) && usuario.getTipo() != TipoUsuario.ADMIN) {
            throw new AccessDeniedException("Acesso negado aos pedidos");
        }
        return pedidos.findAllByUsuarioIdOrderByDataPedidoDesc(usuarioId).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<PedidoResponseDto> listarTodos() {
        return pedidos.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponseDto acompanhar(Long id, String emailAutenticado) {
        Pedido pedido = buscar(id);
        Usuario atual = usuarioAutenticado(emailAutenticado);
        if (!pedido.getUsuario().getId().equals(atual.getId()) && atual.getTipo() != TipoUsuario.ADMIN) {
            throw new AccessDeniedException("Acesso negado ao pedido");
        }
        return toDto(pedido);
    }

    private Pedido novoPedido(Usuario usuario, PedidoRequestDto dto) {
        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setStatus(StatusCompra.PENDENTE);
        pedido.setStatusPagamento(StatusPagamento.PENDENTE);
        pedido.setFormaPagamento(dto.getFormaPagamento().trim().toUpperCase());
        pedido.setItems(new ArrayList<>());
        pedido.setHistoricoStatus(new LinkedHashSet<>());
        return pedido;
    }

    private BigDecimal adicionarItens(Pedido pedido, List<ItemCompraRequestDto> solicitados) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (ItemCompraRequestDto solicitado : solicitados) {
            Produto produto = produtos.findByIdAndAtivoTrue(solicitado.getProdutoId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Produto indisponivel: " + solicitado.getProdutoId()));
            if (produtos.debitarEstoque(produto.getId(), solicitado.getQuantidade()) == 0) {
                throw new IllegalArgumentException("Estoque insuficiente para " + produto.getNome());
            }
            ItemCompra item = criarItem(pedido, produto, solicitado.getQuantidade());
            pedido.getItems().add(item);
            subtotal = subtotal.add(item.getSubtotal());
        }
        return subtotal.setScale(2, RoundingMode.HALF_UP);
    }

    private ItemCompra criarItem(Pedido pedido, Produto produto, int quantidade) {
        BigDecimal preco = produto.getPrecoFinal();
        ItemCompra item = new ItemCompra();
        item.setPedido(pedido);
        item.setProduto(produto);
        item.setQuantidade(quantidade);
        item.setPrecoUnitario(preco);
        item.setSubtotal(preco.multiply(BigDecimal.valueOf(quantidade)).setScale(2, RoundingMode.HALF_UP));
        return item;
    }

    private void aplicarRecebimento(Pedido pedido, PedidoRequestDto dto) {
        pedido.setFormaRecebimento(dto.getFormaRecebimento());
        if (dto.getFormaRecebimento() == FormaRecebimento.RETIRADA) {
            pedido.setFrete(BigDecimal.ZERO.setScale(2));
            return;
        }
        if (dto.getEnderecoId() == null) {
            throw new IllegalArgumentException("Endereco obrigatorio para entrega");
        }
        Endereco endereco = enderecos.buscarParaCheckout(dto.getUsuarioId(), dto.getEnderecoId());
        FreteService.CotacaoFrete cotacao = fretes.calcular(endereco);
        pedido.setEnderecoEntrega(endereco.formatado());
        pedido.setPrazoEntregaDias(cotacao.prazoDias());
        pedido.setFrete(cotacao.valor());
    }

    private Cupom obterCupom(Long cupomId, BigDecimal subtotal) {
        if (cupomId == null) return null;
        Cupom cupom = cupons.findById(cupomId)
                .orElseThrow(() -> new IllegalArgumentException("Cupom nao encontrado"));
        validarCupom(cupom, subtotal);
        return cupom;
    }

    private BigDecimal calcularDesconto(Cupom cupom, BigDecimal subtotal) {
        if (cupom == null) return BigDecimal.ZERO.setScale(2);
        BigDecimal desconto = cupom.getTipo() == CupomTipo.PERCENTUAL
                ? subtotal.multiply(cupom.getDesconto()).divide(CEM, 2, RoundingMode.HALF_UP)
                : cupom.getDesconto();
        return desconto.min(subtotal).setScale(2, RoundingMode.HALF_UP);
    }

    private void cancelar(Pedido pedido) {
        boolean pagamentoAprovado = pedido.getStatusPagamento() == StatusPagamento.APROVADO;
        pedido.getItems().forEach(item -> {
            produtos.creditarEstoque(item.getProduto().getId(), item.getQuantidade());
            if (pagamentoAprovado) {
                produtos.alterarTotalVendido(item.getProduto().getId(), -item.getQuantidade());
            }
        });
        if (pagamentoAprovado) cashback.estornarPedido(pedido);
        pedido.setStatusPagamento(pedido.getStatusPagamento() == StatusPagamento.APROVADO
                ? StatusPagamento.ESTORNADO : StatusPagamento.RECUSADO);
    }

    private void validarTransicao(StatusCompra atual, StatusCompra novoStatus) {
        if (atual == StatusCompra.CANCELADO || atual == StatusCompra.ENTREGUE) {
            throw new IllegalArgumentException("Pedido finalizado nao pode mudar de status");
        }
        if (atual == novoStatus) throw new IllegalArgumentException("Pedido ja esta com o status informado");
        if (atual == StatusCompra.PENDENTE && novoStatus != StatusCompra.PAGAMENTO_APROVADO && novoStatus != StatusCompra.CANCELADO) {
            throw new IllegalArgumentException("Confirme o pagamento antes de iniciar a separacao");
        }
        if (atual == StatusCompra.PAGAMENTO_APROVADO && novoStatus != StatusCompra.EM_SEPARACAO && novoStatus != StatusCompra.CANCELADO) {
            throw new IllegalArgumentException("Pedido aprovado deve seguir para separacao");
        }
        if (atual == StatusCompra.EM_SEPARACAO && novoStatus != StatusCompra.ENVIADO && novoStatus != StatusCompra.CANCELADO) {
            throw new IllegalArgumentException("Pedido em separacao deve seguir para envio");
        }
        if (atual == StatusCompra.ENVIADO && novoStatus != StatusCompra.ENTREGUE && novoStatus != StatusCompra.CANCELADO) {
            throw new IllegalArgumentException("Pedido enviado deve seguir para entrega");
        }
    }

    private void validarCupom(Cupom cupom, BigDecimal subtotal) {
        if (!cupom.isAtivo()) throw new IllegalArgumentException("Cupom inativo");
        if (cupom.getDataVencimento() != null && cupom.getDataVencimento().isBefore(java.time.LocalDateTime.now())) {
            throw new IllegalArgumentException("Cupom expirado");
        }
        if (cupom.getMinSubtotal() != null && subtotal.compareTo(cupom.getMinSubtotal()) < 0) {
            throw new IllegalArgumentException("Subtotal minimo do cupom nao atingido");
        }
    }

    private Usuario usuarioAutenticado(String email) {
        return usuarios.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new AccessDeniedException("Usuario nao autenticado"));
    }

    private Pedido buscar(Long id) {
        return pedidos.buscarComCupomEColaborador(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido nao encontrado"));
    }

    private void adicionarHistorico(Pedido pedido, StatusCompra status, String observacao) {
        HistoricoStatusPedido historico = new HistoricoStatusPedido();
        historico.setPedido(pedido);
        historico.setStatus(status);
        historico.setObservacao(observacao);
        pedido.getHistoricoStatus().add(historico);
    }

    public PedidoResponseDto toDto(Pedido pedido) {
        PedidoResponseDto dto = new PedidoResponseDto();
        dto.setId(pedido.getId());
        dto.setUsuarioId(pedido.getUsuario().getId());
        dto.setDataPedido(pedido.getDataPedido());
        dto.setSubtotal(pedido.getSubtotal());
        dto.setDesconto(pedido.getDesconto());
        dto.setFrete(pedido.getFrete());
        dto.setTotal(pedido.getTotal());
        dto.setStatus(pedido.getStatus().name());
        dto.setCupomCodigo(pedido.getCupomUtilizado() == null ? null : pedido.getCupomUtilizado().getCodigo());
        dto.setFormaRecebimento(pedido.getFormaRecebimento().name());
        dto.setEnderecoMascarado(mascararEndereco(pedido.getEnderecoEntrega()));
        dto.setPrazoEntregaDias(pedido.getPrazoEntregaDias());
        dto.setFormaPagamento(pedido.getFormaPagamento());
        dto.setStatusPagamento(pedido.getStatusPagamento().name());
        dto.setReferenciaPagamentoMascarada(mascararReferencia(pedido.getReferenciaPagamento()));
        dto.setHistoricoStatus(pedido.getHistoricoStatus().stream().map(historico ->
                new HistoricoStatusPedidoDto(historico.getStatus().name(), historico.getObservacao(),
                        historico.getData())).toList());
        dto.setItens(pedido.getItems().stream().map(this::toItemDto).toList());
        return dto;
    }

    private ItemCompraResponseDto toItemDto(ItemCompra item) {
        ItemCompraResponseDto dto = new ItemCompraResponseDto();
        dto.setId(item.getItemId());
        dto.setProdutoId(item.getProduto().getId());
        dto.setProdutoNome(item.getProduto().getNome());
        dto.setQuantidade(item.getQuantidade());
        dto.setPrecoUnitario(item.getPrecoUnitario());
        dto.setSubtotal(item.getSubtotal());
        return dto;
    }

    private String mascararEndereco(String endereco) {
        if (endereco == null || endereco.isBlank()) return null;
        int separador = endereco.indexOf(',');
        return separador < 0 ? "Endereco cadastrado" : endereco.substring(0, separador) + ", ***";
    }

    private String mascararReferencia(String referencia) {
        if (referencia == null || referencia.isBlank()) return null;
        return "***" + referencia.substring(Math.max(0, referencia.length() - 4));
    }
}
