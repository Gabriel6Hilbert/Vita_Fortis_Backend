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

    ProdutoResponseDto toResponseDto(Produto produto);

    @Mapping(target = "produtoId", ignore = true)
    @Mapping(target = "versao", ignore = true)
    @Mapping(target = "ativo",ignore = true)
    @Mapping(target = "pontosNecessarios", source = "pontosNecessarios")
    @Mapping(target = "nome", qualifiedByName = "tromOrNull")
    Produto toEntity(ProdutoRequestDto produtoRequestDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "produtoId", ignore = true)
    @Mapping(target = "versao", ignore = true)
    @Mapping(target = "ativo", ignore = true)
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

    @AfterMapping
    default void ajustarPontosResgate (@MappingTarget Produto entity) {
        if (!entity.isResgatavel()) {
            entity.setPontosNecessarios(null);
        }
    }

}
