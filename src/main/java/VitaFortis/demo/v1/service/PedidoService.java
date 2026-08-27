package VitaFortis.demo.v1.service;

import VitaFortis.demo.v1.dto.*;
import VitaFortis.demo.v1.entity.*;
import VitaFortis.demo.v1.enums.CupomTipo;
import VitaFortis.demo.v1.enums.StatusCompra;
import VitaFortis.demo.v1.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;

@Service
public class PedidoService {
    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProdutoRepository produtoRepository;
    private final CupomRepository cupomRepository;

    public PedidoService(PedidoRepository pedidoRepository, UsuarioRepository usuarioRepository,
                         ProdutoRepository produtoRepository, CupomRepository cupomRepository) {
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.produtoRepository = produtoRepository;
        this.cupomRepository = cupomRepository;
    }

    @Transactional
    public PedidoResponseDto criar(PedidoRequestDto dto) {
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado"));
        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setStatus(StatusCompra.PENDENTE);
        pedido.setEnderecoEntrega(dto.getEnderecoEntrega()); pedido.setFormaPagamento(dto.getFormaPagamento());
        pedido.setReferenciaPagamento(dto.getReferenciaPagamento());
        List<ItemCompra> itens = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        for (ItemCompraRequestDto itemDto : dto.getItens()) {
            Produto produto = produtoRepository.findByIdAndAtivoTrue(itemDto.getProdutoId())
                    .orElseThrow(() -> new IllegalArgumentException("Produto indisponivel: " + itemDto.getProdutoId()));
            if (produtoRepository.debitarEstoque(produto.getId(), itemDto.getQuantidade()) == 0) {
                throw new IllegalArgumentException("Estoque insuficiente para " + produto.getNome());
            }
            BigDecimal preco = produto.getPrecoFinal();
            ItemCompra item = new ItemCompra();
            item.setPedido(pedido);
            item.setProduto(produto);
            item.setQuantidade(itemDto.getQuantidade());
            item.setPrecoUnitario(preco);
            item.setSubtotal(preco.multiply(BigDecimal.valueOf(itemDto.getQuantidade())).setScale(2, RoundingMode.HALF_UP));
            subtotal = subtotal.add(item.getSubtotal());
            itens.add(item);
        }
        Cupom cupom = null;
        BigDecimal total = subtotal;
        if (dto.getCupomId() != null) {
            cupom = cupomRepository.findById(dto.getCupomId())
                    .orElseThrow(() -> new IllegalArgumentException("Cupom nao encontrado"));
            validarCupom(cupom, subtotal);
            BigDecimal desconto = cupom.getTipo() == CupomTipo.PERCENTUAL
                    ? subtotal.multiply(cupom.getDesconto()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP)
                    : cupom.getDesconto();
            total = subtotal.subtract(desconto.min(subtotal));
        }
        pedido.setItems(itens);
        pedido.setCupomUtilizado(cupom);
        pedido.setTotal(total.setScale(2, RoundingMode.HALF_UP));
        adicionarHistorico(pedido, StatusCompra.PENDENTE, "Pedido criado");
        return toDto(pedidoRepository.save(pedido));
    }

    @Transactional
    public PedidoResponseDto alterarStatus(Long id, StatusCompra novoStatus) {
        Pedido pedido = buscar(id);
        StatusCompra anterior = pedido.getStatus();
        if (anterior == StatusCompra.CANCELADO || anterior == StatusCompra.ENTREGUE) {
            throw new IllegalArgumentException("Pedido finalizado nao pode mudar de status");
        }
        if (novoStatus == StatusCompra.PAGAMENTO_APROVADO && pedido.getPontosGerados() == 0) {
            int pontos = pedido.getTotal().setScale(0, RoundingMode.FLOOR).intValue();
            pedido.setPontosGerados(pontos);
            pedido.getUsuario().setPontosFidelidade(pedido.getUsuario().getPontosFidelidade() + pontos);
            pedido.getItems().forEach(item -> produtoRepository.alterarTotalVendido(item.getProduto().getId(), item.getQuantidade()));
        }
        if (novoStatus == StatusCompra.CANCELADO) {
            pedido.getItems().forEach(item -> produtoRepository.creditarEstoque(item.getProduto().getId(), item.getQuantidade()));
            if (pedido.getPontosGerados() > 0) {
                pedido.getItems().forEach(item -> produtoRepository.alterarTotalVendido(item.getProduto().getId(), -item.getQuantidade()));
                pedido.getUsuario().setPontosFidelidade(Math.max(0,
                        pedido.getUsuario().getPontosFidelidade() - pedido.getPontosGerados()));
                pedido.setPontosGerados(0);
            }
        }
        pedido.setStatus(novoStatus);
        adicionarHistorico(pedido, novoStatus, null);
        return toDto(pedidoRepository.save(pedido));
    }

    @Transactional(readOnly = true)
    public List<PedidoResponseDto> listarUsuario(Long usuarioId) {
        return pedidoRepository.findAllByUsuarioIdOrderByDataPedidoDesc(usuarioId).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<PedidoResponseDto> listarTodos() {
        return pedidoRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly=true)
    public PedidoResponseDto acompanhar(Long id, String email) {
        Pedido p=buscar(id); Usuario atual=usuarioRepository.findByEmail(email).orElseThrow(()->new AccessDeniedException("Usuario nao autenticado"));
        if(!p.getUsuario().getId().equals(atual.getId()) && atual.getTipo()==VitaFortis.demo.v1.enums.TipoUsuario.CLIENTE) throw new AccessDeniedException("Acesso negado");
        return toDto(p);
    }

    private void adicionarHistorico(Pedido p, StatusCompra status, String observacao){HistoricoStatusPedido h=new HistoricoStatusPedido();h.setPedido(p);h.setStatus(status);h.setObservacao(observacao);p.getHistoricoStatus().add(h);}

    private Pedido buscar(Long id) {
        return pedidoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Pedido nao encontrado"));
    }

    private void validarCupom(Cupom cupom, BigDecimal subtotal) {
        if (!cupom.isAtivo()) throw new IllegalArgumentException("Cupom inativo");
        if (cupom.getDataVencimento() != null && cupom.getDataVencimento().isBefore(java.time.LocalDateTime.now()))
            throw new IllegalArgumentException("Cupom expirado");
        if (cupom.getMinSubtotal() != null && subtotal.compareTo(cupom.getMinSubtotal()) < 0)
            throw new IllegalArgumentException("Subtotal minimo do cupom nao atingido");
    }

    private PedidoResponseDto toDto(Pedido pedido) {
        PedidoResponseDto dto = new PedidoResponseDto();
        dto.setId(pedido.getId());
        dto.setUsuarioId(pedido.getUsuario().getId());
        dto.setDataPedido(pedido.getDataPedido());
        dto.setTotal(pedido.getTotal().doubleValue());
        dto.setStatus(pedido.getStatus().name());
        dto.setPontosGerados(pedido.getPontosGerados());
        dto.setCupomCodigo(pedido.getCupomUtilizado() == null ? null : pedido.getCupomUtilizado().getCodigo());
        dto.setEnderecoMascarado(mascararEndereco(pedido.getEnderecoEntrega())); dto.setFormaPagamento(pedido.getFormaPagamento());
        dto.setReferenciaPagamentoMascarada(mascararReferencia(pedido.getReferenciaPagamento()));
        dto.setHistoricoStatus(pedido.getHistoricoStatus().stream().map(h->new HistoricoStatusPedidoDto(h.getStatus().name(),h.getObservacao(),h.getData())).toList());
        dto.setItens(pedido.getItems().stream().map(item -> {
            ItemCompraResponseDto i = new ItemCompraResponseDto();
            i.setId(item.getItemId()); i.setProdutoId(item.getProduto().getId());
            i.setProdutoNome(item.getProduto().getNome()); i.setQuantidade(item.getQuantidade());
            i.setPrecoUnitario(item.getPrecoUnitario()); i.setSubtotal(item.getSubtotal()); return i;
        }).toList());
        return dto;
    }
    private String mascararEndereco(String v){if(v==null||v.isBlank())return null;int i=v.indexOf(',');return i<0?"Endereco cadastrado":v.substring(0,i)+", ***";}
    private String mascararReferencia(String v){if(v==null||v.isBlank())return null;return "***"+v.substring(Math.max(0,v.length()-4));}
}
