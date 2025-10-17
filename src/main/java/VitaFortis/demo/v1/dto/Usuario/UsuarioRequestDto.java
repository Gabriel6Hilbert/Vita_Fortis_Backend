package VitaFortis.demo.v1.dto.Usuario;

import VitaFortis.demo.v1.enums.TipoUsuario;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.br.CPF;

@Getter @Setter
public class UsuarioRequestDto {

    @NotBlank @Size(max = 120)
    private String nome;

    @NotBlank @Email @Size(max = 255)
    private String email;

    @NotBlank @Size(min = 8, max = 72)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String senha;

    @NotBlank
    @CPF(message = "CPF inválido")
    private String cpf;

    @Size(max = 20)
    private String telefone;

}