package VitaFortis.demo.v1.controller;

import VitaFortis.demo.v1.dto.EnderecoRequestDto;
import VitaFortis.demo.v1.dto.EnderecoResponseDto;
import VitaFortis.demo.v1.service.EnderecoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios/{usuarioId}/enderecos")
public class EnderecoController {

    private final EnderecoService enderecos;

    public EnderecoController(EnderecoService enderecos) {
        this.enderecos = enderecos;
    }

    @GetMapping
    public List<EnderecoResponseDto> listar(@PathVariable Long usuarioId, Authentication authentication) {
        return enderecos.listar(usuarioId, authentication.getName());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EnderecoResponseDto criar(@PathVariable Long usuarioId, @Valid @RequestBody EnderecoRequestDto dto,
                                     Authentication authentication) {
        return enderecos.criar(usuarioId, dto, authentication.getName());
    }

    @PutMapping("/{enderecoId}")
    public EnderecoResponseDto atualizar(@PathVariable Long usuarioId, @PathVariable Long enderecoId,
                                         @Valid @RequestBody EnderecoRequestDto dto,
                                         Authentication authentication) {
        return enderecos.atualizar(usuarioId, enderecoId, dto, authentication.getName());
    }

    @DeleteMapping("/{enderecoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable Long usuarioId, @PathVariable Long enderecoId,
                        Authentication authentication) {
        enderecos.remover(usuarioId, enderecoId, authentication.getName());
    }
}
