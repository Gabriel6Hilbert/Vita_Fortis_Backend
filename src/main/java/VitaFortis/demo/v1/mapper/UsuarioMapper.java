package VitaFortis.demo.v1.mapper;

import VitaFortis.demo.v1.dto.Usuario.UsuarioRequestDto;
import VitaFortis.demo.v1.dto.Usuario.UsuarioResponseDto;
import VitaFortis.demo.v1.entity.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UsuarioMapper{

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "carrinho", ignore = true)
    Usuario toEntity(UsuarioRequestDto usuarioRequestDto);

    UsuarioResponseDto toResponseDto(Usuario entity);
}
