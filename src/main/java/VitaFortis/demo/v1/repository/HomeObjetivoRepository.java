package VitaFortis.demo.v1.repository;

import VitaFortis.demo.v1.entity.HomeObjetivo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface HomeObjetivoRepository extends JpaRepository<HomeObjetivo,Long> {
    List<HomeObjetivo> findAllByOrderByOrdemAscIdAsc();
    List<HomeObjetivo> findAllByAtivoTrueOrderByOrdemAscIdAsc();
    Optional<HomeObjetivo> findByChaveIgnoreCase(String chave);
}
