package VitaFortis.demo.v1.dto;

import VitaFortis.demo.v1.enums.TipoResgate;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ResgateFidelidadeResponseDto {
    private Long id;
    private Long usuarioId;
    private int pontosUtilizados;
    private LocalDate dataResgate;
    private TipoResgate tipoResgate;
    private String cupomCodigo;
    private String produtoNome;
}
