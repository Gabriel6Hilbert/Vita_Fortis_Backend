package VitaFortis.demo.v1.mapper;

import VitaFortis.demo.v1.dto.ProdutoRequestDto;
import VitaFortis.demo.v1.dto.ProdutoResponseDto;
import VitaFortis.demo.v1.entity.Produto;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ProdutoMapper {

    @Mapping(target = "valorDesconto", expression = "java(produto.getValorDesconto())")
    @Mapping(target = "precoFinal",    expression = "java(produto.getPrecoFinal())")
    ProdutoResponseDto toResponseDto(Produto produto);

    /**
     * Cards e listagens públicas não exibem as coleções comerciais. Evitar
     * inicializá-las elimina consultas extras para cada página do catálogo;
     * o endpoint de detalhe continua usando o mapeamento completo acima.
     */
    @Mapping(target = "valorDesconto", expression = "java(produto.getValorDesconto())")
    @Mapping(target = "precoFinal", expression = "java(produto.getPrecoFinal())")
    @Mapping(target = "atributos", expression = "java(java.util.Map.of())")
    @Mapping(target = "objetivos", expression = "java(java.util.Set.of())")
    @Mapping(target = "esportes", expression = "java(java.util.Set.of())")
    @Mapping(target = "imagensAdicionais", ignore = true)
    ProdutoResponseDto toCatalogResponseDto(Produto produto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "versao", ignore = true)
    @Mapping(target = "ativo", expression = "java(produtoRequestDto.getAtivo() == null || produtoRequestDto.getAtivo())")
    @Mapping(target = "nome", qualifiedByName = "trimOrNull")
    @Mapping(target = "descricao", qualifiedByName = "trimOrNull")
    Produto toEntity(ProdutoRequestDto produtoRequestDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "versao", ignore = true)
    @Mapping(target = "ativo", expression = "java(dto.getAtivo() == null ? entity.isAtivo() : dto.getAtivo())")
    @Mapping(target = "nome", qualifiedByName = "trimOrNull")
    @Mapping(target = "descricao", qualifiedByName = "trimOrNull")
    void updateFromDto(ProdutoRequestDto dto, @MappingTarget Produto entity);

    /* === HELPERS ===*/
    @Named("trimOrNull")
    default String trimOrNull(String value) {
        if (value == null)return null;
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }

}
