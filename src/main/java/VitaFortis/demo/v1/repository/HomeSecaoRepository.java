package VitaFortis.demo.v1.repository;

import VitaFortis.demo.v1.entity.HomeSecao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HomeSecaoRepository extends JpaRepository<HomeSecao, Long> {
    List<HomeSecao> findAllByOrderByOrdemAscIdAsc();
    List<HomeSecao> findAllByAtivaTrueOrderByOrdemAscIdAsc();
    Optional<HomeSecao> findByChaveIgnoreCase(String chave);
}
