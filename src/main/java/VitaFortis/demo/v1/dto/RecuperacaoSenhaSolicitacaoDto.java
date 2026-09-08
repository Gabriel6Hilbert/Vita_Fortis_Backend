package VitaFortis.demo.v1.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RecuperacaoSenhaSolicitacaoDto(@NotBlank @Email String email) {}
