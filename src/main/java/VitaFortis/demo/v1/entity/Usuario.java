package VitaFortis.demo.v1.entity;

import VitaFortis.demo.v1.enums.TipoUsuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "Usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @Column(unique = true)
    private String email;

    private String senha;

    @Column(unique = true)
    private String cpf;

    private String telefone;
    private int pontosFidelidade;

    @Enumerated(EnumType.STRING)
    private TipoUsuario tipo;

    private boolean ativo = true;
}
