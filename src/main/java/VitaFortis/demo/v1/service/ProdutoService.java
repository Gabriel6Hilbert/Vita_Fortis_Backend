package VitaFortis.demo.v1.service;

import VitaFortis.demo.v1.Filtro.FiltroProduto;
import VitaFortis.demo.v1.dto.ProdutoFiltroDto;
import VitaFortis.demo.v1.dto.ProdutoRequestDto;
import VitaFortis.demo.v1.dto.ProdutoResponseDto;
import VitaFortis.demo.v1.dto.ProdutoMetadadosComerciaisDto;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import VitaFortis.demo.v1.dto.CategoriaResumoDto;

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
        String codigo = dto.getCodigo().trim().toUpperCase();
        if (produtoRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new IllegalArgumentException("Codigo de produto ja cadastrado");
        }
        Produto entity = produtoMapper.toEntity(dto);
        entity.setCodigo(codigo);

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

        String codigo = dto.getCodigo().trim().toUpperCase();
        produtoRepository.findAll().stream()
                .filter(outro -> outro.getCodigo() != null && outro.getCodigo().equalsIgnoreCase(codigo))
                .filter(outro -> !outro.getId().equals(produtoId))
                .findFirst()
                .ifPresent(outro -> { throw new IllegalArgumentException("Codigo de produto ja cadastrado"); });

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

        if (entity.isAtivo() && entity.getQuantidadeEstoque() == 0) {
            throw new IllegalArgumentException("Não é possível ativar um produto sem estoque.");
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

        int ok = produtoRepository.debitarEstoque(produtoId, qtd);
        if (ok == 0) throw new IllegalArgumentException("Estoque insuficiente");

        Produto p = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new IllegalArgumentException("Produto nao encontrado"));
       return produtoMapper.toResponseDto(produtoRepository.save(p));
    }

    @Transactional
    public ProdutoResponseDto reporEstoque (Long produtoId, int qtd) {
        if (qtd <= 0) throw new IllegalArgumentException("Quantidade deve ser > 0");

        int ok = produtoRepository.creditarEstoque(produtoId, qtd);
        if (ok == 0) throw new IllegalArgumentException("Produto nao encontrado");

        Produto p = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new IllegalArgumentException("Produto nao encontrado"));


        return produtoMapper.toResponseDto(produtoRepository.save(p));
    }

    //BUSCAS PAGINADAS
    @Transactional(readOnly = true)
    public Page<ProdutoResponseDto> listarPublicos (ProdutoFiltroDto filtro) {
        int page = filtro.getPagina() == null ? 0 :Math.max(0, filtro.getPagina());
        int tamanho = filtro.getTamanho() == null ? 12 : Math.max(1, filtro.getTamanho());

        Specification<Produto> spec = FiltroProduto.buildSpec(filtro);
        Sort sort = FiltroProduto.buildSort(filtro);

        Pageable pageable = PageRequest.of(page, tamanho, sort);

        return produtoRepository.findAll(spec, pageable)
                .map(produtoMapper::toResponseDto);
    }

    //getById(ADMIN)
    public ProdutoResponseDto getByIdAdmin(Long produtoId) {
        Produto p = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new IllegalArgumentException("Produto nao encontrado"));
        return produtoMapper.toResponseDto(p);
    }

    @Transactional(readOnly = true)
    public ProdutoResponseDto getByIdPublico(Long produtoId) {
        Produto p = produtoRepository.findByIdAndAtivoTrue(produtoId)
                .orElseThrow(() -> new IllegalArgumentException("Produto nao encontrado ou indisponivel"));
        return produtoMapper.toResponseDto(p);
    }

    @Transactional(readOnly = true)
    public List<CategoriaResumoDto> listarCategorias() {
        Map<VitaFortis.demo.v1.enums.CategoriaProduto, Long> totais = produtoRepository.findAll().stream()
                .filter(Produto::isAtivo)
                .collect(Collectors.groupingBy(Produto::getCategoria, Collectors.counting()));
        return totais.entrySet().stream()
                .map(item -> new CategoriaResumoDto(item.getKey(), item.getValue()))
                .sorted(java.util.Comparator.comparing(item -> item.categoria().name()))
                .toList();
    }

    //APLICAR DESCONTO EM UM PRODUTO VALOR OU PORCENTUAL
    @Transactional
    public ProdutoResponseDto aplicarDescontoPercentual (Long produtoId, BigDecimal percentual) {
        if (percentual == null) throw new IllegalArgumentException("Percentual deve ser informado");
        if (percentual.compareTo(BigDecimal.ZERO) < 0 || percentual.compareTo(new BigDecimal("100")) > 0 ) throw new IllegalArgumentException("Percentual deve ser informado");

        Produto p = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new IllegalArgumentException("Produto nao encontrado"));

        p.setDescontoValor(null);
        p.setDescontoPercentual(percentual.setScale(2, RoundingMode.HALF_UP));

        return produtoMapper.toResponseDto(produtoRepository.save(p));
    }

    @Transactional
    public ProdutoResponseDto aplicarDescontoValor (Long produtoId, BigDecimal valor) {
        if (valor == null) throw new IllegalArgumentException("Valor deve ser informado");
        if (valor.compareTo(BigDecimal.ZERO) < 0)  throw new IllegalArgumentException("Valor deve ser maior > 0");

        Produto p = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new IllegalArgumentException("Produto nao encontrado"));

        BigDecimal v = valor.setScale(2, RoundingMode.HALF_UP);
        if(v.compareTo(p.getPreco()) > 0) throw new IllegalArgumentException("Desconto nao pode ser maior que o Preço");

        p.setDescontoPercentual(null);
        p.setDescontoValor(v);

        return produtoMapper.toResponseDto(produtoRepository.save(p));
    }

    @Transactional
    public ProdutoResponseDto removerDesconto (Long produtoId) {
        Produto p = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new IllegalArgumentException("Produto nao encontrado"));

        p.setDescontoValor(null);
        p.setDescontoPercentual(null);
        return produtoMapper.toResponseDto(produtoRepository.save(p));
    }

    @Transactional
    public ProdutoResponseDto atualizarMetadados(Long id, ProdutoMetadadosComerciaisDto dto) {
        Produto p = produtoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Produto nao encontrado"));
        p.setObjetivos(normalizar(dto.getObjetivos())); p.setEsportes(normalizar(dto.getEsportes()));
        p.setVegano(dto.isVegano()); p.setVegetariano(dto.isVegetariano());
        p.setLinhaClinica(dto.isLinhaClinica()); p.setLancamento(dto.isLancamento());
        p.setSubcategoria(dto.getSubcategoria() == null ? null : dto.getSubcategoria().trim().toUpperCase());
        p.setAvaliacaoMedia(dto.getAvaliacaoMedia());
        return produtoMapper.toResponseDto(produtoRepository.save(p));
    }

    private java.util.Set<String> normalizar(java.util.Set<String> valores) {
        if (valores == null) return new java.util.HashSet<>();
        return valores.stream().filter(java.util.Objects::nonNull).map(String::trim).filter(v -> !v.isEmpty())
                .map(String::toUpperCase).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new));
    }









}
