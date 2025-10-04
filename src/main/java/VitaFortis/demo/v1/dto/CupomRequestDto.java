package VitaFortis.demo.v1.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class CupomRequestDto {

    private String codigo;
    private String descricao;
    private double desconto;
    private LocalDateTime dataVencimento;

}
