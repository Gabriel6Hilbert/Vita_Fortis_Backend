package VitaFortis.demo.v1.entity;

import VitaFortis.demo.v1.enums.StatusCompra;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "Pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    private LocalDateTime dataPedido;
    private double total;

    @Enumerated(EnumType.STRING)
    private StatusCompra status;

    private int pontosGerados;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL)
    private List<ItemCompra> itens;

    @ManyToOne
    @JoinColumn(name = "cupom_id", nullable = false )
    private Cupom cupomUtilizado;
}
