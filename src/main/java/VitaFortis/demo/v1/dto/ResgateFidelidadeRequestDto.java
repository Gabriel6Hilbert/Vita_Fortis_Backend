package VitaFortis.demo.v1.dto;

import VitaFortis.demo.v1.enums.TipoResgate;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResgateFidelidadeRequestDto {

    @NotNull
    private Long usuarioId;

    @NotNull
    private TipoResgate tipoResgate;

    private Long cupomId;
    private Long produtoId;

    @Min(0)
    private int pontosUtilizados;

    @jakarta.validation.constraints.AssertTrue(message = "Para CUPOM, informe cupomId; para PRODUTO, informe produtoId")
    public boolean isAlvoValido() {
        if (tipoResgate == null) return false;
        return switch (tipoResgate) {
            case CUPOM -> cupomId != null && produtoId == null;
            case PRODUTO -> produtoId != null && cupomId == null;
        };
    }
}