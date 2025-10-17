package VitaFortis.demo.v1.dto.Usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UsuarioLoginDto {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String senha;
}
