package VitaFortis.demo.v1.dto;

import VitaFortis.demo.v1.enums.FormaRecebimento;
import VitaFortis.demo.v1.enums.StatusCompra;
import VitaFortis.demo.v1.enums.StatusPagamento;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

public record PedidoFiltroDto(
        String numero,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
        String cliente, StatusCompra status, FormaRecebimento recebimento,
        String pagamento, StatusPagamento statusPagamento) {
}
