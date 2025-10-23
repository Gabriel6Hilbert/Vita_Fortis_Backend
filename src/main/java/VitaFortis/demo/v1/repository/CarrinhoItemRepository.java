package VitaFortis.demo.v1.repository;

import VitaFortis.demo.v1.entity.CarrinhoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface CarrinhoItemRepository extends JpaRepository<CarrinhoItem,Long> {
    Optional<CarrinhoItem> findByCarrinhoIdAndProdutoId(Long carrinhoId, Long produtoId);
    Optional<CarrinhoItem> findByIdAndCarrinhoId(Long itemId, Long carrinhoId);

    List<CarrinhoItem> findAllByCarrinhoId(Long carrinhoId);
    @Modifying
    @Transactional
    void deleteAllByCarrinhoId(Long carrinhoId);

}
