package VitaFortis.demo.v1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RecuperacaoSenhaConfirmacaoDto(
        @NotBlank String token,
        @NotBlank @Size(min = 8, max = 120) String novaSenha
) {}
