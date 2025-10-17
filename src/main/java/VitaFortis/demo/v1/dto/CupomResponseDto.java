package VitaFortis.demo.v1.dto;

import VitaFortis.demo.v1.entity.Usuario;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class CupomResponseDto {

    private Long id;
    private String codigo;
    private String descricao;
    private BigDecimal desconto;
    private boolean ativo;
    private LocalDateTime dataCadastro;
    private LocalDateTime dataVencimento;
}
