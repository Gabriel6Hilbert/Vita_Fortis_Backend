package VitaFortis.demo.v1.repository;

import VitaFortis.demo.v1.entity.RecuperacaoSenha;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecuperacaoSenhaRepository extends JpaRepository<RecuperacaoSenha, Long> {
    Optional<RecuperacaoSenha> findByTokenHashAndUtilizadoEmIsNull(String tokenHash);
    List<RecuperacaoSenha> findAllByUsuarioIdAndUtilizadoEmIsNull(Long usuarioId);
}
