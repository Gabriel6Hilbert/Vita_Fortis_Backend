package VitaFortis.demo.v1.service;

import VitaFortis.demo.v1.dto.Carrinho.CarrinhoItemRequestDto;
import VitaFortis.demo.v1.dto.Carrinho.CarrinhoRequestDto;
import VitaFortis.demo.v1.dto.Carrinho.CarrinhoResponseDto;
import VitaFortis.demo.v1.dto.CupomRequestDto;
import VitaFortis.demo.v1.dto.AlteracaoPrecoDto;
import VitaFortis.demo.v1.entity.*;
import VitaFortis.demo.v1.enums.CupomTipo;
import VitaFortis.demo.v1.mapper.CarrinhoItemMapper;
import VitaFortis.demo.v1.mapper.CarrinhoMapper;
import VitaFortis.demo.v1.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CarrinhoService {
    private final CupomRegrasService regrasCupom;

    private final CarrinhoRepository carrinhoRepository;
    private final CarrinhoItemRepository  carrinhoItemRepository;
    private final ProdutoRepository produtoRepository;
    private final CarrinhoMapper carrinhoMapper;
    private final CarrinhoItemMapper carrinhoItemMapper;
    private final UsuarioRepository usuarioRepository;
    private final CupomRepository cupomRepository;

    public CarrinhoService(CarrinhoRepository carrinhoRepository,
                           CarrinhoItemRepository itemRepository,
                           ProdutoRepository produtoRepository,
                           CarrinhoMapper carrinhoMapper,
                           CarrinhoItemMapper itemMapper, UsuarioRepository usuarioRepository, CupomRepository cupomRepository, CupomRegrasService regrasCupom) {
        this.regrasCupom = regrasCupom;
        this.carrinhoRepository = carrinhoRepository;
        this.carrinhoItemRepository = itemRepository;
        this.produtoRepository = produtoRepository;
        this.carrinhoMapper = carrinhoMapper;
        this.carrinhoItemMapper = itemMapper;
        this.usuarioRepository = usuarioRepository;
        this.cupomRepository = cupomRepository;
    }


    //OBTER CARRINHO PELO ID DO USUARIO
    @Transactional
    protected Carrinho getOrCreateAtivoEntity(Long usuarioId) {
        return carrinhoRepository.findByUsuarioIdAndAtivoTrue(usuarioId)
                .orElseGet(() -> {
                    Carrinho novo = new Carrinho();
                    novo.setUsuario(usuarioRepository.getReferenceById(usuarioId));
                    novo.setAtivo(true);
                    novo.setSubtotal(BigDecimal.ZERO);
                    novo.setDescontos(BigDecimal.ZERO);
                    novo.setTotal(BigDecimal.ZERO);

                    try {
                        return carrinhoRepository.save(novo);
                    } catch (org.springframework.dao.DataIntegrityViolationException e) {
                        return carrinhoRepository.findByUsuarioIdAndAtivoTrue(usuarioId)
                                .orElseThrow(() -> e);
                    }
                });
    }

    //RECALCULO DOS TOTAIS
    protected void recalcularTotais(Carrinho carrinho) {
        BigDecimal subtotal = carrinhoItemRepository.findAllByCarrinhoId(carrinho.getId()).stream()
                .map(i -> i.getPrecoUnitario().multiply(BigDecimal.valueOf(i.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        carrinho.setSubtotal(subtotal);

        // cupom
        BigDecimal descontos = BigDecimal.ZERO;
        String codigo = carrinho.getCupomCodigo();
        if (codigo != null) {
            cupomRepository.findByCodigoIgnoreCase(codigo).ifPresent(c -> {
                try {
                    validarCupom(c, subtotal);
                    BigDecimal d = calcularDesconto(c, subtotal).setScale(2, RoundingMode.HALF_UP);
                    carrinho.setDescontos(d);
                } catch (IllegalArgumentException ex) {
                    // cupom inválido
                    carrinho.setCupomCodigo(null);
                    carrinho.setDescontos(BigDecimal.ZERO);
                }
            });
            descontos = carrinho.getDescontos() == null ? BigDecimal.ZERO : carrinho.getDescontos();
        } else {
            carrinho.setDescontos(BigDecimal.ZERO);
        }

        BigDecimal total = subtotal.subtract(descontos);
        if (total.signum() < 0) total = BigDecimal.ZERO;
        carrinho.setTotal(total.setScale(2, RoundingMode.HALF_UP));
    }

    //ADCIONAR ITEM NO CARRINHO
    @Transactional
    public CarrinhoResponseDto adcionarItem(Long usuarioId, CarrinhoItemRequestDto dto, String emailAutenticado) {
       validarProprietario(usuarioId, emailAutenticado);
       if (dto.getQuantidade() <= 0) {
       throw new IllegalArgumentException("Quantidade deve ser maior que 0");
       }

       Carrinho carrinho = getOrCreateAtivoEntity(usuarioId);
       Produto produto = produtoRepository.findById(dto.getProdutoId())
               .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado"));

       if (!produto.isAtivo()) {
           throw new IllegalArgumentException("Produto indisponivel");
       }

       var itemExistente = carrinhoItemRepository.findByCarrinhoIdAndProdutoId(carrinho.getId(), produto.getId());
       int totalDesejado = itemExistente.map(CarrinhoItem::getQuantidade).orElse(0) + dto.getQuantidade();
       if(produto.getQuantidadeEstoque() < totalDesejado) {
           throw new IllegalArgumentException("Estoque insuficiente. Disponível: " + produto.getQuantidadeEstoque());
       }

       CarrinhoItem item = itemExistente.orElseGet(() -> {
           CarrinhoItem novoItem = new CarrinhoItem();
           novoItem.setProduto(produto);
           novoItem.setPrecoUnitario(produto.getPrecoFinal());
           carrinho.addItem(novoItem);
           return novoItem;
       });
       item.setQuantidade(totalDesejado);
       carrinhoItemRepository.save(item);

       recalcularTotais(carrinho);
       carrinhoRepository.save(carrinho);

       return resposta(carrinho);
    }

    //OBTER CARRINHO
    @Transactional
    public CarrinhoResponseDto obterCarrinho (Long usuarioId, String emailAutenticado) {
        validarProprietario(usuarioId, emailAutenticado);
        Carrinho carrinho = carrinhoRepository.findAtivoParaAtualizacao(usuarioId)
                .orElseGet(() -> getOrCreateAtivoEntity(usuarioId));
        return resposta(carrinho);
    }

    //CASO O CARRINHO EXISTA ELE PEGA O QUE JA TEM COM OS PRODUTOS
    @Transactional
    public CarrinhoResponseDto obterCarrinhoSeExistir(Long usuarioId) {
        return carrinhoRepository.findByUsuarioIdAndAtivoTrue(usuarioId)
                .map(this::resposta)
                .orElse(null);
    }

    //LIMPA TODO O CARRINHO
    @Transactional
    public CarrinhoResponseDto limparCarrinho (Long usuarioId, String emailAutenticado) {
        validarProprietario(usuarioId, emailAutenticado);
        Carrinho carrinho = getOrCreateAtivoEntity(usuarioId);

        carrinho.getItens().clear();

        carrinho.setSubtotal(BigDecimal.ZERO);
        carrinho.setDescontos(BigDecimal.ZERO);
        carrinho.setTotal(BigDecimal.ZERO);

        carrinhoRepository.saveAndFlush(carrinho);
        return resposta(carrinho);
    }

    //ATUALIZA QUANTIDADE DOS ITENS DO CARRINHO
    @Transactional
    public CarrinhoResponseDto atualizarQuantidade(Long usuarioId, CarrinhoItemRequestDto dto, String emailAutenticado) {
        validarProprietario(usuarioId, emailAutenticado);
        Carrinho carrinho = getOrCreateAtivoEntity(usuarioId);

        CarrinhoItem item = carrinhoItemRepository.findByItemIdAndCarrinhoId(dto.getItemId(), carrinho.getId())
                .orElseThrow(() -> new IllegalArgumentException("Item não pertence ao carrinho."));

        if (!item.getCarrinho().getId().equals(carrinho.getId())) {
            throw new IllegalArgumentException("Item nao pertence ao carrinho");
        }

        Integer qtd = dto.getQuantidade();
        if (qtd <= 0) {
            carrinho.removeItem(item);
        } else {
            Produto produto = item.getProduto();
            Integer estoque  = produto.getQuantidadeEstoque();
            if (estoque == null || estoque < qtd) {
                throw new IllegalArgumentException("Estoque insuficiente");
            }
            item.setQuantidade(qtd);
            carrinhoItemRepository.save(item);
        }
        recalcularTotais(carrinho);
        carrinhoRepository.save(carrinho);
        return resposta(carrinho);
    }

    //REMOVE ITEM
    @Transactional
    public CarrinhoResponseDto removeItem (Long usuarioId, Long itemId, int delta, String emailAutenticado) {
        validarProprietario(usuarioId, emailAutenticado);
        if (delta <= 0) {
            throw new IllegalArgumentException("Quantidade insuficiente");
        }

        Carrinho carrinho = getOrCreateAtivoEntity(usuarioId);

        CarrinhoItem item = carrinhoItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item nao pertence ao carrinho"));

        if (!item.getCarrinho().getId().equals(carrinho.getId())) {
            throw new IllegalArgumentException("Item nao pertence ao carrinho");
        }

        int novaQtd = item.getQuantidade() - delta;
        if (novaQtd <= 0) {
            carrinho.removeItem(item);
        } else {
            item.setQuantidade(novaQtd);
            carrinhoItemRepository.save(item);
        }

        recalcularTotais(carrinho);
        carrinhoRepository.save(carrinho);

        return resposta(carrinho);
    }

    //CUPOM
    @Transactional
    public CarrinhoResponseDto aplicarCupom (Long usuarioId, String codigo, String emailAutenticado) {
        validarProprietario(usuarioId, emailAutenticado);
        Carrinho carrinho = getOrCreateAtivoEntity(usuarioId);

        BigDecimal subtotal = calcularSubtotal(carrinho);

        Cupom cupom = cupomRepository.findByCodigoIgnoreCase(codigo)
                .orElseThrow(() -> new IllegalArgumentException("Cupom invalido"));

        validarCupom(cupom, subtotal);

        carrinho.setCupomCodigo(cupom.getCodigo());

        recalcularTotais(carrinho);
        carrinhoRepository.save(carrinho);
        return resposta(carrinho);
    }

    //REMOVE CUPOM
    @Transactional
    public CarrinhoResponseDto removerCupom(Long usuarioId, String emailAutenticado) {
        validarProprietario(usuarioId, emailAutenticado);
        Carrinho carrinho = getOrCreateAtivoEntity(usuarioId);
        carrinho.setCupomCodigo(null);
        carrinho.setDescontos(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));

        recalcularTotais(carrinho);
        carrinhoRepository.save(carrinho);
        return resposta(carrinho);
    }

    public ResultadoReconciliacao reconciliarParaPedido(Long usuarioId, String emailAutenticado) {
        validarProprietario(usuarioId, emailAutenticado);
        Carrinho carrinho = carrinhoRepository.findAtivoParaAtualizacao(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("A sacola está vazia."));
        List<AlteracaoPrecoDto> alteracoes = reconciliarPrecos(carrinho);
        recalcularTotais(carrinho);
        carrinhoRepository.saveAndFlush(carrinho);
        return new ResultadoReconciliacao(carrinho, alteracoes);
    }

    public void esvaziarAposPedido(Carrinho carrinho) {
        carrinho.getItens().clear();
        carrinho.setCupomCodigo(null);
        carrinho.setSubtotal(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        carrinho.setDescontos(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        carrinho.setTotal(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        carrinhoRepository.saveAndFlush(carrinho);
    }

    private CarrinhoResponseDto resposta(Carrinho carrinho) {
        carrinho.getItens().removeIf(item -> !item.getProduto().isAtivo());
        List<AlteracaoPrecoDto> alteracoes = reconciliarPrecos(carrinho);
        recalcularTotais(carrinho);
        carrinhoRepository.saveAndFlush(carrinho);
        CarrinhoResponseDto dto = carrinhoMapper.toResponseDto(carrinho);
        dto.setPrecoAlterado(!alteracoes.isEmpty());
        dto.setAlteracoesPreco(alteracoes);
        if (carrinho.getCupomCodigo() != null) {
            cupomRepository.findByCodigoIgnoreCase(carrinho.getCupomCodigo()).ifPresent(cupom -> {
                dto.setCupomId(cupom.getId());
                if (cupom.getColaborador() != null && cupom.getPercentualCashback() != null) {
                    dto.setColaboradorNome(cupom.getColaborador().getNome());
                    dto.setCashbackColaborador(CashbackService.calcularSobreSubtotal(
                            carrinho.getSubtotal(), cupom.getPercentualCashback()));
                }
            });
        }
        return dto;
    }

    private List<AlteracaoPrecoDto> reconciliarPrecos(Carrinho carrinho) {
        List<AlteracaoPrecoDto> alteracoes = new ArrayList<>();
        for (CarrinhoItem item : carrinho.getItens()) {
            Produto produto = produtoRepository.findAtivoParaAtualizacao(item.getProduto().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Produto indisponível: " + item.getProduto().getNome()));
            BigDecimal anterior = item.getPrecoUnitario().setScale(2, RoundingMode.HALF_UP);
            BigDecimal atual = produto.getPrecoFinal().setScale(2, RoundingMode.HALF_UP);
            if (anterior.compareTo(atual) != 0) {
                alteracoes.add(new AlteracaoPrecoDto(produto.getId(), produto.getNome(), anterior, atual));
                item.setPrecoUnitario(atual);
                carrinhoItemRepository.save(item);
            }
        }
        if (!alteracoes.isEmpty()) carrinho.setAtualizadoEm(LocalDateTime.now());
        return alteracoes;
    }

    public record ResultadoReconciliacao(Carrinho carrinho, List<AlteracaoPrecoDto> alteracoesPreco) {}

    private Usuario validarProprietario(Long usuarioId, String emailAutenticado) {
        Usuario atual = usuarioRepository.findByEmail(emailAutenticado.trim().toLowerCase())
                .orElseThrow(() -> new AccessDeniedException("Usuario nao autenticado"));
        if (!atual.getId().equals(usuarioId)) throw new AccessDeniedException("Acesso negado ao carrinho");
        return atual;
    }


    /* ===== helpers CUPOM ===== */
    private BigDecimal calcularSubtotal(Carrinho carrinho) {
        return carrinhoItemRepository.findAllByCarrinhoId(carrinho.getId()).stream()
                .map(i -> i.getPrecoUnitario().multiply(BigDecimal.valueOf(i.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }


    private BigDecimal calcularDesconto(Cupom cupom, BigDecimal subtotal) {
        BigDecimal desconto;
        if (cupom.getTipo() == CupomTipo.PERCENTUAL) {
            BigDecimal perc = cupom.getDesconto().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            desconto = subtotal.multiply(perc);
        } else {
            desconto = cupom.getDesconto();
        }
        return desconto.min(subtotal).setScale(2, RoundingMode.HALF_UP);
    }

    private void validarCupom(Cupom cupom, BigDecimal subtotalAtual) {
        regrasCupom.validar(cupom, subtotalAtual);
    }

}
