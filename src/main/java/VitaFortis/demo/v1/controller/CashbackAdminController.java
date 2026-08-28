package VitaFortis.demo.v1.controller;

import VitaFortis.demo.v1.dto.CashbackMovimentoRequestDto;
import VitaFortis.demo.v1.dto.CashbackSaldoDto;
import VitaFortis.demo.v1.service.CashbackService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/colaboradores/{colaboradorId}/cashback")
public class CashbackAdminController {
    private final CashbackService cashback;

    public CashbackAdminController(CashbackService cashback) {
        this.cashback = cashback;
    }

    @PostMapping("/ajustes")
    public CashbackSaldoDto ajustar(@PathVariable Long colaboradorId,
                                    @Valid @RequestBody CashbackMovimentoRequestDto dto,
                                    Authentication authentication) {
        return cashback.ajustar(colaboradorId, dto.valor(), dto.justificativa(), authentication.getName());
    }

    @PostMapping("/baixas")
    public CashbackSaldoDto baixar(@PathVariable Long colaboradorId,
                                   @Valid @RequestBody CashbackMovimentoRequestDto dto,
                                   Authentication authentication) {
        return cashback.baixar(colaboradorId, dto.valor(), dto.justificativa(), authentication.getName());
    }
}
