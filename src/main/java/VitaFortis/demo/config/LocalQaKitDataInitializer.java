package VitaFortis.demo.config;

import VitaFortis.demo.v1.entity.Produto;
import VitaFortis.demo.v1.repository.ProdutoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Component
@Profile("local-qa")
public class LocalQaKitDataInitializer implements CommandLineRunner {
    public static final String SKU="QA-KIT-HOME-001";
    private final ProdutoRepository produtos;
    public LocalQaKitDataInitializer(ProdutoRepository produtos){this.produtos=produtos;}
    @Override @Transactional public void run(String... args){
        Produto p=produtos.findByCodigoIgnoreCase(SKU).orElseGet(Produto::new);
        p.setCodigo(SKU);p.setNome("Kit QA Evolução");p.setDescricao("Registro fictício criado exclusivamente no ambiente local para validação de qualidade.");
        p.setMarca("Vita Fortis QA");p.setUnidade("KIT");p.setCategoria("KITS");p.setPreco(new BigDecimal("149.90"));p.setValorItensSeparados(new BigDecimal("189.90"));
        p.setQuantidadeEstoque(3);p.setImagemUrl("/assets/imagens/proteina_exemplo.png");p.setKit(true);p.setAtivo(true);
        p.getAtributos().put("quantidadeItens","3");p.getAtributos().put("conteudoKit","Whey fictício 900g • Creatina fictícia 300g • Coqueteleira de teste");
        produtos.save(p);
    }
}
