package VitaFortis.demo.v1.mapper;

import VitaFortis.demo.v1.dto.Carrinho.CarrinhoItemRequestDto;
import VitaFortis.demo.v1.dto.Carrinho.CarrinhoItemResponseDto;
import VitaFortis.demo.v1.entity.CarrinhoItem;
import VitaFortis.demo.v1.entity.Produto;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CarrinhoItemMapper {

    @Mapping(target = "itemId", ignore = true)
    @Mapping(target = "carrinho", ignore = true)
    @Mapping(target = "produto", source = "produtoId", qualifiedByName = "toProduto")
    @Mapping(target = "quantidade", source = "quantidade")
    CarrinhoItem toEntity(CarrinhoItemRequestDto dto);

    @Mapping(source = "itemId", target = "itemId")
    @Mapping(source = "produto.id",  target = "produtoId")
    @Mapping(source = "produto.nome", target = "produtoNome")
    @Mapping(source = "quantidade", target = "quantidade")
    @Mapping(target = "subtotal", expression = "java(entity.getPrecoUnitario().multiply(java.math.BigDecimal.valueOf(entity.getQuantidade())))")
    CarrinhoItemResponseDto toResponseDto(CarrinhoItem entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "itemId", ignore = true)
    @Mapping(target = "carrinho", ignore = true)
    @Mapping(source = "produtoId", target = "produto", qualifiedByName = "toProduto")
    @Mapping(source = "quantidade", target = "quantidade")
    void updateEntityFromDto(CarrinhoItemRequestDto dto, @MappingTarget CarrinhoItem entity);

    @Named("toProduto")
    default Produto toProduto(Long id) {
        if (id == null) return null;
        Produto p = new Produto();
        p.setId(id);
        return p;
    }
}
