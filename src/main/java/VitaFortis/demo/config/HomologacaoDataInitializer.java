package VitaFortis.demo.config;

import VitaFortis.demo.v1.entity.Cupom;
import VitaFortis.demo.v1.entity.Produto;
import VitaFortis.demo.v1.entity.Usuario;
import VitaFortis.demo.v1.enums.CategoriaProduto;
import VitaFortis.demo.v1.enums.CupomTipo;
import VitaFortis.demo.v1.enums.TipoUsuario;
import VitaFortis.demo.v1.repository.CupomRepository;
import VitaFortis.demo.v1.repository.ProdutoRepository;
import VitaFortis.demo.v1.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@Profile("homologacao")
public class HomologacaoDataInitializer implements CommandLineRunner {
    private final UsuarioRepository usuarios;
    private final ProdutoRepository produtos;
    private final CupomRepository cupons;
    private final PasswordEncoder encoder;

    public HomologacaoDataInitializer(UsuarioRepository usuarios, ProdutoRepository produtos,
                                     CupomRepository cupons, PasswordEncoder encoder) {
        this.usuarios = usuarios; this.produtos = produtos; this.cupons = cupons; this.encoder = encoder;
    }

    @Override @Transactional
    public void run(String... args) {
        Usuario admin = usuario("admin@vitafortis.test", "Administrador Homologacao", "52998224725", TipoUsuario.ADMIN);
        Usuario colaborador = usuario("colaborador@vitafortis.test", "Colaborador Homologacao", "16899535009", TipoUsuario.COLABORADOR);
        colaborador.setPermissaoRelatorios(true);
        usuarios.save(colaborador);
        usuario("cliente@vitafortis.test", "Cliente Homologacao", "11144477735", TipoUsuario.CLIENTE);
        usuario("cliente2@vitafortis.test", "Segundo Cliente", "39053344705", TipoUsuario.CLIENTE);

        produto("HML-WHEY", "Whey Homologacao", 149.90, 20, true, CategoriaProduto.PROTEINAS);
        produto("HML-CREATINA", "Creatina Estoque Baixo", 89.90, 2, true, CategoriaProduto.AMINOACIDOS);
        produto("HML-ZERO", "Produto Sem Estoque", 49.90, 0, true, CategoriaProduto.VITAMINAS);
        produto("HML-INATIVO", "Produto Inativo", 39.90, 10, false, CategoriaProduto.VITAMINAS);

        if (!cupons.existsByCodigoIgnoreCase("COLAB10")) {
            Cupom cupom = new Cupom(); cupom.setCodigo("COLAB10"); cupom.setDescricao("Cupom de homologacao vinculado");
            cupom.setTipo(CupomTipo.PERCENTUAL); cupom.setDesconto(new BigDecimal("10.00")); cupom.setMinSubtotal(BigDecimal.ZERO);
            cupom.setAtivo(true); cupom.setColaborador(colaborador); cupom.setPercentualCashback(new BigDecimal("5.00")); cupons.save(cupom);
        }
    }

    private Usuario usuario(String email, String nome, String cpf, TipoUsuario tipo) {
        return usuarios.findByEmail(email).orElseGet(() -> {
            Usuario u = new Usuario(); u.setEmail(email); u.setNome(nome); u.setCpf(cpf); u.setTelefone("11999999999");
            u.setSenha(encoder.encode("Teste@123")); u.setTipo(tipo); u.setAtivo(true); u.setSaldoCashback(BigDecimal.ZERO);
            return usuarios.save(u);
        });
    }

    private void produto(String codigo, String nome, double preco, int estoque, boolean ativo, CategoriaProduto categoria) {
        if (produtos.existsByCodigoIgnoreCase(codigo)) return;
        Produto p = new Produto(); p.setCodigo(codigo); p.setNome(nome); p.setDescricao("Produto criado exclusivamente para homologacao do fluxo de vendas.");
        p.setMarca("Vita Test"); p.setUnidade("UN"); p.setPreco(BigDecimal.valueOf(preco)); p.setQuantidadeEstoque(estoque);
        p.setCategoria(categoria); p.setAtivo(ativo); produtos.save(p);
    }
}
