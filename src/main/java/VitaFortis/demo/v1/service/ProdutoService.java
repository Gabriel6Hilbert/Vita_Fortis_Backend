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
    private final CatalogoCadastroService cadastros;

    public ProdutoService(ProdutoRepository produtoRepository, ProdutoMapper produtoMapper, MovimentacaoEstoqueRepository movimentacoes, CatalogoCadastroService cadastros) {
        this.produtoRepository = produtoRepository;
        this.produtoMapper = produtoMapper;
        this.movimentacoes = movimentacoes;
        this.cadastros = cadastros;
    }

    // CRIAR PRODUTO
    @Transactional
    public ProdutoResponseDto create (ProdutoRequestDto dto) {
        String codigo = dto.getCodigo().trim().toUpperCase();
        if (produtoRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new IllegalArgumentException("Codigo de produto ja cadastrado");
        }
        cadastros.validarProduto(dto, null);
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
    @Transactional
    public ProdutoResponseDto update (Long produtoId, ProdutoRequestDto dto) {
        Produto entity = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new IllegalArgumentException("Produto nao encontrado"));

        String codigo = dto.getCodigo().trim().toUpperCase();
        produtoRepository.findAll().stream()
                .filter(outro -> outro.getCodigo() != null && outro.getCodigo().equalsIgnoreCase(codigo))
                .filter(outro -> !outro.getId().equals(produtoId))
                .findFirst()
                .ifPresent(outro -> { throw new IllegalArgumentException("Codigo de produto ja cadastrado"); });

        cadastros.validarProduto(dto, entity);
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
    public void arquivar(Long produtoId) {
        Produto produto = produtoRepository.findById(produtoId).orElseThrow(() -> new IllegalArgumentException("Produto nao encontrado"));
        produto.setAtivo(false);
        produtoRepository.save(produto);
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
        Map<String, Long> totais = produtoRepository.contarAtivosPorCategoria().stream()
                .collect(Collectors.toMap(row -> (String) row[0], row -> (Long) row[1]));
        return cadastros.listar().stream().filter(c -> c.getTipo().equals("CATEGORIA") && c.isAtivo())
                .map(c -> new CategoriaResumoDto(c.getCodigo(), c.getNome(), totais.getOrDefault(c.getCodigo(), 0L))).toList();
    }

    @Transactional(readOnly = true)
    public List<MarcaResumoDto> listarMarcas() {
        return produtoRepository.contarAtivosPorMarca().stream()
                .map(row -> new MarcaResumoDto((String) row[0], (Long) row[1])).toList();
    }

    @Transactional(readOnly = true)
    public List<ObjetivoResumoDto> listarObjetivos() {
        Map<String, Long> totais = produtoRepository.contarAtivosPorObjetivo().stream()
                .collect(Collectors.toMap(row -> ((String) row[0]).toUpperCase(), row -> (Long) row[1]));
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

    @Transactional
    public Map<String,Object> importar(org.springframework.web.multipart.MultipartFile arquivo, boolean preVisualizar) throws java.io.IOException {
        String nome = java.util.Optional.ofNullable(arquivo.getOriginalFilename()).orElse("").toLowerCase();
        if (arquivo.isEmpty()) throw new IllegalArgumentException("Selecione uma planilha com produtos");
        List<List<String>> linhas;
        if (nome.endsWith(".xlsx")) {
            linhas = new java.util.ArrayList<>();
            try (var workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook(arquivo.getInputStream())) {
                if (workbook.getNumberOfSheets() == 0) throw new IllegalArgumentException("Planilha sem abas");
                var formatter = new org.apache.poi.ss.usermodel.DataFormatter(java.util.Locale.US);
                for (var row : workbook.getSheetAt(0)) {
                    List<String> cols = new java.util.ArrayList<>();
                    for (int i=0; i<10; i++) cols.add(formatter.formatCellValue(row.getCell(i)));
                    linhas.add(cols);
                }
            }
        } else if (nome.endsWith(".csv")) {
            String texto = new String(arquivo.getBytes(), java.nio.charset.StandardCharsets.UTF_8).replace("\uFEFF", "");
            String cabecalho = texto.lines().findFirst().orElse("");
            linhas = CsvDados.ler(texto, cabecalho.contains(";") ? ';' : ',');
        } else throw new IllegalArgumentException("Use um arquivo CSV ou XLSX");
        List<String> colunas = List.of("codigo", "nome", "descricao", "marca", "unidade", "preco", "estoque", "categoria", "imagemUrl", "ativo");
        if (linhas.isEmpty() || !linhas.get(0).stream().map(String::trim).toList().equals(colunas)) {
            throw new IllegalArgumentException("Cabecalho invalido. Baixe o modelo CSV e mantenha a ordem das colunas.");
        }
        List<String> erros = new java.util.ArrayList<>();
        List<Produto> validos = new java.util.ArrayList<>();
        var codigos = new java.util.HashSet<String>();
        int inseridos=0, atualizados=0;
        try (var factory = jakarta.validation.Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            for (int index=1; index<linhas.size(); index++) {
                var c = linhas.get(index);
                if (c.stream().allMatch(String::isBlank)) continue;
                try {
                    if (c.size()!=10) throw new IllegalArgumentException("esperadas 10 colunas");
                    String codigo = c.get(0).trim().toUpperCase();
                    if (codigo.isBlank()) throw new IllegalArgumentException("SKU obrigatorio");
                    if (!codigos.add(codigo)) throw new IllegalArgumentException("SKU duplicado no arquivo: " + codigo);
                    Produto p = new Produto();
                    p.setCodigo(codigo); p.setNome(c.get(1).trim()); p.setDescricao(c.get(2).trim());
                    p.setMarca(c.get(3).trim()); p.setUnidade(c.get(4).trim());
                    p.setPreco(new BigDecimal(c.get(5).trim().replace(',', '.')));
                    p.setQuantidadeEstoque(Integer.parseInt(c.get(6).trim()));
                    p.setCategoria(cadastros.validarCategoria(c.get(7).trim().toUpperCase(), null));
                    p.setImagemUrl(c.get(8).isBlank() ? null : c.get(8).trim());
                    String ativo=c.get(9).trim();
                    if (!ativo.isBlank() && !ativo.equalsIgnoreCase("true") && !ativo.equalsIgnoreCase("false")) throw new IllegalArgumentException("ativo deve ser true ou false");
                    p.setAtivo(ativo.isBlank() || Boolean.parseBoolean(ativo));
                    if (p.getPreco().signum()<0 || (p.isAtivo() && (p.getPreco().signum()==0 || p.getQuantidadeEstoque()<=0))) throw new IllegalArgumentException("produto ativo exige preco e estoque positivos; preco nao pode ser negativo");
                    var falhas=validator.validate(p);
                    if (!falhas.isEmpty()) throw new IllegalArgumentException(falhas.stream().map(v->v.getPropertyPath()+": "+v.getMessage()).sorted().collect(Collectors.joining("; ")));
                    validos.add(p);
                    if (produtoRepository.existsByCodigoIgnoreCase(codigo)) atualizados++; else inseridos++;
                } catch (IllegalArgumentException erro) { erros.add("Linha "+(index+1)+": "+erro.getMessage()); }
            }
        }
        if (validos.isEmpty() && erros.isEmpty()) erros.add("A planilha nao contem produtos");
        if (!preVisualizar) {
            if (!erros.isEmpty()) throw new IllegalArgumentException("Importacao cancelada: "+String.join(" | ", erros));
            for (Produto dado : validos) {
                Produto destino=produtoRepository.findByCodigoIgnoreCase(dado.getCodigo()).orElseGet(Produto::new);
                destino.setCodigo(dado.getCodigo()); destino.setNome(dado.getNome()); destino.setDescricao(dado.getDescricao());
                destino.setMarca(dado.getMarca()); destino.setUnidade(dado.getUnidade()); destino.setPreco(dado.getPreco());
                destino.setQuantidadeEstoque(dado.getQuantidadeEstoque()); destino.setCategoria(dado.getCategoria());
                destino.setImagemUrl(dado.getImagemUrl()); destino.setAtivo(dado.isAtivo());
                produtoRepository.save(destino);
            }
            produtoRepository.flush();
        }
        return Map.of("preVisualizacao",preVisualizar,"inseridos",inseridos,"atualizados",atualizados,"ignorados",0,"rejeitados",erros.size(),"erros",erros);
    }
}
