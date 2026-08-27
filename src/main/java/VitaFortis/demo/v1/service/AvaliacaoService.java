package VitaFortis.demo.v1.service;
import VitaFortis.demo.v1.dto.*;
import VitaFortis.demo.v1.entity.*;
import VitaFortis.demo.v1.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.*;
import java.util.List;

@Service public class AvaliacaoService {
    private final AvaliacaoProdutoRepository avaliacoes; private final ProdutoRepository produtos; private final UsuarioRepository usuarios;
    public AvaliacaoService(AvaliacaoProdutoRepository a, ProdutoRepository p, UsuarioRepository u){avaliacoes=a;produtos=p;usuarios=u;}
    @Transactional(readOnly=true) public List<AvaliacaoResponseDto> listar(Long produtoId){return avaliacoes.findByProdutoIdAndAprovadoTrueOrderByCriadoEmDesc(produtoId).stream().map(this::dto).toList();}
    @Transactional public AvaliacaoResponseDto criar(Long produtoId, AvaliacaoRequestDto req, String email){
        Usuario u=usuarios.findById(req.getUsuarioId()).orElseThrow(()->new IllegalArgumentException("Usuario nao encontrado"));
        if(!u.getEmail().equalsIgnoreCase(email)) throw new org.springframework.security.access.AccessDeniedException("Somente o proprio usuario pode avaliar");
        Produto p=produtos.findByIdAndAtivoTrue(produtoId).orElseThrow(()->new IllegalArgumentException("Produto nao encontrado"));
        AvaliacaoProduto a=avaliacoes.findByProdutoIdAndUsuarioId(produtoId,u.getId()).orElseGet(AvaliacaoProduto::new);
        a.setProduto(p);a.setUsuario(u);a.setNota(req.getNota());a.setComentario(req.getComentario());a.setAprovado(true);
        a=avaliacoes.save(a); recalcular(p); return dto(a);
    }
    @Transactional public AvaliacaoResponseDto moderar(Long id, boolean aprovado){AvaliacaoProduto a=avaliacoes.findById(id).orElseThrow(()->new IllegalArgumentException("Avaliacao nao encontrada"));a.setAprovado(aprovado);a=avaliacoes.save(a);recalcular(a.getProduto());return dto(a);}
    private void recalcular(Produto p){var todas=avaliacoes.findByProdutoIdAndAprovadoTrueOrderByCriadoEmDesc(p.getId());p.setAvaliacaoMedia(todas.isEmpty()?null:BigDecimal.valueOf(todas.stream().mapToInt(AvaliacaoProduto::getNota).average().orElse(0)).setScale(2,RoundingMode.HALF_UP));produtos.save(p);}
    private AvaliacaoResponseDto dto(AvaliacaoProduto a){return new AvaliacaoResponseDto(a.getId(),a.getProduto().getId(),a.getUsuario().getId(),a.getUsuario().getNome(),a.getNota(),a.getComentario(),a.isAprovado(),a.getCriadoEm());}
}
