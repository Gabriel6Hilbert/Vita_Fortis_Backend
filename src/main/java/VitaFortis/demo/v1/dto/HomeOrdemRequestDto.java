package VitaFortis.demo.v1.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record HomeOrdemRequestDto(@NotEmpty List<@Valid Item> secoes) {
    public record Item(@NotBlank String chave, @Positive int posicao) {}
}
