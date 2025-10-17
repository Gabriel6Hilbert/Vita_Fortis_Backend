package VitaFortis.demo.v1.service;

import VitaFortis.demo.v1.dto.Carrinho.CarrinhoItemRequestDto;
import VitaFortis.demo.v1.dto.Carrinho.CarrinhoRequestDto;
import VitaFortis.demo.v1.dto.Carrinho.CarrinhoResponseDto;
import VitaFortis.demo.v1.entity.Carrinho;
import VitaFortis.demo.v1.entity.CarrinhoItem;
import VitaFortis.demo.v1.entity.Produto;
import VitaFortis.demo.v1.entity.Usuario;
import VitaFortis.demo.v1.mapper.CarrinhoItemMapper;
import VitaFortis.demo.v1.mapper.CarrinhoMapper;
import VitaFortis.demo.v1.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class CarrinhoService {

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
                           CarrinhoItemMapper itemMapper, UsuarioRepository usuarioRepository, CupomRepository cupomRepository) {
        this.carrinhoRepository = carrinhoRepository;
        this.carrinhoItemRepository = itemRepository;
        this.produtoRepository = produtoRepository;
        this.carrinhoMapper = carrinhoMapper;
        this.carrinhoItemMapper = itemMapper;
        this.usuarioRepository = usuarioRepository;
        this.cupomRepository = cupomRepository;
    }

    @Transactional
    protected Carrinho getOrCreateAtivoEntity(Long usuarioId) {
        return carrinhoRepository.findyByUsuarioAtivo(usuarioId)
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
                        return carrinhoRepository.findyByUsuarioAtivo(usuarioId)
                                .orElseThrow(() -> e);
                    }
                });
    }

    protected void recalcularTotais(Carrinho carrinho) {
        BigDecimal subtotal = carrinhoItemRepository.findAllByCarrinhoId(carrinho.getId()).stream()
                .map(i -> i.getPrecoUnitario().multiply(BigDecimal.valueOf(i.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        carrinho.setSubtotal(subtotal);
        if (carrinho.getDescontos() == null) carrinho.setDescontos(BigDecimal.ZERO);

        BigDecimal total = subtotal.subtract(carrinho.getDescontos());
        if (total.compareTo(BigDecimal.ZERO) < 0) total = BigDecimal.ZERO;
        carrinho.setTotal(total);
    }

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



}
