package VitaFortis.demo.v1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PerfilAtualizacaoDto(
        @NotBlank @Size(max = 120) String nome,
        @Size(max = 20) String telefone,
        boolean aceitaComunicacoes
) {
}
