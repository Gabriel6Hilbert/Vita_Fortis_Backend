package VitaFortis.demo.v1.dto;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
@Getter @Setter public class AvaliacaoRequestDto {
    @NotNull private Long usuarioId;
    @Min(1) @Max(5) private int nota;
    @Size(max=1000) private String comentario;
}
