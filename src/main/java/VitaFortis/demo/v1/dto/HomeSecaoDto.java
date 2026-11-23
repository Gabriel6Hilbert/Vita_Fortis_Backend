package VitaFortis.demo.v1.dto;

import VitaFortis.demo.v1.entity.HomeSecao;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class HomeSecaoDto {
    private Long id;
    @NotBlank @Size(max = 40) private String chave;
    private boolean ativa;
    private int ordem;
    @NotNull private HomeSecao.Modo modo;
    @Size(max = 120) private String titulo;
    @Size(max = 500) private String subtitulo;
    @Size(max = 500) private String mediaUrl;
    @Size(max = 1000) private String texto;
    @Min(1) @Max(20) private int limite;
    @Size(max = 20) private List<Long> produtoIds = new ArrayList<>();
}
