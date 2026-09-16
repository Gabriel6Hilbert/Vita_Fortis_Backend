package VitaFortis.demo.v1.service;

import VitaFortis.demo.v1.dto.ProdutoRequestDto;
import VitaFortis.demo.v1.entity.CatalogoCadastro;
import VitaFortis.demo.v1.entity.Produto;
import VitaFortis.demo.v1.repository.CatalogoCadastroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
public class CatalogoCadastroService {
    private final CatalogoCadastroRepository repository;
    public CatalogoCadastroService(CatalogoCadastroRepository repository) { this.repository = repository; }

    @Transactional(readOnly = true)
    public List<CatalogoCadastro> listar() { return repository.findAllByOrderByTipoAscNomeAsc(); }

    @Transactional
    public CatalogoCadastro salvar(Long id, CatalogoCadastro dados) {
        String codigo = dados.getCodigo().trim().toUpperCase(Locale.ROOT);
        CatalogoCadastro atual = id == null ? new CatalogoCadastro() : buscar(id);
        if (id != null && (!atual.getCodigo().equals(codigo) || !atual.getTipo().equals(dados.getTipo())))
            throw new IllegalArgumentException("Codigo e tipo nao podem mudar; edite o nome para preservar os vinculos");
        repository.findByTipoAndCodigo(dados.getTipo(), codigo).filter(c -> !c.getId().equals(id))
                .ifPresent(c -> { throw new IllegalArgumentException("Codigo ja cadastrado, inclusive entre inativos"); });
        atual.setTipo(dados.getTipo()); atual.setCodigo(codigo); atual.setNome(dados.getNome().trim()); atual.setAtivo(dados.isAtivo());
        return repository.save(atual);
    }

    @Transactional
    public void inativar(Long id) { buscar(id).setAtivo(false); }

    private CatalogoCadastro buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Cadastro nao encontrado"));
    }

    public String validarCategoria(String codigo, String anterior) {
        if (codigo == null || codigo.isBlank()) throw new IllegalArgumentException("Categoria obrigatoria");
        codigo = codigo.trim().toUpperCase(Locale.ROOT);
        var cadastro = repository.findByTipoAndCodigo("CATEGORIA", codigo)
                .orElseThrow(() -> new IllegalArgumentException("Categoria nao cadastrada no painel"));
        if (!cadastro.isAtivo() && !codigo.equals(anterior)) throw new IllegalArgumentException("Categoria inativa");
        return codigo;
    }

    public void validarProduto(ProdutoRequestDto dto, Produto anterior) {
        dto.setCategoria(validarCategoria(dto.getCategoria(), anterior == null ? null : anterior.getCategoria()));
        if (dto.getAtributos() == null) return;
        Map<String, String> anteriores = anterior == null ? Map.of() : anterior.getAtributos();
        for (var entry : dto.getAtributos().entrySet()) {
            var cadastro = repository.findByTipoAndCodigo("ATRIBUTO", entry.getKey())
                    .orElseThrow(() -> new IllegalArgumentException("Atributo nao cadastrado: " + entry.getKey()));
            if (!cadastro.isAtivo() && !Objects.equals(anteriores.get(entry.getKey()), entry.getValue()))
                throw new IllegalArgumentException("Atributo inativo: " + cadastro.getNome());
            if (entry.getValue() == null || entry.getValue().isBlank()) throw new IllegalArgumentException("Informe o valor do atributo ou remova-o");
        }
    }
}
