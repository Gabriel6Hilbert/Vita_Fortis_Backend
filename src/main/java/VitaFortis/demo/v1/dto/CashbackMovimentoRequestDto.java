package VitaFortis.demo.v1.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CashbackMovimentoRequestDto(
        @NotNull @DecimalMin(value = "0.01") BigDecimal valor,
        @NotBlank @Size(max = 300) String justificativa
) {
}
