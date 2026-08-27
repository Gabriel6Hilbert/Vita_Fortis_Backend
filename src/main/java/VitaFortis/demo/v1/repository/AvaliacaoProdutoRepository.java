package VitaFortis.demo.v1.repository;
import VitaFortis.demo.v1.entity.AvaliacaoProduto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface AvaliacaoProdutoRepository extends JpaRepository<AvaliacaoProduto,Long> {
    List<AvaliacaoProduto> findByProdutoIdAndAprovadoTrueOrderByCriadoEmDesc(Long produtoId);
    Optional<AvaliacaoProduto> findByProdutoIdAndUsuarioId(Long produtoId, Long usuarioId);
}
