package VitaFortis.demo.v1.mapper;

import VitaFortis.demo.v1.dto.Carrinho.CarrinhoItemRequestDto;
import VitaFortis.demo.v1.dto.Carrinho.CarrinhoItemResponseDto;
import VitaFortis.demo.v1.entity.CarrinhoItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CarrinhoItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "carrinho", ignore = true)
    @Mapping(target = "produto.id", source = "produtoId")
    CarrinhoItem toEntity(CarrinhoItemRequestDto dto);

    @Mapping(target = "produtoId", source = "produto.id")
    @Mapping(target = "produtoNome", source = "produto.nome")
    CarrinhoItemResponseDto toResponseDto(CarrinhoItem entity);
}
