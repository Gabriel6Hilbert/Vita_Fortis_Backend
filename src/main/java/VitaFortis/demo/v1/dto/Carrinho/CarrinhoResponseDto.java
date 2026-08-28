package VitaFortis.demo.v1.dto.Carrinho;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.math.BigDecimal;

@Getter
@Setter
public class CarrinhoResponseDto {

    private Long carrinhoId;
    private Long usuarioId;
    private List<CarrinhoItemResponseDto> carrinhoItens;
    private BigDecimal subtotal;
    private BigDecimal descontos;
    private BigDecimal total;
    private String cupomCodigo;
    private Long cupomId;
    private String colaboradorNome;
    private BigDecimal cashbackColaborador;
}
