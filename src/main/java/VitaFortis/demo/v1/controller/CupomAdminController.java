package VitaFortis.demo.v1.controller;

import VitaFortis.demo.v1.dto.*;
import VitaFortis.demo.v1.service.CupomService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/cupons")
public class CupomAdminController {
    private final CupomService cupons;
    public CupomAdminController(CupomService cupons) { this.cupons = cupons; }
    @GetMapping public List<CupomResponseDto> listar() { return cupons.listar(); }
    @PostMapping @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public CupomResponseDto criar(@Valid @RequestBody CupomRequestDto dto) { return cupons.criar(dto); }
    @PutMapping("/{id}") public CupomResponseDto atualizar(@PathVariable Long id, @Valid @RequestBody CupomRequestDto dto) { return cupons.atualizar(id, dto); }
    @PatchMapping("/{id}/ativo") public CupomResponseDto ativo(@PathVariable Long id, @RequestParam boolean valor) { return cupons.alterarAtivo(id, valor); }
}
