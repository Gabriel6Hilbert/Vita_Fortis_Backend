package VitaFortis.demo.v1.controller;

import VitaFortis.demo.v1.dto.Usuario.UsuarioLoginDto;
import VitaFortis.demo.v1.dto.Usuario.UsuarioRequestDto;
import VitaFortis.demo.v1.dto.Usuario.UsuarioResponseDto;
import VitaFortis.demo.v1.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final UsuarioService usuarios;
    private final AuthenticationManager authenticationManager;

    public AuthController(UsuarioService usuarios, AuthenticationManager authenticationManager) {
        this.usuarios = usuarios; this.authenticationManager = authenticationManager;
    }

    @PostMapping("/cadastro")
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public UsuarioResponseDto cadastro(@Valid @RequestBody UsuarioRequestDto dto) { return usuarios.create(dto); }

    @PostMapping("/login")
    public UsuarioResponseDto login(@Valid @RequestBody UsuarioLoginDto dto) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getSenha()));
        return usuarios.buscarPorEmail(dto.getEmail());
    }
}
