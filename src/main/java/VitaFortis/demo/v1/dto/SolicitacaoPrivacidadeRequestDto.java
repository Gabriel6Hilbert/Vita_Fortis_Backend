package VitaFortis.demo.v1.dto;

import VitaFortis.demo.v1.enums.TipoSolicitacaoPrivacidade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SolicitacaoPrivacidadeRequestDto(
        @NotNull TipoSolicitacaoPrivacidade tipo,
        @NotBlank @Size(max = 1000) String descricao
) { }
