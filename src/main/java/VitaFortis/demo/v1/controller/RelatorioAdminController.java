package VitaFortis.demo.v1.controller;

import VitaFortis.demo.v1.enums.TipoRelatorio;
import VitaFortis.demo.v1.service.RelatorioService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import VitaFortis.demo.v1.enums.TipoUsuario;
import VitaFortis.demo.v1.repository.UsuarioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/v1/admin/relatorios")
public class RelatorioAdminController {
    private final RelatorioService relatorios;
    private final UsuarioRepository usuarios;

    public RelatorioAdminController(RelatorioService relatorios, UsuarioRepository usuarios) {
        this.relatorios = relatorios;
        this.usuarios = usuarios;
    }

    @GetMapping(value = "/{tipo}.csv", produces = "text/csv")
    public ResponseEntity<byte[]> exportar(
            @PathVariable TipoRelatorio tipo,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            Authentication authentication) {
        var atual = usuarios.findByEmail(authentication.getName().trim().toLowerCase())
                .orElseThrow(() -> new AccessDeniedException("Usuario nao autenticado"));
        if (atual.getTipo() == TipoUsuario.COLABORADOR && !atual.isPermissaoRelatorios()) {
            throw new AccessDeniedException("Sem permissao de relatorio");
        }
        if (fim.isBefore(inicio)) throw new IllegalArgumentException("Periodo do relatorio invalido");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("vita-fortis-" + tipo.name().toLowerCase() + ".csv", StandardCharsets.UTF_8).build());
        return ResponseEntity.ok().headers(headers).body(relatorios.gerar(tipo, inicio, fim));
    }
}
