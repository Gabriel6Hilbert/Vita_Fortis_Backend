package VitaFortis.demo.v1.repository;
import VitaFortis.demo.v1.entity.MovimentacaoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque,Long>{
    List<MovimentacaoEstoque> findAllByProdutoIdOrderByCriadoEmDesc(Long produtoId);
}
