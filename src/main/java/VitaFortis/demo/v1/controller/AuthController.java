package VitaFortis.demo.v1.controller;

import VitaFortis.demo.v1.dto.Usuario.UsuarioLoginDto;
import VitaFortis.demo.v1.dto.Usuario.UsuarioRequestDto;
import VitaFortis.demo.v1.dto.Usuario.UsuarioResponseDto;
import VitaFortis.demo.v1.service.UsuarioService;
import VitaFortis.demo.v1.service.RecuperacaoSenhaService;
import VitaFortis.demo.v1.dto.RecuperacaoSenhaSolicitacaoDto;
import VitaFortis.demo.v1.dto.RecuperacaoSenhaConfirmacaoDto;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final UsuarioService usuarios;
    private final AuthenticationManager authenticationManager;
    private final RecuperacaoSenhaService recuperacaoSenha;

    public AuthController(UsuarioService usuarios, AuthenticationManager authenticationManager,
                          RecuperacaoSenhaService recuperacaoSenha) {
        this.usuarios = usuarios;
        this.authenticationManager = authenticationManager;
        this.recuperacaoSenha = recuperacaoSenha;
    }

    @PostMapping("/cadastro")
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public UsuarioResponseDto cadastro(@Valid @RequestBody UsuarioRequestDto dto) { return usuarios.create(dto); }

    @PostMapping("/login")
    public UsuarioResponseDto login(@Valid @RequestBody UsuarioLoginDto dto, HttpServletRequest request) {
        var authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getSenha()));
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        request.getSession(true).setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        return usuarios.buscarPorEmail(dto.getEmail());
    }

    @PostMapping("/logout") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request) { var session=request.getSession(false); if(session!=null) session.invalidate(); SecurityContextHolder.clearContext(); }

    @PostMapping("/recuperacao-senha")
    public Map<String, String> solicitarRecuperacao(@Valid @RequestBody RecuperacaoSenhaSolicitacaoDto dto) {
        recuperacaoSenha.solicitar(dto.email());
        return Map.of("message", "Se o e-mail estiver cadastrado, você receberá as instruções em instantes.");
    }

    @PostMapping("/redefinicao-senha")
    public Map<String, String> redefinirSenha(@Valid @RequestBody RecuperacaoSenhaConfirmacaoDto dto) {
        recuperacaoSenha.redefinir(dto.token(), dto.novaSenha());
        return Map.of("message", "Senha redefinida com sucesso. Você já pode entrar.");
    }
}
