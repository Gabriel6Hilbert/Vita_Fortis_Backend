package VitaFortis.demo.v1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "Cupom")
public class Cupom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String codigo;
    private String descricao;
    private double desconto;

    private boolean ativo = true;

    private LocalDateTime dataCadastro;
    private LocalDateTime dataVencimento;
}
