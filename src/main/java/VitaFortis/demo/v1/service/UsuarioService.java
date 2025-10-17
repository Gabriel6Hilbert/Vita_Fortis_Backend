package VitaFortis.demo.v1.service;

import VitaFortis.demo.v1.dto.Usuario.UsuarioRequestDto;
import VitaFortis.demo.v1.dto.Usuario.UsuarioResponseDto;
import VitaFortis.demo.v1.entity.Usuario;
import VitaFortis.demo.v1.enums.TipoUsuario;
import VitaFortis.demo.v1.mapper.UsuarioMapper;
import VitaFortis.demo.v1.repository.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private UsuarioRepository usuarioRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private UsuarioMapper usuarioMapper;

    public UsuarioService (UsuarioRepository usuarioRepository, BCryptPasswordEncoder bCryptPasswordEncoder, UsuarioMapper usuarioMapper) {
        this.usuarioRepository = usuarioRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.usuarioMapper = usuarioMapper;
    }

    @Transactional
    public UsuarioResponseDto create(UsuarioRequestDto usuarioRequestDto) {
        final String emailNormalizado = usuarioRequestDto.getEmail().trim().toLowerCase();

        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new IllegalArgumentException("E-mail já cadastrado.");
        }

        Usuario usuario = usuarioMapper.toEntity(usuarioRequestDto);
        usuario.setEmail(emailNormalizado);
        usuario.setSenha(bCryptPasswordEncoder.encode(usuario.getSenha()));
        usuario.setTipo(TipoUsuario.CLIENTE);

        Usuario saved = usuarioRepository.save(usuario);
        return  usuarioMapper.toResponseDto(saved);
    }

    @Transactional
    public UsuarioResponseDto update(Long id, UsuarioRequestDto usuarioRequestDto) {
        Usuario usuario = usuarioRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado"));

        if (usuarioRequestDto.getEmail() != null && !usuarioRequestDto.getEmail().isBlank()) {
            String novoEmail = usuarioRequestDto.getEmail().trim().toLowerCase();
            if (!novoEmail.equals(usuario.getEmail()) && usuarioRepository.existsByEmail(novoEmail)) {
                throw new IllegalArgumentException("E-mail já cadastrado.");
            }
            usuario.setEmail(novoEmail);
        }

            usuarioMapper.updateEntityFromDto(usuarioRequestDto, usuario);
        if (usuarioRequestDto.getSenha() != null && !usuarioRequestDto.getSenha().isBlank()) {
            usuario.setSenha(bCryptPasswordEncoder.encode(usuarioRequestDto.getSenha()));
        }

        usuario.setAtivo(true);
        usuario.setTipo(TipoUsuario.CLIENTE);

        Usuario saved = usuarioRepository.save(usuario);
        return  usuarioMapper.toResponseDto(saved);
    }



}
