package VitaFortis.demo.v1.controller;

import VitaFortis.demo.v1.dto.RelatorioFiltroDto;
import VitaFortis.demo.v1.enums.TipoRelatorio;
import VitaFortis.demo.v1.service.RelatorioService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/admin/relatorios")
public class RelatorioAdminController {
    private final RelatorioService relatorios;
    public RelatorioAdminController(RelatorioService relatorios) { this.relatorios=relatorios; }

    @GetMapping("/{tipo}.{formato}")
    public ResponseEntity<byte[]> exportar(@PathVariable TipoRelatorio tipo, @PathVariable String formato,
                                         @Valid @ModelAttribute RelatorioFiltroDto filtro) {
        filtro.validar();
        byte[] dados;
        MediaType contentType;
        switch(formato) {
            case "csv" -> { dados=relatorios.gerar(tipo,filtro); contentType=new MediaType("text","csv",StandardCharsets.UTF_8); }
            case "xlsx" -> { dados=relatorios.gerarXlsx(tipo,filtro); contentType=MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"); }
            case "pdf" -> { dados=relatorios.gerarPdf(tipo,filtro); contentType=MediaType.APPLICATION_PDF; }
            default -> throw new IllegalArgumentException("Formato deve ser CSV, XLSX ou PDF");
        }
        return ResponseEntity.ok().contentType(contentType).header(HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment().filename("vita-fortis-"+tipo.name().toLowerCase()+"."+formato,StandardCharsets.UTF_8).build().toString()).body(dados);
    }
}
