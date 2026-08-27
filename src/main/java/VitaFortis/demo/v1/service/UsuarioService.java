package VitaFortis.demo.v1.service;

import VitaFortis.demo.v1.dto.Usuario.UsuarioRequestDto;
import VitaFortis.demo.v1.dto.Usuario.UsuarioResponseDto;
import VitaFortis.demo.v1.entity.Usuario;
import VitaFortis.demo.v1.enums.TipoUsuario;
import VitaFortis.demo.v1.mapper.UsuarioMapper;
import VitaFortis.demo.v1.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private UsuarioMapper usuarioMapper;

    public UsuarioService (UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, UsuarioMapper usuarioMapper) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
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
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
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

            usuarioMapper.updateFromDto(usuarioRequestDto, usuario);
        if (usuarioRequestDto.getSenha() != null && !usuarioRequestDto.getSenha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(usuarioRequestDto.getSenha()));
        }

        Usuario saved = usuarioRepository.save(usuario);
        return  usuarioMapper.toResponseDto(saved);
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDto buscar(Long id) {
        return usuarioMapper.toResponseDto(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDto buscarPorEmail(String email) {
        return usuarioMapper.toResponseDto(usuarioRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado")));
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDto> listar() {
        return usuarioRepository.findAll().stream().map(usuarioMapper::toResponseDto).toList();
    }

    @Transactional
    public UsuarioResponseDto alterarTipo(Long id, TipoUsuario tipo) {
        Usuario usuario = buscarEntidade(id);
        usuario.setTipo(tipo);
        return usuarioMapper.toResponseDto(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponseDto alterarAtivo(Long id, boolean ativo) {
        Usuario usuario = buscarEntidade(id);
        usuario.setAtivo(ativo);
        return usuarioMapper.toResponseDto(usuarioRepository.save(usuario));
    }

    private Usuario buscarEntidade(Long id) {
        return usuarioRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado"));
    }



}
