package VitaFortis.demo.v1.controller;

import VitaFortis.demo.v1.dto.*;
import VitaFortis.demo.v1.service.FidelidadeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/fidelidade")
public class FidelidadeController {
    private final FidelidadeService fidelidade;
    public FidelidadeController(FidelidadeService fidelidade) { this.fidelidade = fidelidade; }
    @GetMapping("/{usuarioId}/saldo") public FidelidadeSaldoDto saldo(@PathVariable Long usuarioId) { return fidelidade.saldo(usuarioId); }
    @GetMapping("/produtos") public List<ProdutoResponseDto> produtos() { return fidelidade.produtosResgataveis(); }
    @GetMapping("/{usuarioId}/historico") public List<ResgateFidelidadeResponseDto> historico(@PathVariable Long usuarioId) { return fidelidade.historico(usuarioId); }
    @PostMapping("/resgates") @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public ResgateFidelidadeResponseDto resgatar(@Valid @RequestBody ResgateFidelidadeRequestDto dto) { return fidelidade.resgatar(dto); }
}
