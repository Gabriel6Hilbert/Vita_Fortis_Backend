package VitaFortis.demo.v1.dto;

import VitaFortis.demo.v1.enums.TipoResgate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResgateFidelidadeRequestDto {
    private Long usuarioId;
    private TipoResgate tipoResgate;
    private Long cupomId;
    private Long produtoId;
}