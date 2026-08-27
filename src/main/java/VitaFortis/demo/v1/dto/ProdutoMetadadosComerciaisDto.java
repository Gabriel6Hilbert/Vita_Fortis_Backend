package VitaFortis.demo.v1.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.Set;

@Getter @Setter
public class ProdutoMetadadosComerciaisDto {
    private Set<@Size(max = 50) String> objetivos;
    private Set<@Size(max = 50) String> esportes;
    private boolean vegano;
    private boolean vegetariano;
    private boolean linhaClinica;
    private boolean lancamento;
    @Size(max = 60) private String subcategoria;
    @DecimalMin("0.0") @DecimalMax("5.0") private BigDecimal avaliacaoMedia;
}
