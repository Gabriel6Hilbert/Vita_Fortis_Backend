package VitaFortis.demo.v1.repository;

import VitaFortis.demo.v1.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ProdutoRepository extends JpaRepository<Produto,Long> {

    @Modifying
    @Query("""
        update Produto p
           set p.quantidadeEstoque = p.quantidadeEstoque - :qtd
         where p.id = :produtoId
           and p.quantidadeEstoque >= :qtd
    """)
    int debitarEstoque(Long produtoId, int quantidadeEstoque);
}
