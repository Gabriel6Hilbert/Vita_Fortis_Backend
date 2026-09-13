package VitaFortis.demo.v1.controller;

import VitaFortis.demo.v1.service.ProdutoImagemService;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.Duration;

@RestController
@RequestMapping("/uploads/produtos")
public class ProdutoImagemController {
    private final ProdutoImagemService imagens;

    public ProdutoImagemController(ProdutoImagemService imagens) { this.imagens = imagens; }

    @GetMapping("/{nome}")
    public ResponseEntity<byte[]> buscar(@PathVariable String nome) throws IOException {
        var arquivo = imagens.buscar(nome);
        if (arquivo.isEmpty()) return ResponseEntity.notFound().build();
        var imagem = arquivo.get();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(imagem.contentType()))
                .contentLength(imagem.conteudo().length)
                .cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable())
                .eTag(nome)
                .header("X-Content-Type-Options", "nosniff")
                .body(imagem.conteudo());
    }
}
