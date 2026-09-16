package VitaFortis.demo.v1.repository;

import VitaFortis.demo.v1.entity.CatalogoCadastro;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CatalogoCadastroRepository extends JpaRepository<CatalogoCadastro, Long> {
    List<CatalogoCadastro> findAllByOrderByTipoAscNomeAsc();
    Optional<CatalogoCadastro> findByTipoAndCodigo(String tipo, String codigo);
}
