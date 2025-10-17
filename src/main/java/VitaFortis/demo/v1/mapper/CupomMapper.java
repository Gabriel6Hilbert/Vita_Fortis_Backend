package VitaFortis.demo.v1.mapper;

import VitaFortis.demo.v1.dto.CupomRequestDto;
import VitaFortis.demo.v1.dto.CupomResponseDto;
import VitaFortis.demo.v1.entity.Cupom;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CupomMapper {

    CupomResponseDto toDto(Cupom cupom);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    @Mapping(target = "dataCadastro", ignore = true)
    @Mapping(target = "codigo", qualifiedByName = "normalizeCodigo")
    Cupom toEntity(CupomRequestDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    @Mapping(target = "dataCadastro", ignore = true)
    @Mapping(target = "codigo", qualifiedByName = "normalizeCodigo")
    void updateFromDto(CupomRequestDto dto, @MappingTarget Cupom entity);

    @Named("normalizeCodigo")
    default String normalizeCodigo(String codigo) {
        return codigo == null ? null : codigo.trim().toUpperCase();
    }
}
