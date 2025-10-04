package VitaFortis.demo.v1.mapper;

import VitaFortis.demo.v1.dto.CupomRequestDto;
import VitaFortis.demo.v1.dto.CupomResponseDto;
import VitaFortis.demo.v1.entity.Cupom;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CupomMapper {

    CupomResponseDto toDto(Cupom cupom);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    @Mapping(target = "dataCadastro", ignore = true)
    Cupom toEntity(CupomRequestDto dto);

}
