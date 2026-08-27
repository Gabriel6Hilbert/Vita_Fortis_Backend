package VitaFortis.demo.v1.dto;

import VitaFortis.demo.v1.enums.CategoriaProduto;

public record CategoriaResumoDto(CategoriaProduto categoria, long quantidadeProdutos) {
}
