package VitaFortis.demo.v1.controller;

import VitaFortis.demo.v1.entity.CatalogoCadastro;
import VitaFortis.demo.v1.service.CatalogoCadastroService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/catalogo/cadastros")
public class CatalogoCadastroController {
    private final CatalogoCadastroService service;
    public CatalogoCadastroController(CatalogoCadastroService service) { this.service = service; }
    @GetMapping public List<CatalogoCadastro> listar() { return service.listar(); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public CatalogoCadastro criar(@RequestBody @Valid CatalogoCadastro dto) { return service.salvar(null, dto); }
    @PutMapping("/{id}") public CatalogoCadastro editar(@PathVariable Long id, @RequestBody @Valid CatalogoCadastro dto) { return service.salvar(id, dto); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void inativar(@PathVariable Long id) { service.inativar(id); }
}
