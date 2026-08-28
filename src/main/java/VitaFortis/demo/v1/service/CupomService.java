package VitaFortis.demo.v1.service;

import VitaFortis.demo.v1.dto.CupomRequestDto;
import VitaFortis.demo.v1.dto.CupomResponseDto;
import VitaFortis.demo.v1.entity.Cupom;
import VitaFortis.demo.v1.enums.CupomTipo;
import VitaFortis.demo.v1.mapper.CupomMapper;
import VitaFortis.demo.v1.repository.CupomRepository;
import VitaFortis.demo.v1.repository.UsuarioRepository;
import VitaFortis.demo.v1.enums.TipoUsuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CupomService {
    private final CupomRepository repository;
    private final CupomMapper mapper;
    private final UsuarioRepository usuarios;

    public CupomService(CupomRepository repository, CupomMapper mapper, UsuarioRepository usuarios) {
        this.repository = repository;
        this.mapper = mapper;
        this.usuarios = usuarios;
    }

    @Transactional
    public CupomResponseDto criar(CupomRequestDto dto) {
        String codigo = dto.getCodigo().trim().toUpperCase();
        if (repository.existsByCodigoIgnoreCase(codigo)) throw new IllegalArgumentException("Codigo de cupom ja cadastrado");
        validar(dto);
        Cupom cupom = mapper.toEntity(dto);
        cupom.setCodigo(codigo);
        cupom.setAtivo(true);
        vincularColaborador(cupom, dto.getColaboradorId());
        return mapper.toDto(repository.save(cupom));
    }

    @Transactional
    public CupomResponseDto atualizar(Long id, CupomRequestDto dto) {
        Cupom cupom = buscarEntidade(id);
        validar(dto);
        String codigo = dto.getCodigo().trim().toUpperCase();
        repository.findByCodigoIgnoreCase(codigo)
                .filter(outro -> !outro.getId().equals(id))
                .ifPresent(outro -> { throw new IllegalArgumentException("Codigo de cupom ja cadastrado"); });
        mapper.updateFromDto(dto, cupom);
        vincularColaborador(cupom, dto.getColaboradorId());
        return mapper.toDto(repository.save(cupom));
    }

    @Transactional(readOnly = true)
    public List<CupomResponseDto> listar() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    @Transactional
    public CupomResponseDto alterarAtivo(Long id, boolean ativo) {
        Cupom cupom = buscarEntidade(id);
        cupom.setAtivo(ativo);
        return mapper.toDto(repository.save(cupom));
    }

    private Cupom buscarEntidade(Long id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Cupom nao encontrado"));
    }

    private void validar(CupomRequestDto dto) {
        if (dto.getTipo() == CupomTipo.PERCENTUAL && dto.getDesconto().compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Desconto percentual nao pode ultrapassar 100%");
        }
        if (dto.getDataVencimento() != null && dto.getDataVencimento().isBefore(java.time.LocalDateTime.now())) {
            throw new IllegalArgumentException("Data de vencimento deve estar no futuro");
        }
    }

    private void vincularColaborador(Cupom cupom, Long colaboradorId) {
        if (colaboradorId == null) {
            cupom.setColaborador(null);
            cupom.setPercentualCashback(null);
            return;
        }
        if (cupom.getPercentualCashback() == null || cupom.getPercentualCashback().signum() <= 0) {
            throw new IllegalArgumentException("Percentual de cashback obrigatorio para cupom de colaborador");
        }
        var colaborador = usuarios.findById(colaboradorId)
                .orElseThrow(() -> new IllegalArgumentException("Colaborador nao encontrado"));
        if (colaborador.getTipo() != TipoUsuario.COLABORADOR) {
            throw new IllegalArgumentException("Usuario informado nao possui perfil COLABORADOR");
        }
        cupom.setColaborador(colaborador);
    }
}
