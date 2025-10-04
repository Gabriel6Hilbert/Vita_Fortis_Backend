package VitaFortis.demo.v1.dto.Usuario;

import VitaFortis.demo.v1.enums.TipoUsuario;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioRequestDto {

    private String nome;
    private String email;
    private String senha;
    private String cpf;
    private String telefone;
    private TipoUsuario tipoUsuario;
}
