package VitaFortis.demo.v1.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Entity @Getter @Setter
@Table(name = "catalogo_cadastro", uniqueConstraints = @UniqueConstraint(columnNames = {"tipo", "codigo"}))
public class CatalogoCadastro {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank @Pattern(regexp = "CATEGORIA|ATRIBUTO") @Column(nullable = false, length = 20)
    private String tipo;
    @NotBlank @Pattern(regexp = "[A-Z0-9_]{1,40}") @Column(nullable = false, length = 40)
    private String codigo;
    @NotBlank @Size(max = 120) @Column(nullable = false, length = 120)
    private String nome;
    @Column(nullable = false)
    private boolean ativo = true;
}
