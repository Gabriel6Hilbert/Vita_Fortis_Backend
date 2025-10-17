package VitaFortis.demo.v1.mapper;

import VitaFortis.demo.v1.dto.Carrinho.CarrinhoRequestDto;
import VitaFortis.demo.v1.dto.Carrinho.CarrinhoResponseDto;
import VitaFortis.demo.v1.entity.Carrinho;
import VitaFortis.demo.v1.entity.CarrinhoItem;
import org.mapstruct.*;
import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = { CarrinhoItemMapper.class },
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CarrinhoMapper {



    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "itens", source = "carrinhoItens")
    Carrinho toEntity(CarrinhoRequestDto dto);

    @Mapping(target = "usuarioId", source = "usuario.id")
    @Mapping(target = "carrinhoItens", source = "itens")
    CarrinhoResponseDto toResponseDto(Carrinho entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "itens", ignore = true)
    void updateEntityFromDto(CarrinhoRequestDto dto, @MappingTarget Carrinho entity);

    @AfterMapping
    default void bindBackReference(@MappingTarget Carrinho carrinho) {
        List<CarrinhoItem> itens = carrinho.getItens();
        if (itens != null) {
            for (CarrinhoItem it : itens) {
                it.setCarrinho(carrinho);
            }
        }
    }
}
