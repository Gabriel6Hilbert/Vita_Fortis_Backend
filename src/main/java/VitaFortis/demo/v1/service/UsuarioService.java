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
import VitaFortis.demo.v1.dto.PerfilAtualizacaoDto;
import VitaFortis.demo.v1.dto.AlteracaoSenhaDto;

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
        return criarComTipo(usuarioRequestDto, TipoUsuario.CLIENTE);
    }

    @Transactional
    public UsuarioResponseDto criarColaborador(UsuarioRequestDto usuarioRequestDto) {
        return criarComTipo(usuarioRequestDto, TipoUsuario.COLABORADOR);
    }

    private UsuarioResponseDto criarComTipo(UsuarioRequestDto usuarioRequestDto, TipoUsuario tipo) {
        final String emailNormalizado = usuarioRequestDto.getEmail().trim().toLowerCase();

        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new IllegalArgumentException("E-mail já cadastrado.");
        }

        Usuario usuario = usuarioMapper.toEntity(usuarioRequestDto);
        usuario.setEmail(emailNormalizado);
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        usuario.setTipo(tipo);

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

    @Transactional
    public UsuarioResponseDto alterarPermissaoRelatorios(Long id, boolean valor) {
        Usuario usuario = buscarEntidade(id);
        if (usuario.getTipo() != TipoUsuario.COLABORADOR) {
            throw new IllegalArgumentException("Permissao de relatorios se aplica apenas a colaborador");
        }
        usuario.setPermissaoRelatorios(valor);
        return usuarioMapper.toResponseDto(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponseDto atualizarPerfil(String email, PerfilAtualizacaoDto dto) {
        Usuario usuario = buscarPorEmailEntidade(email);
        usuario.setNome(dto.nome().trim());
        usuario.setTelefone(dto.telefone() == null || dto.telefone().isBlank() ? null : dto.telefone().trim());
        usuario.setAceitaComunicacoes(dto.aceitaComunicacoes());
        return usuarioMapper.toResponseDto(usuarioRepository.save(usuario));
    }

    @Transactional
    public void alterarSenha(String email, AlteracaoSenhaDto dto) {
        Usuario usuario = buscarPorEmailEntidade(email);
        if (!passwordEncoder.matches(dto.senhaAtual(), usuario.getSenha())) {
            throw new IllegalArgumentException("Senha atual invalida");
        }
        usuario.setSenha(passwordEncoder.encode(dto.novaSenha()));
        usuarioRepository.save(usuario);
    }

    private Usuario buscarPorEmailEntidade(String email) {
        return usuarioRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado"));
    }

    private Usuario buscarEntidade(Long id) {
        return usuarioRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado"));
    }



}
