package VitaFortis.demo.v1.repository;

import VitaFortis.demo.v1.entity.Carrinho;
import VitaFortis.demo.v1.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface CarrinhoRepository extends JpaRepository<Carrinho, Long> {

    Optional<Carrinho> findByUsuarioIdAndAtivoTrue(Long usuarioId);

    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select c from Carrinho c where c.usuario.id = :usuarioId and c.ativo = true")
    Optional<Carrinho> findAtivoParaAtualizacao(Long usuarioId);

}
