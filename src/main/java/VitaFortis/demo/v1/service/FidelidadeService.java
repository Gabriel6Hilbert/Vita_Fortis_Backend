package VitaFortis.demo.v1.service;

import VitaFortis.demo.v1.dto.*;
import VitaFortis.demo.v1.entity.*;
import VitaFortis.demo.v1.enums.TipoResgate;
import VitaFortis.demo.v1.mapper.ProdutoMapper;
import VitaFortis.demo.v1.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class FidelidadeService {
    private final UsuarioRepository usuarioRepository;
    private final ProdutoRepository produtoRepository;
    private final CupomRepository cupomRepository;
    private final ResgateFidelidadeRepository resgateRepository;
    private final ProdutoMapper produtoMapper;
    private final int metaDiamante;

    public FidelidadeService(UsuarioRepository usuarioRepository, ProdutoRepository produtoRepository,
                             CupomRepository cupomRepository, ResgateFidelidadeRepository resgateRepository,
                             ProdutoMapper produtoMapper,
                             @Value("${vitafortis.fidelidade.meta-diamante:5000}") int metaDiamante) {
        this.usuarioRepository = usuarioRepository;
        this.produtoRepository = produtoRepository;
        this.cupomRepository = cupomRepository;
        this.resgateRepository = resgateRepository;
        this.produtoMapper = produtoMapper;
        this.metaDiamante = metaDiamante;
    }

    @Transactional(readOnly = true)
    public FidelidadeSaldoDto saldo(Long usuarioId) {
        Usuario usuario = buscarUsuario(usuarioId);
        return new FidelidadeSaldoDto(usuarioId, usuario.getPontosFidelidade(),
                usuario.getPontosFidelidade() >= metaDiamante, metaDiamante);
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponseDto> produtosResgataveis() {
        return produtoRepository.findAll().stream()
                .filter(p -> p.isAtivo() && p.isResgatavel() && p.getQuantidadeEstoque() > 0)
                .map(produtoMapper::toResponseDto).toList();
    }

    @Transactional
    public ResgateFidelidadeResponseDto resgatar(ResgateFidelidadeRequestDto dto) {
        Usuario usuario = buscarUsuario(dto.getUsuarioId());
        RegateFidelidade resgate = new RegateFidelidade();
        resgate.setUsuario(usuario);
        resgate.setTipoResgate(dto.getTipoResgate());
        resgate.setDataResgate(LocalDate.now());
        int custo;
        if (dto.getTipoResgate() == TipoResgate.PRODUTO) {
            Produto produto = produtoRepository.findByIdAndAtivoTrue(dto.getProdutoId())
                    .filter(Produto::isResgatavel)
                    .orElseThrow(() -> new IllegalArgumentException("Produto indisponivel para resgate"));
            custo = produto.getPontosNecessarios();
            if (produtoRepository.debitarEstoque(produto.getId(), 1) == 0)
                throw new IllegalArgumentException("Produto sem estoque");
            resgate.setProdutoResgatado(produto);
        } else {
            Cupom cupom = cupomRepository.findById(dto.getCupomId())
                    .filter(Cupom::isAtivo)
                    .orElseThrow(() -> new IllegalArgumentException("Cupom indisponivel para resgate"));
            custo = dto.getPontosUtilizados();
            if (custo <= 0) throw new IllegalArgumentException("Informe os pontos necessarios para o cupom");
            resgate.setCupomResgatado(cupom);
        }
        if (usuario.getPontosFidelidade() < custo) throw new IllegalArgumentException("Saldo de pontos insuficiente");
        usuario.setPontosFidelidade(usuario.getPontosFidelidade() - custo);
        resgate.setPontosUtilizados(custo);
        return toDto(resgateRepository.save(resgate));
    }

    @Transactional(readOnly = true)
    public List<ResgateFidelidadeResponseDto> historico(Long usuarioId) {
        return resgateRepository.findAllByUsuarioIdOrderByDataResgateDesc(usuarioId).stream().map(this::toDto).toList();
    }

    private Usuario buscarUsuario(Long id) {
        return usuarioRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado"));
    }

    private ResgateFidelidadeResponseDto toDto(RegateFidelidade r) {
        ResgateFidelidadeResponseDto dto = new ResgateFidelidadeResponseDto();
        dto.setId(r.getId()); dto.setUsuarioId(r.getUsuario().getId());
        dto.setPontosUtilizados(r.getPontosUtilizados()); dto.setDataResgate(r.getDataResgate());
        dto.setTipoResgate(r.getTipoResgate());
        dto.setCupomCodigo(r.getCupomResgatado() == null ? null : r.getCupomResgatado().getCodigo());
        dto.setProdutoNome(r.getProdutoResgatado() == null ? null : r.getProdutoResgatado().getNome());
        return dto;
    }
}
