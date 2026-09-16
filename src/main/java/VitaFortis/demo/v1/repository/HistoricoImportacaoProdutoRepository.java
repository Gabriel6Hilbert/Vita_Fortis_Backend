package VitaFortis.demo.v1.repository;

import VitaFortis.demo.v1.entity.HistoricoImportacaoProduto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HistoricoImportacaoProdutoRepository extends JpaRepository<HistoricoImportacaoProduto, Long> {
    List<HistoricoImportacaoProduto> findTop50ByOrderByCriadoEmDesc();
}
