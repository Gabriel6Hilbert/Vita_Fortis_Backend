package VitaFortis.demo.v1.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class HomeObjetivoDto {
    private Long id;
    @NotBlank @Size(max=40) private String chave;
    @NotBlank @Size(max=80) private String titulo;
    @Size(max=500) private String imagemUrl;
    @NotBlank @Size(max=180) private String textoAlternativo;
    @Positive private int ordem;
    private boolean ativo;
}
