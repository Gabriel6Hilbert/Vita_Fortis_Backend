package VitaFortis.demo.config;

import VitaFortis.demo.v1.entity.CatalogoCadastro;
import VitaFortis.demo.v1.repository.CatalogoCadastroRepository;
import VitaFortis.demo.v1.repository.ProdutoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component @Order(100)
public class CatalogoCadastroInitializer implements CommandLineRunner {
    private final ProdutoRepository produtos;
    private final CatalogoCadastroRepository cadastros;
    public CatalogoCadastroInitializer(ProdutoRepository produtos, CatalogoCadastroRepository cadastros) {
        this.produtos = produtos; this.cadastros = cadastros;
    }
    @Override @Transactional
    public void run(String... args) {
        // Migra os valores existentes sem substituir nomes ou reativar cadastros administrativos.
        produtos.findAll().stream().map(p -> p.getCategoria()).filter(java.util.Objects::nonNull).distinct().forEach(codigo -> {
            if (cadastros.findByTipoAndCodigo("CATEGORIA", codigo).isPresent()) return;
            var cadastro = new CatalogoCadastro();
            cadastro.setTipo("CATEGORIA"); cadastro.setCodigo(codigo); cadastro.setNome(codigo.replace('_', ' '));
            cadastros.save(cadastro);
        });
    }
}
