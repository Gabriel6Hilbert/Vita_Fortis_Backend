package VitaFortis.demo.v1.entity;

import VitaFortis.demo.v1.enums.TipoUsuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "USUARIO",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "EMAIL"),
                @UniqueConstraint(columnNames = "CPF")
        }
)
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USUARIO_ID")
    private Long id;

    @NotBlank
    @Size(max = 120)
    @Column(name = "NOME", nullable = false, length = 120)
    private String nome;

    @NotBlank
    @Email
    @Size(max = 255)
    @Column(name = "EMAIL", nullable = false, length = 255)
    private String email;

    @NotBlank
    @Size(max = 60)
    @Column(name = "SENHA", nullable = false, length = 60)
    private String senha;

    @NotBlank
    @Column(name = "CPF", nullable = false, length = 11)
    private String cpf;

    @Size(max = 20)
    @Column(name = "TELEFONE", length = 20)
    private String telefone;

    @Min(0)
    @Column(name = "PONTOS_FIDELIDADE", nullable = false, columnDefinition = "INT DEFAULT 0")
    private int pontosFidelidade = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO_USUARIO", nullable = false, length = 30)
    private TipoUsuario tipo;

    @Column(name = "ATIVO", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private boolean ativo = true;

    @CreationTimestamp
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    public Usuario(Long usuarioId) {

    }

    @PrePersist
    void prePersist() {
        if (tipo == null)  tipo = TipoUsuario.CLIENTE;
        ativo = true;
    }

}
