package VitaFortis.demo.v1.service;
import VitaFortis.demo.v1.dto.ProdutoResponseDto; import VitaFortis.demo.v1.entity.*; import VitaFortis.demo.v1.mapper.ProdutoMapper; import VitaFortis.demo.v1.repository.*;
import org.springframework.security.access.AccessDeniedException; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.util.List;
@Service public class FavoritoService {
 private final FavoritoRepository favoritos; private final UsuarioRepository usuarios; private final ProdutoRepository produtos; private final ProdutoMapper mapper;
 public FavoritoService(FavoritoRepository f,UsuarioRepository u,ProdutoRepository p,ProdutoMapper m){favoritos=f;usuarios=u;produtos=p;mapper=m;}
 private Usuario autorizado(Long id,String email){Usuario u=usuarios.findById(id).orElseThrow(()->new IllegalArgumentException("Usuario nao encontrado"));Usuario atual=usuarios.findByEmail(email).orElseThrow(()->new AccessDeniedException("Usuario nao autenticado"));if(!atual.getId().equals(id)&&!atual.getTipo().name().equals("ADMIN"))throw new AccessDeniedException("Acesso negado");return u;}
 @Transactional(readOnly=true) public List<ProdutoResponseDto> listar(Long id,String email){autorizado(id,email);return favoritos.findByUsuarioIdOrderByCriadoEmDesc(id).stream().map(Favorito::getProduto).map(mapper::toResponseDto).toList();}
 @Transactional public ProdutoResponseDto adicionar(Long id,Long produtoId,String email){Usuario u=autorizado(id,email);Produto p=produtos.findByIdAndAtivoTrue(produtoId).orElseThrow(()->new IllegalArgumentException("Produto nao encontrado"));favoritos.findByUsuarioIdAndProdutoId(id,produtoId).orElseGet(()->{Favorito f=new Favorito();f.setUsuario(u);f.setProduto(p);return favoritos.save(f);});return mapper.toResponseDto(p);}
 @Transactional public void remover(Long id,Long produtoId,String email){autorizado(id,email);favoritos.findByUsuarioIdAndProdutoId(id,produtoId).ifPresent(favoritos::delete);}
}
