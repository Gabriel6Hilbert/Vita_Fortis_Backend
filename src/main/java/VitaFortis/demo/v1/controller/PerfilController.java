package VitaFortis.demo.v1.controller;

import VitaFortis.demo.v1.dto.AlteracaoSenhaDto;
import VitaFortis.demo.v1.dto.PerfilAtualizacaoDto;
import VitaFortis.demo.v1.dto.Usuario.UsuarioResponseDto;
import VitaFortis.demo.v1.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/me")
public class PerfilController {
    private final UsuarioService usuarios;

    public PerfilController(UsuarioService usuarios) {
        this.usuarios = usuarios;
    }

    @GetMapping
    public UsuarioResponseDto obter(Authentication authentication) {
        return usuarios.buscarPorEmail(authentication.getName());
    }

    @PutMapping
    public UsuarioResponseDto atualizar(@Valid @RequestBody PerfilAtualizacaoDto dto,
                                         Authentication authentication) {
        return usuarios.atualizarPerfil(authentication.getName(), dto);
    }

    @PutMapping("/senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void alterarSenha(@Valid @RequestBody AlteracaoSenhaDto dto, Authentication authentication) {
        usuarios.alterarSenha(authentication.getName(), dto);
    }
}
