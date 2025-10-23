package VitaFortis.demo.v1.service;

import VitaFortis.demo.v1.Filtro.FiltroProduto;
import VitaFortis.demo.v1.dto.ProdutoFiltroDto;
import VitaFortis.demo.v1.dto.ProdutoRequestDto;
import VitaFortis.demo.v1.dto.ProdutoResponseDto;
import VitaFortis.demo.v1.entity.Produto;
import VitaFortis.demo.v1.mapper.ProdutoMapper;
import VitaFortis.demo.v1.repository.ProdutoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final ProdutoMapper produtoMapper;

    public ProdutoService(ProdutoRepository produtoRepository, ProdutoMapper produtoMapper) {
        this.produtoRepository = produtoRepository;
        this.produtoMapper = produtoMapper;
    }

    // CRIAR PRODUTO
    @Transactional
    public ProdutoResponseDto create (ProdutoRequestDto dto) {
        Produto entity = produtoMapper.toEntity(dto);

        if (entity.getNome() == null || entity.getNome().isEmpty()) throw new IllegalArgumentException("Nome é obrigatorio");
        if (entity.getQuantidadeEstoque() == null) entity.setQuantidadeEstoque(0);
        if (entity.getPreco() == null) throw new IllegalArgumentException("Preço obrigatorio");
        if (entity.getPreco().signum() < 0) throw new IllegalArgumentException("Preço invalido");
        if (entity.getQuantidadeEstoque() < 0) throw new IllegalArgumentException("Estoque invalido");
        if (entity.getCategoria() == null) throw new IllegalArgumentException("Categoria obrigatorio");
        if (entity.isResgatavel()) {
            if (entity.getPontosNecessarios() == null || entity.getPontosNecessarios() < 0) {
            throw new IllegalArgumentException("Pontos necessários devem ser informados e >= 0 para produto resgatável.");
            }
        } else {
            entity.setPontosNecessarios(null);
        }

        Produto saved = produtoRepository.save(entity);
        return produtoMapper.toResponseDto(saved);
    }

    // MODIFICAR PRODUTO
    public ProdutoResponseDto update (Long produtoId, ProdutoRequestDto dto) {
        Produto entity = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new IllegalArgumentException("Produto nao encontrado"));

        produtoMapper.updateFromDto(dto, entity);

        if (entity.getPreco() == null || entity.getPreco().signum() < 0) throw new IllegalArgumentException("Preço invalido");
        if (entity.getQuantidadeEstoque() == null || entity.getQuantidadeEstoque() < 0) throw new IllegalArgumentException("Estoque invalido");
        if (entity.getCategoria() == null) throw new IllegalArgumentException("Categoria obrigatorio");
        if (entity.isResgatavel()) {
            if (entity.getPontosNecessarios() == null || entity.getPontosNecessarios() < 0) {
                throw new IllegalArgumentException("Pontos necessarios devem ser informados e >= 0 para produto.");
            }
        }else {
            entity.setPontosNecessarios(null);
        }
        Produto updated = produtoRepository.save(entity);
        return produtoMapper.toResponseDto(updated);
    }

    //ESTOQUE
    @Transactional
    public void setAtivo (Long produtoId, Boolean ativo) {
        Produto p = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new IllegalArgumentException("Produto nao encontrado"));
        p.setAtivo(ativo);
        produtoRepository.save(p);
    }

    @Transactional
    public ProdutoResponseDto baixarEstoque(Long produtoId, int qtd) {
        if (qtd <= 0) throw new IllegalArgumentException("Quantidade deve ser > 0");
        Produto p = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado"));

        int novo = p.getQuantidadeEstoque() - qtd;

        if (novo == 0) {
            p.setAtivo(false);
        }

        Produto saved =  produtoRepository.save(p);
        return produtoMapper.toResponseDto(saved);
    }

    @Transactional
    public ProdutoResponseDto reporEstoque (Long produtoId, int qtd) {
        if (qtd <= 0) throw new IllegalArgumentException("Quantidade deve ser > 0");
        Produto p = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new IllegalArgumentException("Produto nao encontrado"));
        p.setQuantidadeEstoque(p.getQuantidadeEstoque() + qtd);
        return produtoMapper.toResponseDto(produtoRepository.save(p));
    }


    //BUSCAS PAGINADAS
    @Transactional(readOnly = true)
    public Page<ProdutoResponseDto> listarPublicos (ProdutoFiltroDto filtro) {
        int page = filtro.getPagina() == null ? 0 :Math.max(0, filtro.getPagina());
        int tamanho = filtro.getTamanho() == null ? 12 : Math.max(1, filtro.getTamanho());

        Specification<Produto> spec = FiltroProduto.buildSpec(filtro);
        Sort sort = FiltroProduto.buildSort(filtro.getOrdenacao());

        Pageable pageable = PageRequest.of(page, tamanho, sort);

        return produtoRepository.findAll(spec, pageable)
                .map(produtoMapper::toResponseDto);
    }



}
