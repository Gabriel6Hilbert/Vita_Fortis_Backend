package VitaFortis.demo.v1.dto;

import java.math.BigDecimal;

public record AlteracaoPrecoDto(Long produtoId, String produtoNome,
                                BigDecimal precoAnterior, BigDecimal precoAtual) {}
