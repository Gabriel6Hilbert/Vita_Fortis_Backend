package VitaFortis.demo.v1.mapper;

import VitaFortis.demo.v1.dto.Usuario.UsuarioRequestDto;
import VitaFortis.demo.v1.dto.Usuario.UsuarioResponseDto;
import VitaFortis.demo.v1.entity.Usuario;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UsuarioMapper {


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "carrinho", ignore = true)
    @Mapping(target = "email", qualifiedByName = "normalizeEmail")
    @Mapping(target = "cpf", qualifiedByName = "onlyDigits")
    @Mapping(target = "telefone", qualifiedByName = "trimOrNull")
    Usuario toEntity(UsuarioRequestDto usuarioRequestDto);

    @Mapping(target = "tipoUsuario", source = "tipo")
    UsuarioResponseDto toResponseDto(Usuario entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "carrinho", ignore = true)
    @Mapping(target = "tipo", ignore = true)
    @Mapping(target = "senha", ignore = true)
    @Mapping(target = "email",  ignore = true)
    @Mapping(target = "cpf", qualifiedByName = "onlyDigits")
    @Mapping(target = "telefone", qualifiedByName = "trimOrNull")
    void updateEntityFromDto(UsuarioRequestDto dto, @MappingTarget Usuario entity);

    @Named("normalizeEmail")
    default String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    @Named("onlyDigits")
    default String onlyDigits(String s) {
        return s == null ? null : s.replaceAll("\\D+", "");
    }

    @Named("trimOrNull")
    default String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
