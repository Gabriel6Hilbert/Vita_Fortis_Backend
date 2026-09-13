package VitaFortis.demo.v1.controller;

import VitaFortis.demo.v1.repository.ProdutoImagemRepository;
import VitaFortis.demo.v1.service.ProdutoImagemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;
import java.util.zip.CRC32;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProdutoImagemIntegrationTest {
    private static final String UPLOAD = "/api/v1/admin/produtos/imagens";
    private static final Path LEGADO = diretorioTemporario();
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired ProdutoImagemRepository imagens;
    @Autowired EntityManager persistence;

    @DynamicPropertySource
    static void propriedades(DynamicPropertyRegistry registry) {
        registry.add("vita-fortis.uploads-diretorio", LEGADO::toString);
    }

    @AfterAll
    static void limparDiretorios() throws Exception {
        Files.deleteIfExists(LEGADO.resolve("produtos"));
        Files.deleteIfExists(LEGADO);
    }

    @ParameterizedTest
    @CsvSource({"image/png,png", "image/jpeg,jpg", "image/webp,webp"})
    void adminEnviaImagemEPublicoLeOsMesmosBytesDoBanco(String tipo, String extensao) throws Exception {
        byte[] bytes = imagem(extensao);
        String url = enviar(bytes, tipo);
        assertThat(url).matches("/uploads/produtos/[a-f0-9-]{36}\\." + extensao);
        persistence.flush();
        persistence.clear();
        String nome = url.substring(url.lastIndexOf('/') + 1);
        assertThat(imagens.findById(nome).orElseThrow().getConteudo()).containsExactly(bytes);
        persistence.clear();
        // Sem sessão/autenticação e sem depender da entidade que recebeu o upload.
        mvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(content().contentType(tipo))
                .andExpect(content().bytes(bytes))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("Cache-Control", "max-age=31536000, public, immutable"));
        assertThat(Files.exists(LEGADO.resolve("produtos").resolve(nome))).isFalse();
    }

    @Test
    void aceitaExatamenteCincoMegabytesELobNaoTruncaEm64Kb() throws Exception {
        byte[] bytes = pngComTamanho(ProdutoImagemService.MAX_BYTES);
        String url = enviar(bytes, "image/png");
        persistence.flush();
        persistence.clear();
        mvc.perform(get(url)).andExpect(status().isOk()).andExpect(content().bytes(bytes));
    }

    @Test
    void tamanhoAcimaDeCincoMegabytesRetorna413SemPersistir() throws Exception {
        long quantidade = imagens.count();
        mvc.perform(multipart(UPLOAD)
                        .file(new MockMultipartFile("imagem", "grande.png", "image/png", new byte[ProdutoImagemService.MAX_BYTES + 1]))
                        .with(user("admin@teste.com").roles("ADMIN")))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.status").value(413))
                .andExpect(jsonPath("$.mensagem").value("A imagem deve ter no máximo 5 MB"));
        assertThat(imagens.count()).isEqualTo(quantidade);
    }

    @ParameterizedTest
    @ValueSource(strings = {"CLIENTE", "COLABORADOR"})
    void outrosPerfisNaoEnviamImagens(String papel) throws Exception {
        long quantidade = imagens.count();
        mvc.perform(multipart(UPLOAD).file(new MockMultipartFile("imagem", "foto.png", "image/png", imagem("png")))
                        .with(user("usuario@teste.com").roles(papel)))
                .andExpect(status().isForbidden());
        assertThat(imagens.count()).isEqualTo(quantidade);
    }

    @Test
    void visitanteNaoEnviaImagem() throws Exception {
        mvc.perform(multipart(UPLOAD).file(new MockMultipartFile("imagem", "foto.png", "image/png", imagem("png"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void vazioTipoNaoPermitidoEConteudoDisfarcadoSaoRejeitados() throws Exception {
        long quantidade = imagens.count();
        var invalidos = new MockMultipartFile[] {
                new MockMultipartFile("imagem", "vazia.png", "image/png", new byte[0]),
                new MockMultipartFile("imagem", "script.svg", "image/svg+xml", "<svg/>".getBytes(StandardCharsets.UTF_8)),
                new MockMultipartFile("imagem", "foto.png", "image/png", "<script>alert(1)</script>".getBytes(StandardCharsets.UTF_8)),
                new MockMultipartFile("imagem", "foto.jpg", "image/jpeg", imagem("png")),
                new MockMultipartFile("imagem", "foto.webp", "image/webp", "RIFF0000WEBP".getBytes(StandardCharsets.US_ASCII)),
                new MockMultipartFile("imagem", "foto.png", null, imagem("png"))
        };
        for (var arquivo : invalidos) {
            mvc.perform(multipart(UPLOAD).file(arquivo).with(user("admin@teste.com").roles("ADMIN")))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.mensagem").isNotEmpty());
        }
        assertThat(imagens.count()).isEqualTo(quantidade);
    }

    @Test
    void imagemInexistenteOuNomeNaoGeradoRetornam404() throws Exception {
        mvc.perform(get("/uploads/produtos/" + UUID.randomUUID() + ".png")).andExpect(status().isNotFound());
        mvc.perform(get("/uploads/produtos/application.properties")).andExpect(status().isNotFound());
    }

    @Test
    void urlAntigaContinuaLendoArquivoLocal() throws Exception {
        String nome = UUID.randomUUID() + ".png";
        Path arquivo = Files.createDirectories(LEGADO.resolve("produtos")).resolve(nome);
        byte[] bytes = imagem("png");
        Files.write(arquivo, bytes);
        try {
            assertThat(imagens.existsById(nome)).isFalse();
            mvc.perform(get("/uploads/produtos/" + nome))
                    .andExpect(status().isOk()).andExpect(content().contentType("image/png")).andExpect(content().bytes(bytes));
        } finally {
            Files.deleteIfExists(arquivo);
        }
    }

    private String enviar(byte[] bytes, String tipo) throws Exception {
        var resultado = mvc.perform(multipart(UPLOAD)
                        .file(new MockMultipartFile("imagem", "../../nome-do-cliente.php", tipo, bytes))
                        .with(user("admin@teste.com").roles("ADMIN")))
                .andExpect(status().isOk()).andReturn();
        return json.readTree(resultado.getResponse().getContentAsString()).get("url").asText();
    }

    private static byte[] imagem(String extensao) throws Exception {
        if (extensao.equals("webp")) {
            try (var input = ProdutoImagemIntegrationTest.class.getResourceAsStream("/static/assets/imagens/logo-vita-fortis-brand.webp")) {
                if (input == null) throw new IllegalStateException("Imagem WebP do projeto ausente");
                return input.readAllBytes();
            }
        }
        var output = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), extensao, output);
        return output.toByteArray();
    }

    private static byte[] pngComTamanho(int tamanho) throws Exception {
        byte[] base = imagem("png");
        byte[] texto = new byte[tamanho - base.length - 12];
        Arrays.fill(texto, (byte) 'a');
        texto[7] = 0; // Separador obrigatório entre nome e valor do chunk tEXt.
        byte[] tipo = "tEXt".getBytes(StandardCharsets.US_ASCII);
        var output = new ByteArrayOutputStream(tamanho);
        var data = new DataOutputStream(output);
        data.write(base, 0, base.length - 12);
        data.writeInt(texto.length);
        data.write(tipo);
        data.write(texto);
        var crc = new CRC32();
        crc.update(tipo);
        crc.update(texto);
        data.writeInt((int) crc.getValue());
        data.write(base, base.length - 12, 12);
        return output.toByteArray();
    }

    private static Path diretorioTemporario() {
        try { return Files.createTempDirectory("vita-fortis-upload-test-"); }
        catch (java.io.IOException ex) { throw new java.io.UncheckedIOException(ex); }
    }
}
