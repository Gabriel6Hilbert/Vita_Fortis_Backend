package VitaFortis.demo.v1.repository;

import VitaFortis.demo.v1.entity.Carrinho;
import VitaFortis.demo.v1.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface CarrinhoRepository extends JpaRepository<Carrinho, Long> {

    Optional<Carrinho> findByUsuarioIdAndAtivoTrue(Long usuarioId);

    Optional<Carrinho> findByCarrinhoAndProduto(Long id, Long produtoId);

    Long usuario(Usuario usuario);


}
