package VitaFortis.demo.v1.repository;

import VitaFortis.demo.v1.entity.Carrinho;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CarrinhoRepository extends JpaRepository<Carrinho, Long> {

    Optional<Carrinho> findyByUsuarioAtivo(Long usuarioId);

    Optional<Carrinho> findByCarrinhoAndProduto(Long id, Long id1);
}
