package VitaFortis.demo.v1.service;

import VitaFortis.demo.v1.entity.ProdutoImagem;
import VitaFortis.demo.v1.repository.ProdutoImagemRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class ProdutoImagemService {
    public static final int MAX_BYTES = 5 * 1024 * 1024;
    private static final Set<String> TIPOS = Set.of("image/jpeg", "image/png", "image/webp");
    private static final Pattern NOME = Pattern.compile("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}\\.(jpg|png|webp)");
    private static final byte[] PNG = {(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a};

    private final ProdutoImagemRepository imagens;
    private final Path diretorioLegado;

    public ProdutoImagemService(ProdutoImagemRepository imagens,
            @Value("${vita-fortis.uploads-diretorio:uploads}") String diretorioUploads) {
        this.imagens = imagens;
        this.diretorioLegado = Path.of(diretorioUploads).toAbsolutePath().normalize().resolve("produtos");
    }

    @Transactional
    public String salvar(MultipartFile imagem) throws IOException {
        if (imagem.isEmpty()) throw new IllegalArgumentException("Selecione uma imagem");
        if (imagem.getSize() > MAX_BYTES) throw new MaxUploadSizeExceededException(MAX_BYTES);
        String declarado = imagem.getContentType();
        if (declarado == null || !TIPOS.contains(declarado)) {
            throw new IllegalArgumentException("Use uma imagem JPG, PNG ou WebP");
        }
        byte[] conteudo;
        try (var input = imagem.getInputStream()) {
            conteudo = input.readNBytes(MAX_BYTES + 1);
        }
        if (conteudo.length > MAX_BYTES) throw new MaxUploadSizeExceededException(MAX_BYTES);
        String detectado = detectarTipo(conteudo);
        if (!declarado.equals(detectado)) {
            throw new IllegalArgumentException("O conteúdo do arquivo não corresponde a uma imagem JPG, PNG ou WebP do tipo informado");
        }
        String nome = UUID.randomUUID() + extensao(detectado);
        imagens.save(new ProdutoImagem(nome, detectado, conteudo));
        return "/uploads/produtos/" + nome;
    }

    @Transactional(readOnly = true)
    public Optional<ArquivoImagem> buscar(String nome) throws IOException {
        if (!NOME.matcher(nome).matches()) return Optional.empty();
        var persistida = imagens.findById(nome);
        if (persistida.isPresent()) {
            var imagem = persistida.get();
            return Optional.of(new ArquivoImagem(imagem.getContentType(), imagem.getConteudo()));
        }
        // Compatibilidade com URLs gravadas antes da persistência no banco.
        // Novos uploads nunca dependem do disco efêmero do servidor.
        Path arquivo = diretorioLegado.resolve(nome).normalize();
        if (!arquivo.startsWith(diretorioLegado) || !Files.isRegularFile(arquivo)) return Optional.empty();
        if (!arquivo.toRealPath().startsWith(diretorioLegado.toRealPath()) || Files.size(arquivo) > MAX_BYTES) {
            return Optional.empty();
        }
        byte[] conteudo;
        try (var input = Files.newInputStream(arquivo)) {
            conteudo = input.readNBytes(MAX_BYTES + 1);
        }
        String tipo = detectarTipo(conteudo);
        if (conteudo.length > MAX_BYTES || tipo == null || !nome.endsWith(extensao(tipo))) return Optional.empty();
        return Optional.of(new ArquivoImagem(tipo, conteudo));
    }

    private static String extensao(String tipo) {
        return switch (tipo) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }

    private static String detectarTipo(byte[] bytes) {
        if (bytes.length >= 33 && Arrays.equals(PNG, Arrays.copyOf(bytes, PNG.length))
                && ascii(bytes, 12, "IHDR") && bytes[8] == 0 && bytes[9] == 0 && bytes[10] == 0 && bytes[11] == 13) {
            return "image/png";
        }
        if (bytes.length >= 4 && (bytes[0] & 0xff) == 0xff && (bytes[1] & 0xff) == 0xd8
                && (bytes[2] & 0xff) == 0xff && (bytes[bytes.length - 2] & 0xff) == 0xff
                && (bytes[bytes.length - 1] & 0xff) == 0xd9) {
            return "image/jpeg";
        }
        if (bytes.length >= 20 && ascii(bytes, 0, "RIFF") && ascii(bytes, 8, "WEBP")
                && (ascii(bytes, 12, "VP8 ") || ascii(bytes, 12, "VP8L") || ascii(bytes, 12, "VP8X"))) {
            long tamanho = (bytes[4] & 0xffL) | ((bytes[5] & 0xffL) << 8)
                    | ((bytes[6] & 0xffL) << 16) | ((bytes[7] & 0xffL) << 24);
            if (tamanho == bytes.length - 8L) return "image/webp";
        }
        return null;
    }

    private static boolean ascii(byte[] bytes, int inicio, String esperado) {
        return new String(bytes, inicio, esperado.length(), StandardCharsets.US_ASCII).equals(esperado);
    }

    public record ArquivoImagem(String contentType, byte[] conteudo) {}
}
