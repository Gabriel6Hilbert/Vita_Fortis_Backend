package VitaFortis.demo.v1.service;

import VitaFortis.demo.v1.enums.FormaRecebimento;
import VitaFortis.demo.v1.enums.StatusCompra;

public record PedidoStatusAlteradoEvent(Long pedidoId, String email, String nome,
                                        StatusCompra status, FormaRecebimento recebimento) {
}
