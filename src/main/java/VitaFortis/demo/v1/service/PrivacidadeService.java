package VitaFortis.demo.v1.service;

import VitaFortis.demo.v1.dto.*;
import VitaFortis.demo.v1.entity.AuditoriaSolicitacaoPrivacidade;
import VitaFortis.demo.v1.entity.SolicitacaoPrivacidade;
import VitaFortis.demo.v1.entity.Usuario;
import VitaFortis.demo.v1.enums.StatusSolicitacaoPrivacidade;
import VitaFortis.demo.v1.enums.TipoUsuario;
import VitaFortis.demo.v1.repository.AuditoriaSolicitacaoPrivacidadeRepository;
import VitaFortis.demo.v1.repository.SolicitacaoPrivacidadeRepository;
import VitaFortis.demo.v1.repository.UsuarioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class PrivacidadeService {
    private final UsuarioRepository usuarios;
    private final SolicitacaoPrivacidadeRepository solicitacoes;
    private final AuditoriaSolicitacaoPrivacidadeRepository auditorias;

    public PrivacidadeService(UsuarioRepository usuarios,
                              SolicitacaoPrivacidadeRepository solicitacoes,
                              AuditoriaSolicitacaoPrivacidadeRepository auditorias) {
        this.usuarios = usuarios;
        this.solicitacoes = solicitacoes;
        this.auditorias = auditorias;
    }

    @Transactional(readOnly = true)
    public ConsentimentoComunicacaoDto consultarConsentimento(String email) {
        return new ConsentimentoComunicacaoDto(cliente(email).isAceitaComunicacoes());
    }

    @Transactional
    public ConsentimentoComunicacaoDto alterarConsentimento(String email, ConsentimentoComunicacaoDto dto) {
        Usuario usuario = cliente(email);
        usuario.setAceitaComunicacoes(dto.aceitaComunicacoes());
        usuarios.save(usuario);
        return new ConsentimentoComunicacaoDto(usuario.isAceitaComunicacoes());
    }

    @Transactional
    public SolicitacaoPrivacidadeResponseDto abrir(String email, SolicitacaoPrivacidadeRequestDto dto) {
        Usuario usuario = cliente(email);
        SolicitacaoPrivacidade solicitacao = new SolicitacaoPrivacidade();
        solicitacao.setUsuario(usuario);
        solicitacao.setProtocolo(novoProtocolo());
        solicitacao.setTipo(dto.tipo());
        solicitacao.setDescricao(dto.descricao().trim());
        solicitacao.setStatus(StatusSolicitacaoPrivacidade.PENDENTE);
        return clienteDto(solicitacoes.save(solicitacao));
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoPrivacidadeResponseDto> listarProprias(String email) {
        Usuario usuario = cliente(email);
        return solicitacoes.findAllByUsuarioIdOrderByCriadaEmDesc(usuario.getId()).stream()
                .map(this::clienteDto).toList();
    }

    @Transactional(readOnly = true)
    public SolicitacaoPrivacidadeResponseDto buscarPropria(Long id, String email) {
        Usuario usuario = cliente(email);
        return solicitacoes.findByIdAndUsuarioId(id, usuario.getId()).map(this::clienteDto)
                .orElseThrow(() -> new AccessDeniedException("Solicitacao nao pertence ao cliente autenticado"));
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoPrivacidadeAdminDto> listarAdministracao() {
        return solicitacoes.findAllByOrderByCriadaEmDesc().stream().map(this::adminDto).toList();
    }

    @Transactional
    public SolicitacaoPrivacidadeAdminDto processar(Long id, ProcessamentoPrivacidadeRequestDto dto, String responsavel) {
        if (dto.status() == StatusSolicitacaoPrivacidade.PENDENTE) {
            throw new IllegalArgumentException("O processamento administrativo nao pode retornar a solicitacao para PENDENTE");
        }
        SolicitacaoPrivacidade solicitacao = solicitacoes.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitacao de privacidade nao encontrada"));
        StatusSolicitacaoPrivacidade anterior = solicitacao.getStatus();
        solicitacao.setStatus(dto.status());
        solicitacao.setDecisao(dto.decisao().trim());
        solicitacao.setJustificativaAdmin(dto.justificativa().trim());
        solicitacao.setResponsavel(responsavel);
        solicitacao.setProcessadaEm(LocalDateTime.now());
        solicitacoes.save(solicitacao);

        AuditoriaSolicitacaoPrivacidade auditoria = new AuditoriaSolicitacaoPrivacidade();
        auditoria.setSolicitacao(solicitacao);
        auditoria.setStatusAnterior(anterior);
        auditoria.setStatusNovo(dto.status());
        auditoria.setDecisao(dto.decisao().trim());
        auditoria.setJustificativa(dto.justificativa().trim());
        auditoria.setResponsavel(responsavel);
        auditorias.save(auditoria);
        return adminDto(solicitacao);
    }

    private Usuario cliente(String email) {
        Usuario usuario = usuarios.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new AccessDeniedException("Usuario autenticado nao encontrado"));
        if (usuario.getTipo() != TipoUsuario.CLIENTE) {
            throw new AccessDeniedException("Recurso exclusivo do cliente");
        }
        return usuario;
    }

    private String novoProtocolo() {
        return "RCP-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private SolicitacaoPrivacidadeResponseDto clienteDto(SolicitacaoPrivacidade s) {
        return new SolicitacaoPrivacidadeResponseDto(s.getId(), s.getProtocolo(), s.getTipo(), s.getDescricao(),
                s.getStatus(), s.getCriadaEm(), s.getDecisao(), s.getJustificativaAdmin(), s.getProcessadaEm());
    }

    private SolicitacaoPrivacidadeAdminDto adminDto(SolicitacaoPrivacidade s) {
        return new SolicitacaoPrivacidadeAdminDto(s.getId(), s.getProtocolo(), s.getUsuario().getId(),
                s.getUsuario().getNome(), s.getUsuario().getEmail(), s.getTipo(), s.getDescricao(), s.getStatus(),
                s.getCriadaEm(), s.getDecisao(), s.getResponsavel(), s.getProcessadaEm(), s.getJustificativaAdmin());
    }
}
