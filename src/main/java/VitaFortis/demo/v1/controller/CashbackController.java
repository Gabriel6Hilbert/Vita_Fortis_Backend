package VitaFortis.demo.v1.controller;

import VitaFortis.demo.v1.dto.CashbackSaldoDto;
import VitaFortis.demo.v1.dto.ColaboradorResumoDto;
import VitaFortis.demo.v1.service.CashbackService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/colaborador/cashback")
public class CashbackController {
    private final CashbackService cashback;

    public CashbackController(CashbackService cashback) {
        this.cashback = cashback;
    }

    @GetMapping
    public CashbackSaldoDto saldo(Authentication authentication) {
        return cashback.meuSaldo(authentication.getName());
    }

    @GetMapping("/resumo")
    public ColaboradorResumoDto resumo(Authentication authentication) {
        return cashback.meuResumo(authentication.getName());
    }
}
