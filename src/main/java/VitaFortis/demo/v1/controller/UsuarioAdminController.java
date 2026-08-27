package VitaFortis.demo.v1.controller;

import VitaFortis.demo.v1.dto.Usuario.UsuarioResponseDto;
import VitaFortis.demo.v1.enums.TipoUsuario;
import VitaFortis.demo.v1.service.UsuarioService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/usuarios")
public class UsuarioAdminController {
    private final UsuarioService usuarios;
    public UsuarioAdminController(UsuarioService usuarios) { this.usuarios = usuarios; }
    @GetMapping public List<UsuarioResponseDto> listar() { return usuarios.listar(); }
    @PatchMapping("/{id}/tipo") public UsuarioResponseDto tipo(@PathVariable Long id, @RequestParam TipoUsuario valor) { return usuarios.alterarTipo(id, valor); }
    @PatchMapping("/{id}/ativo") public UsuarioResponseDto ativo(@PathVariable Long id, @RequestParam boolean valor) { return usuarios.alterarAtivo(id, valor); }
}
