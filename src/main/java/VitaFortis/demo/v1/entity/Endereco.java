package VitaFortis.demo.v1.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "ENDERECO")
public class Endereco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ENDERECO_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USUARIO_ID", nullable = false)
    private Usuario usuario;

    @Size(max = 60)
    @Column(name = "APELIDO", length = 60)
    private String apelido;

    @NotBlank
    @Size(min = 8, max = 8)
    @Column(name = "CEP", nullable = false, length = 8)
    private String cep;

    @NotBlank
    @Size(max = 160)
    @Column(name = "LOGRADOURO", nullable = false, length = 160)
    private String logradouro;

    @NotBlank
    @Size(max = 20)
    @Column(name = "NUMERO", nullable = false, length = 20)
    private String numero;

    @Size(max = 100)
    @Column(name = "COMPLEMENTO", length = 100)
    private String complemento;

    @NotBlank
    @Size(max = 100)
    @Column(name = "BAIRRO", nullable = false, length = 100)
    private String bairro;

    @NotBlank
    @Size(max = 100)
    @Column(name = "CIDADE", nullable = false, length = 100)
    private String cidade;

    @NotBlank
    @Size(min = 2, max = 2)
    @Column(name = "UF", nullable = false, length = 2)
    private String uf;

    @Column(name = "PRINCIPAL", nullable = false)
    private boolean principal;

    @Column(name = "ATIVO", nullable = false)
    private boolean ativo = true;

    @CreationTimestamp
    @Column(name = "CRIADO_EM", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "ATUALIZADO_EM", nullable = false)
    private LocalDateTime atualizadoEm;

    public String formatado() {
        String complementoFormatado = complemento == null || complemento.isBlank()
                ? ""
                : ", " + complemento.trim();
        return "%s, %s%s - %s, %s/%s - CEP %s".formatted(
                logradouro, numero, complementoFormatado, bairro, cidade, uf, cep);
    }
}
