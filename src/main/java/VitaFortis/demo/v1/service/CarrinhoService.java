package VitaFortis.demo.v1.service;

import VitaFortis.demo.v1.dto.Carrinho.CarrinhoItemRequestDto;
import VitaFortis.demo.v1.dto.Carrinho.CarrinhoRequestDto;
import VitaFortis.demo.v1.dto.Carrinho.CarrinhoResponseDto;
import VitaFortis.demo.v1.dto.CupomRequestDto;
import VitaFortis.demo.v1.entity.*;
import VitaFortis.demo.v1.enums.CupomTipo;
import VitaFortis.demo.v1.mapper.CarrinhoItemMapper;
import VitaFortis.demo.v1.mapper.CarrinhoMapper;
import VitaFortis.demo.v1.repository.*;
import org.hibernate.validator.internal.constraintvalidators.bv.time.futureorpresent.FutureOrPresentValidatorForCalendar;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class CarrinhoService {

    private final CarrinhoRepository carrinhoRepository;
    private final CarrinhoItemRepository  carrinhoItemRepository;
    private final ProdutoRepository produtoRepository;
    private final CarrinhoMapper carrinhoMapper;
    private final CarrinhoItemMapper carrinhoItemMapper;
    private final UsuarioRepository usuarioRepository;
    private final CupomRepository cupomRepository;
    private final FutureOrPresentValidatorForCalendar futureOrPresentValidatorForCalendar;

    public CarrinhoService(CarrinhoRepository carrinhoRepository,
                           CarrinhoItemRepository itemRepository,
                           ProdutoRepository produtoRepository,
                           CarrinhoMapper carrinhoMapper,
                           CarrinhoItemMapper itemMapper, UsuarioRepository usuarioRepository, CupomRepository cupomRepository, FutureOrPresentValidatorForCalendar futureOrPresentValidatorForCalendar) {
        this.carrinhoRepository = carrinhoRepository;
        this.carrinhoItemRepository = itemRepository;
        this.produtoRepository = produtoRepository;
        this.carrinhoMapper = carrinhoMapper;
        this.carrinhoItemMapper = itemMapper;
        this.usuarioRepository = usuarioRepository;
        this.cupomRepository = cupomRepository;
        this.futureOrPresentValidatorForCalendar = futureOrPresentValidatorForCalendar;
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
    public CarrinhoResponseDto adcionarItem(Long usuarioId, CarrinhoItemRequestDto dto) {
       if (dto.getQuantidade() <= 0) {
       throw new IllegalArgumentException("Quantidade deve ser maior que 0");
       }

       Carrinho carrinho = getOrCreateAtivoEntity(usuarioId);
       Produto produto = produtoRepository.findById(dto.getProdutoId())
               .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado"));

       if (!produto.isAtivo()) {
           throw new IllegalArgumentException("Produto indisponivel");
       }

       CarrinhoItem item = carrinhoItemRepository
               .findByCarrinhoIdAndProdutoId(carrinho.getId(), produto.getId())
               .orElseGet(() -> {
                   CarrinhoItem carrinhoItem = new CarrinhoItem();
                   carrinhoItem.setCarrinho(carrinho);
                   carrinhoItem.setProduto(produto);
                   carrinhoItem.setPrecoUnitario(produto.getPreco());
                   carrinhoItem.setQuantidade(0);
                   return carrinhoItem;
               });

       int totalDesejado = item.getQuantidade() + dto.getQuantidade();
       if(produto.getQuantidadeEstoque() < totalDesejado) {
           throw new IllegalArgumentException("Estoque insuficiente" + produto.getQuantidadeEstoque());
       }

       item.setQuantidade(totalDesejado);
       carrinhoItemRepository.save(item);

       recalcularTotais(carrinho);
       carrinhoRepository.save(carrinho);

       return carrinhoMapper.toResponseDto(carrinho);
    }

    //OBTER CARRINHO
    @Transactional
    public CarrinhoResponseDto obterCarrinho (Long usuarioId) {
        Carrinho carrinho = carrinhoRepository.findByUsuarioIdAndAtivoTrue(usuarioId)
                .orElseGet(() -> getOrCreateAtivoEntity(usuarioId));
        return carrinhoMapper.toResponseDto(carrinho);
    }

    //CASO O CARRINHO EXISTA ELE PEGA O QUE JA TEM COM OS PRODUTOS
    @Transactional(readOnly = true)
    public CarrinhoResponseDto obterCarrinhoSeExistir(Long usuarioId) {
        return carrinhoRepository.findByUsuarioIdAndAtivoTrue(usuarioId)
                .map(carrinhoMapper::toResponseDto)
                .orElse(null);
    }

    //LIMPA TODO O CARRINHO
    @Transactional
    public CarrinhoResponseDto limparCarrinho (Long usuarioId) {
        Carrinho carrinho = getOrCreateAtivoEntity(usuarioId);

        carrinhoItemRepository.deleteAllByCarrinhoId(carrinho.getId());

        carrinho.setSubtotal(BigDecimal.ZERO);
        carrinho.setDescontos(BigDecimal.ZERO);
        carrinho.setTotal(BigDecimal.ZERO);

        carrinhoRepository.save(carrinho);
        return carrinhoMapper.toResponseDto(carrinho);
    }

    //ATUALIZA QUANTIDADE DOS ITENS DO CARRINHO
    @Transactional
    public CarrinhoResponseDto atualizarQuantidade(Long usuarioId, CarrinhoItemRequestDto dto) {
        Carrinho carrinho = getOrCreateAtivoEntity(usuarioId);

        CarrinhoItem item = carrinhoItemRepository.findByIdAndCarrinhoId(dto.getItemId(), carrinho.getId())
                .orElseThrow(() -> new IllegalArgumentException("Item não pertence ao carrinho."));

        if (!item.getCarrinho().getId().equals(carrinho.getId())) {
            throw new IllegalArgumentException("Item nao pertence ao carrinho");
        }

        Integer qtd = dto.getQuantidade();
        if (qtd <= 0 || qtd <= 0) {
            carrinhoItemRepository.delete(item);
        } else {
            Produto produto = item.getProduto();
            Integer estoque  = produto.getQuantidadeEstoque();
            if (estoque  <= 0 || estoque  == null) {
                throw new IllegalArgumentException("Estoque insuficiente");
            }
            item.setQuantidade(qtd);
            carrinhoItemRepository.save(item);
        }
        recalcularTotais(carrinho);
        carrinhoRepository.save(carrinho);
        return carrinhoMapper.toResponseDto(carrinho);
    }

    //REMOVE ITEM
    public CarrinhoResponseDto removeItem (Long usuarioId, Long itemId, int delta) {
        if (delta <= 0) {
            throw new IllegalArgumentException("Quantidade insuficiente");
        }

        Carrinho carrinho = getOrCreateAtivoEntity(usuarioId);

        CarrinhoItem item = carrinhoItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item nao pertence ao carrinho"));

        int novaQtd = item.getQuantidade() - delta;
        if (novaQtd <= 0) {
            carrinhoItemRepository.delete(item);
        } else {
            item.setQuantidade(novaQtd);
            carrinhoItemRepository.save(item);
        }

        recalcularTotais(carrinho);
        carrinhoRepository.save(carrinho);

        return carrinhoMapper.toResponseDto(carrinho);
    }

    //CUPOM
    public CarrinhoResponseDto aplicarCupom (Long usuarioId, String codigo) {
        Carrinho carrinho = getOrCreateAtivoEntity(usuarioId);

        BigDecimal subtotal = calcularSubtotal(carrinho);

        Cupom cupom = cupomRepository.findByCodigoIgnoreCase(codigo)
                .orElseThrow(() -> new IllegalArgumentException("Cupom invalido"));

        validarCupom(cupom, subtotal);

        BigDecimal desconto = calcularDesconto(cupom, subtotal);

        carrinho.setCupomCodigo(cupom.getCodigo());

        recalcularTotais(carrinho);
        carrinhoRepository.save(carrinho);
        return carrinhoMapper.toResponseDto(carrinho);
    }

    //REMOVE CUPOM
    @Transactional
    public CarrinhoResponseDto removerCupom(Long usuarioId) {
        Carrinho carrinho = getOrCreateAtivoEntity(usuarioId);
        BigDecimal subtotal = calcularSubtotal(carrinho);

        carrinho.setCupomCodigo(null);
        carrinho.setDescontos(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));

        recalcularTotais(carrinho);
        carrinhoRepository.save(carrinho);
        return carrinhoMapper.toResponseDto(carrinho);
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
        if (!cupom.isAtivo()) {
            throw new IllegalArgumentException("Cupom inativo.");
        }
        LocalDateTime agora = LocalDateTime.now();
        if (cupom.getDataCadastro() != null && agora.isBefore(cupom.getDataCadastro())) {
            throw new IllegalArgumentException("Cupom ainda não está válido.");
        }
        if (cupom.getDataVencimento() != null && agora.isAfter(cupom.getDataVencimento())) {
            throw new IllegalArgumentException("Cupom expirado.");
        }
        if (cupom.getMinSubtotal() != null && subtotalAtual.compareTo(cupom.getMinSubtotal()) < 0) {
            throw new IllegalArgumentException("Subtotal mínimo não atingido.");
        }
    }

}
