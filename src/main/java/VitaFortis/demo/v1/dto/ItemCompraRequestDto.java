package VitaFortis.demo.v1.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemCompraRequestDto {

    @NotNull
    private Long produtoId;

    @Min(1)
    private int quantidade;
}
