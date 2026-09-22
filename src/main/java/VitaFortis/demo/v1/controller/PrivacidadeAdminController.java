package VitaFortis.demo.v1.controller;

import VitaFortis.demo.v1.dto.ProcessamentoPrivacidadeRequestDto;
import VitaFortis.demo.v1.dto.SolicitacaoPrivacidadeAdminDto;
import VitaFortis.demo.v1.service.PrivacidadeService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/privacidade/solicitacoes")
public class PrivacidadeAdminController {
    private final PrivacidadeService privacidade;

    public PrivacidadeAdminController(PrivacidadeService privacidade) {
        this.privacidade = privacidade;
    }

    @GetMapping
    public List<SolicitacaoPrivacidadeAdminDto> listar() {
        return privacidade.listarAdministracao();
    }

    @PatchMapping("/{id}")
    public SolicitacaoPrivacidadeAdminDto processar(@PathVariable Long id,
                                                    @Valid @RequestBody ProcessamentoPrivacidadeRequestDto dto,
                                                    Authentication authentication) {
        return privacidade.processar(id, dto, authentication.getName());
    }
}
