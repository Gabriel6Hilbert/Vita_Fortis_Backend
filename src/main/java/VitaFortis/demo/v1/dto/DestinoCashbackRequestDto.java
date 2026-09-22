package VitaFortis.demo.v1.dto;

import VitaFortis.demo.v1.enums.DestinoCashback;
import jakarta.validation.constraints.NotNull;

public record DestinoCashbackRequestDto(@NotNull DestinoCashback destino) {
}
