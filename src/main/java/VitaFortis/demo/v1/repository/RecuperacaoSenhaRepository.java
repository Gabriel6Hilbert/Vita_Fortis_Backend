package VitaFortis.demo.v1.repository;

import VitaFortis.demo.v1.entity.RecuperacaoSenha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface RecuperacaoSenhaRepository extends JpaRepository<RecuperacaoSenha, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RecuperacaoSenha r where r.tokenHash = :tokenHash and r.utilizadoEm is null")
    Optional<RecuperacaoSenha> findDisponivelForUpdate(@Param("tokenHash") String tokenHash);
    List<RecuperacaoSenha> findAllByUsuarioIdAndUtilizadoEmIsNull(Long usuarioId);
}
