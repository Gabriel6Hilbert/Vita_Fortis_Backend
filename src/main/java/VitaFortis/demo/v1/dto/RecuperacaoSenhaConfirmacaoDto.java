package VitaFortis.demo.v1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RecuperacaoSenhaConfirmacaoDto(
        @NotBlank @Pattern(regexp = "^[A-Za-z0-9_-]{43}$", message = "Token de recuperação inválido") String token,
        @NotBlank @Size(min = 8, max = 72) String novaSenha
) {}
