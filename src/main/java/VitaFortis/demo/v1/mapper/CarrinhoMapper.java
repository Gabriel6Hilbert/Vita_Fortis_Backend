package VitaFortis.demo.v1.mapper;

import VitaFortis.demo.v1.dto.Carrinho.CarrinhoRequestDto;
import VitaFortis.demo.v1.dto.Carrinho.CarrinhoResponseDto;
import VitaFortis.demo.v1.entity.Carrinho;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {CarrinhoItemMapper.class})
public interface CarrinhoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario.id", source = "usuarioId")
    Carrinho toEntity(CarrinhoRequestDto dto);

    @Mapping(target = "usuarioId", source = "usuario.id")
    CarrinhoResponseDto toResponseDto(Carrinho entity);
}
