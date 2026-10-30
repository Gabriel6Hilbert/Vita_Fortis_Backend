package VitaFortis.demo.v1.dto;

public record PedidoAdminDetalheDto(PedidoResponseDto pedido, String clienteNome, String clienteEmail,
                                    String clienteTelefone, String enderecoEntrega, String whatsappUrl) {
}
