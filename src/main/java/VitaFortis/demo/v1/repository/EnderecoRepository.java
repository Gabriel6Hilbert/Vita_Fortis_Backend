package VitaFortis.demo.v1.repository;

import VitaFortis.demo.v1.entity.Endereco;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnderecoRepository extends JpaRepository<Endereco, Long> {
    List<Endereco> findAllByUsuarioIdAndAtivoTrueOrderByPrincipalDescIdAsc(Long usuarioId);

    Optional<Endereco> findByIdAndUsuarioIdAndAtivoTrue(Long id, Long usuarioId);
}
