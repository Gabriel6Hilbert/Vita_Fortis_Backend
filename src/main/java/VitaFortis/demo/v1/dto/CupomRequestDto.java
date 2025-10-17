package VitaFortis.demo.v1.dto;

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
    @DecimalMin(value = "0.00")
    private BigDecimal desconto;

    private LocalDateTime dataVencimento; // opcional
}
