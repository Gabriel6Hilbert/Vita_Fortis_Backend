package VitaFortis.demo.v1.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import VitaFortis.demo.v1.enums.*;

public record RelatorioFiltroDto(
        @NotNull @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate inicio,
        @NotNull @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate fim,
        @Size(max=120) String busca, @Positive Long colaboradorId, Boolean ativo,
        String categoria, StatusCompra status, FormaRecebimento recebimento,
        @Pattern(regexp="PIX|CARTAO|BOLETO") String pagamento, @Min(0) Integer estoqueMax) {
    public static RelatorioFiltroDto periodo(LocalDate inicio, LocalDate fim) {
        return new RelatorioFiltroDto(inicio,fim,null,null,null,null,null,null,null,null);
    }
    public void validar() {
        if(inicio==null || fim==null || fim.isBefore(inicio)) throw new IllegalArgumentException("Informe um periodo valido");
    }
    public boolean contem(String... valores) {
        if(busca==null || busca.isBlank()) return true;
        String termo=busca.trim().toLowerCase(java.util.Locale.ROOT);
        return java.util.Arrays.stream(valores).anyMatch(v->v!=null && v.toLowerCase(java.util.Locale.ROOT).contains(termo));
    }
}
