package VitaFortis.demo.v1.controller;

import VitaFortis.demo.v1.dto.*;
import VitaFortis.demo.v1.service.PrivacidadeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/privacidade")
public class PrivacidadeController {
    private final PrivacidadeService privacidade;

    public PrivacidadeController(PrivacidadeService privacidade) {
        this.privacidade = privacidade;
    }

    @GetMapping("/consentimento-comunicacoes")
    public ConsentimentoComunicacaoDto consultarConsentimento(Authentication authentication) {
        return privacidade.consultarConsentimento(authentication.getName());
    }

    @PutMapping("/consentimento-comunicacoes")
    public ConsentimentoComunicacaoDto alterarConsentimento(@RequestBody ConsentimentoComunicacaoDto dto,
                                                             Authentication authentication) {
        return privacidade.alterarConsentimento(authentication.getName(), dto);
    }

    @GetMapping("/solicitacoes")
    public List<SolicitacaoPrivacidadeResponseDto> listar(Authentication authentication) {
        return privacidade.listarProprias(authentication.getName());
    }

    @GetMapping("/solicitacoes/{id}")
    public SolicitacaoPrivacidadeResponseDto buscar(@PathVariable Long id, Authentication authentication) {
        return privacidade.buscarPropria(id, authentication.getName());
    }

    @PostMapping("/solicitacoes")
    @ResponseStatus(HttpStatus.CREATED)
    public SolicitacaoPrivacidadeResponseDto abrir(@Valid @RequestBody SolicitacaoPrivacidadeRequestDto dto,
                                                    Authentication authentication) {
        return privacidade.abrir(authentication.getName(), dto);
    }
}
