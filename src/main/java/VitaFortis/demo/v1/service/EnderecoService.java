package VitaFortis.demo.v1.service;

import VitaFortis.demo.v1.dto.EnderecoRequestDto;
import VitaFortis.demo.v1.dto.EnderecoResponseDto;
import VitaFortis.demo.v1.entity.Endereco;
import VitaFortis.demo.v1.entity.Usuario;
import VitaFortis.demo.v1.repository.EnderecoRepository;
import VitaFortis.demo.v1.repository.UsuarioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EnderecoService {

    private final EnderecoRepository enderecos;
    private final UsuarioRepository usuarios;

    public EnderecoService(EnderecoRepository enderecos, UsuarioRepository usuarios) {
        this.enderecos = enderecos;
        this.usuarios = usuarios;
    }

    @Transactional(readOnly = true)
    public List<EnderecoResponseDto> listar(Long usuarioId, String emailAutenticado) {
        validarProprietario(usuarioId, emailAutenticado);
        return enderecos.findAllByUsuarioIdAndAtivoTrueOrderByPrincipalDescIdAsc(usuarioId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public EnderecoResponseDto criar(Long usuarioId, EnderecoRequestDto dto, String emailAutenticado) {
        Usuario usuario = validarProprietario(usuarioId, emailAutenticado);
        Endereco endereco = new Endereco();
        endereco.setUsuario(usuario);
        aplicar(endereco, dto);
        ajustarPrincipal(usuarioId, endereco, dto.principal());
        return toDto(enderecos.save(endereco));
    }

    @Transactional
    public EnderecoResponseDto atualizar(Long usuarioId, Long enderecoId, EnderecoRequestDto dto,
                                         String emailAutenticado) {
        validarProprietario(usuarioId, emailAutenticado);
        Endereco endereco = buscar(usuarioId, enderecoId);
        aplicar(endereco, dto);
        ajustarPrincipal(usuarioId, endereco, dto.principal());
        return toDto(enderecos.save(endereco));
    }

    @Transactional
    public void remover(Long usuarioId, Long enderecoId, String emailAutenticado) {
        validarProprietario(usuarioId, emailAutenticado);
        Endereco endereco = buscar(usuarioId, enderecoId);
        endereco.setAtivo(false);
        endereco.setPrincipal(false);
        enderecos.save(endereco);
    }

    @Transactional(readOnly = true)
    public Endereco buscarParaCheckout(Long usuarioId, Long enderecoId) {
        return buscar(usuarioId, enderecoId);
    }

    private Usuario validarProprietario(Long usuarioId, String emailAutenticado) {
        Usuario usuario = usuarios.findByEmail(emailAutenticado.trim().toLowerCase())
                .orElseThrow(() -> new AccessDeniedException("Usuario nao autenticado"));
        if (!usuario.getId().equals(usuarioId)) {
            throw new AccessDeniedException("Acesso negado ao endereco");
        }
        return usuario;
    }

    private Endereco buscar(Long usuarioId, Long enderecoId) {
        return enderecos.findByIdAndUsuarioIdAndAtivoTrue(enderecoId, usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Endereco nao encontrado"));
    }

    private void ajustarPrincipal(Long usuarioId, Endereco atual, boolean tornarPrincipal) {
        List<Endereco> cadastrados = enderecos.findAllByUsuarioIdAndAtivoTrueOrderByPrincipalDescIdAsc(usuarioId);
        boolean primeiroEndereco = cadastrados.isEmpty();
        if (tornarPrincipal || primeiroEndereco) {
            cadastrados.stream()
                    .filter(endereco -> !endereco.getId().equals(atual.getId()))
                    .forEach(endereco -> endereco.setPrincipal(false));
            atual.setPrincipal(true);
        }
    }

    private void aplicar(Endereco endereco, EnderecoRequestDto dto) {
        endereco.setApelido(normalizarOpcional(dto.apelido()));
        endereco.setCep(dto.cep().replaceAll("\\D", ""));
        endereco.setLogradouro(dto.logradouro().trim());
        endereco.setNumero(dto.numero().trim());
        endereco.setComplemento(normalizarOpcional(dto.complemento()));
        endereco.setBairro(dto.bairro().trim());
        endereco.setCidade(dto.cidade().trim());
        endereco.setUf(dto.uf().trim().toUpperCase());
    }

    private String normalizarOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private EnderecoResponseDto toDto(Endereco endereco) {
        return new EnderecoResponseDto(
                endereco.getId(), endereco.getApelido(), endereco.getCep(), endereco.getLogradouro(),
                endereco.getNumero(), endereco.getComplemento(), endereco.getBairro(), endereco.getCidade(),
                endereco.getUf(), endereco.isPrincipal());
    }
}
