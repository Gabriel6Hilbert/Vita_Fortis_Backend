package VitaFortis.demo.v1.service;

import VitaFortis.demo.v1.Filtro.FiltroProduto;
import VitaFortis.demo.v1.dto.ProdutoFiltroDto;
import VitaFortis.demo.v1.dto.ProdutoRequestDto;
import VitaFortis.demo.v1.dto.ProdutoResponseDto;
import VitaFortis.demo.v1.dto.ProdutoMetadadosComerciaisDto;
import VitaFortis.demo.v1.entity.Produto;
import VitaFortis.demo.v1.mapper.ProdutoMapper;
import VitaFortis.demo.v1.repository.ProdutoRepository;
import VitaFortis.demo.v1.repository.MovimentacaoEstoqueRepository;
import VitaFortis.demo.v1.entity.MovimentacaoEstoque;
import VitaFortis.demo.v1.dto.MovimentacaoEstoqueDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.PageImpl;
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
import VitaFortis.demo.v1.dto.MarcaResumoDto;
import VitaFortis.demo.v1.dto.ObjetivoResumoDto;
import VitaFortis.demo.v1.enums.ObjetivoProduto;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final ProdutoMapper produtoMapper;
    private final MovimentacaoEstoqueRepository movimentacoes;

    public ProdutoService(ProdutoRepository produtoRepository, ProdutoMapper produtoMapper, MovimentacaoEstoqueRepository movimentacoes) {
        this.produtoRepository = produtoRepository;
        this.produtoMapper = produtoMapper;
        this.movimentacoes = movimentacoes;
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

        Produto updated = produtoRepository.save(entity);
        return produtoMapper.toResponseDto(updated);
    }

    //ESTOQUE
    @Transactional
    public void setAtivo (Long produtoId, Boolean ativo) {
        Produto p = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new IllegalArgumentException("Produto nao encontrado"));
        if (Boolean.TRUE.equals(ativo) && p.getPreco() == null) {
            throw new IllegalArgumentException("Informe o preço antes de ativar o produto.");
        }
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

    @Transactional
    public ProdutoResponseDto ajustarEstoque(Long produtoId, int quantidadeNova, String motivo, String responsavel) {
        if (quantidadeNova < 0) throw new IllegalArgumentException("Estoque nao pode ser negativo");
        if (motivo == null || motivo.trim().length() < 3) throw new IllegalArgumentException("Informe o motivo do ajuste");
        Produto produto = produtoRepository.findById(produtoId).orElseThrow(() -> new IllegalArgumentException("Produto nao encontrado"));
        int anterior = produto.getQuantidadeEstoque();
        if (anterior == quantidadeNova) throw new IllegalArgumentException("A quantidade informada e igual ao estoque atual");
        produto.setQuantidadeEstoque(quantidadeNova);
        if (quantidadeNova == 0) produto.setAtivo(false);
        produtoRepository.save(produto);
        MovimentacaoEstoque movimento = new MovimentacaoEstoque();
        movimento.setProduto(produto); movimento.setQuantidadeAnterior(anterior); movimento.setQuantidadeNova(quantidadeNova);
        movimento.setVariacao(quantidadeNova-anterior); movimento.setMotivo(motivo.trim()); movimento.setResponsavel(responsavel);
        movimentacoes.save(movimento);
        return produtoMapper.toResponseDto(produto);
    }

    @Transactional(readOnly=true)
    public List<MovimentacaoEstoqueDto> historicoEstoque(Long produtoId){
        return movimentacoes.findAllByProdutoIdOrderByCriadoEmDesc(produtoId).stream().map(m->new MovimentacaoEstoqueDto(m.getId(),m.getProduto().getId(),m.getProduto().getNome(),m.getQuantidadeAnterior(),m.getQuantidadeNova(),m.getVariacao(),m.getMotivo(),m.getResponsavel(),m.getCriadoEm())).toList();
    }

    @Transactional(readOnly = true)
    public Page<ProdutoResponseDto> listarAdmin(String busca, int pagina, int tamanho) {
        String termo = busca == null ? "" : busca.trim().toLowerCase();
        var todos = produtoRepository.findAll(Sort.by(Sort.Order.asc("nome").ignoreCase())).stream()
                .filter(p -> termo.isBlank() || ((p.getNome()+" "+p.getCodigo()+" "+(p.getMarca()==null?"":p.getMarca())).toLowerCase().contains(termo)))
                .map(produtoMapper::toResponseDto).toList();
        int page = Math.max(0, pagina), size = Math.max(1, Math.min(500, tamanho));
        int inicio = Math.min(page * size, todos.size()), fim = Math.min(inicio + size, todos.size());
        return new PageImpl<>(todos.subList(inicio, fim), PageRequest.of(page, size), todos.size());
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

    @Transactional(readOnly = true)
    public List<MarcaResumoDto> listarMarcas() {
        return produtoRepository.findAll().stream().filter(Produto::isAtivo)
                .map(Produto::getMarca).filter(m -> m != null && !m.isBlank())
                .collect(Collectors.groupingBy(String::trim, Collectors.counting())).entrySet().stream()
                .map(e -> new MarcaResumoDto(e.getKey(), e.getValue()))
                .sorted(java.util.Comparator.comparing(MarcaResumoDto::nome, String.CASE_INSENSITIVE_ORDER)).toList();
    }

    @Transactional(readOnly = true)
    public List<ObjetivoResumoDto> listarObjetivos() {
        Map<String, Long> totais = produtoRepository.findAll().stream().filter(Produto::isAtivo)
                .flatMap(p -> p.getObjetivos().stream()).collect(Collectors.groupingBy(String::toUpperCase, Collectors.counting()));
        return java.util.Arrays.stream(ObjetivoProduto.values())
                .map(o -> new ObjetivoResumoDto(o.name(), o.getNome(), totais.getOrDefault(o.name(), 0L))).toList();
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
        if (dto.isOferta() && (p.getValorDesconto() == null || p.getValorDesconto().signum() <= 0)) {
            throw new IllegalArgumentException("Informe um desconto antes de marcar o produto como oferta");
        }
        p.setObjetivos(validarObjetivos(dto.getObjetivos())); p.setEsportes(normalizar(dto.getEsportes()));
        p.setVegano(dto.isVegano()); p.setVegetariano(dto.isVegetariano());
        p.setLinhaClinica(dto.isLinhaClinica()); p.setLancamento(dto.isLancamento());
        p.setDestaque(dto.isDestaque()); p.setOferta(dto.isOferta()); p.setKit(dto.isKit());
        p.setSubcategoria(dto.getSubcategoria() == null ? null : dto.getSubcategoria().trim().toUpperCase());
        p.setAvaliacaoMedia(dto.getAvaliacaoMedia());
        return produtoMapper.toResponseDto(produtoRepository.save(p));
    }

    private java.util.Set<String> validarObjetivos(java.util.Set<String> valores) {
        var normalizados = normalizar(valores);
        for (String valor : normalizados) {
            try { ObjetivoProduto.valueOf(valor); }
            catch (IllegalArgumentException e) { throw new IllegalArgumentException("Objetivo inválido: " + valor); }
        }
        return normalizados;
    }

    private java.util.Set<String> normalizar(java.util.Set<String> valores) {
        if (valores == null) return new java.util.HashSet<>();
        return valores.stream().filter(java.util.Objects::nonNull).map(String::trim).filter(v -> !v.isEmpty())
                .map(String::toUpperCase).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new));
    }









}
