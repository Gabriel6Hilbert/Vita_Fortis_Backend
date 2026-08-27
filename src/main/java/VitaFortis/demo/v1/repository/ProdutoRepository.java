package VitaFortis.demo.v1.repository;

import VitaFortis.demo.v1.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ProdutoRepository extends JpaRepository<Produto,Long>, JpaSpecificationExecutor<Produto> {

    boolean existsByCodigoIgnoreCase(String codigo);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
        update Produto p
           set p.quantidadeEstoque = p.quantidadeEstoque - :qtd,
               p.ativo = case when (p.quantidadeEstoque - :qtd) = 0 then false else p.ativo end,
               p.versao = p.versao + 1
         where p.id = :id
           and p.quantidadeEstoque >= :qtd
    """)
    int debitarEstoque(Long id, int qtd);


    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update Produto p
            set p.quantidadeEstoque = p.quantidadeEstoque + :qtd,
            p.ativo = true,
            p.versao = p.versao + 1
            where p.id = :id
        """)
    int creditarEstoque(Long id, int qtd);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Produto p set p.totalVendido = greatest(0, p.totalVendido + :quantidade) where p.id = :id")
    int alterarTotalVendido(Long id, long quantidade);

//    @Modifying(flushAutomatically = true, clearAutomatically = true)
//    @Query("""
//    update Produto p
//        set p.ativo = false,
//        p.versao = p.versao + 1
//    where p.id = :produtoId
//    """)
//    int inativar(Long produtoId);
//
//    @Modifying(flushAutomatically = true, clearAutomatically = true)
//    @Query("""
//    update Produto p
//        set p.ativo = true,
//            p.versao = p.versao + 1
//    where p.id = :produtoId
//        """)
//    int reativar(Long produtoId);

    Optional<Produto> findByIdAndAtivoTrue(Long id);
}
