package VitaFortis.demo.v1.dto.Usuario;

import VitaFortis.demo.v1.enums.TipoUsuario;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UsuarioResponseDto {

    private Long id;
    private String nome;
    private String email;
    private String cpf;
    private String telefone;
    private TipoUsuario tipoUsuario;
    private boolean ativo;
    private boolean aceitaComunicacoes;
    private boolean permissaoRelatorios;
    private java.math.BigDecimal saldoCashback;
}
