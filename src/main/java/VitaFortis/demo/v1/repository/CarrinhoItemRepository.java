package VitaFortis.demo.v1.repository;

import VitaFortis.demo.v1.entity.CarrinhoItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CarrinhoItemRepository extends JpaRepository<CarrinhoItem,Long> {
    Optional<CarrinhoItem> findByCarrinhoIdAndProdutoId(Long carrinhoId, Long produtoId);
    List<CarrinhoItem> findAllByCarrinhoId(Long carrinhoId);

}
