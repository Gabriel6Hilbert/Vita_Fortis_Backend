package VitaFortis.demo.v1.dto;

import VitaFortis.demo.v1.enums.DestinoCashback;
import java.math.BigDecimal;

public record ColaboradorResumoDto(Long colaboradorId, BigDecimal saldo, DestinoCashback destinoCashback) {
}
