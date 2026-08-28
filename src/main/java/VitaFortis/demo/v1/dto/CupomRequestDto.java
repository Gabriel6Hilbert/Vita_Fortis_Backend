package VitaFortis.demo.v1.dto;

import VitaFortis.demo.v1.enums.CupomTipo;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
public class CupomRequestDto {

    @NotBlank
    @Size(max = 50)
    private String codigo;

    @Size(max = 255)
    private String descricao;

    @NotNull
    private CupomTipo tipo;

    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal desconto;

    @DecimalMin(value = "0.00")
    private BigDecimal minSubtotal;

    private LocalDateTime dataVencimento;

    private Long colaboradorId;

    @DecimalMin("0.00")
    @DecimalMax("100.00")
    private BigDecimal percentualCashback;
}
