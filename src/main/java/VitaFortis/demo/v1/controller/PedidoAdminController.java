package VitaFortis.demo.v1.controller;

import VitaFortis.demo.v1.dto.PedidoResponseDto;
import VitaFortis.demo.v1.enums.StatusCompra;
import VitaFortis.demo.v1.service.PedidoService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/pedidos")
public class PedidoAdminController {
    private final PedidoService pedidos;
    public PedidoAdminController(PedidoService pedidos) { this.pedidos = pedidos; }
    @GetMapping public List<PedidoResponseDto> listar() { return pedidos.listarTodos(); }
    @PatchMapping("/{id}/status") public PedidoResponseDto status(@PathVariable Long id, @RequestParam StatusCompra valor, @RequestParam(required = false) String observacao) { return pedidos.alterarStatus(id, valor, observacao); }
    @PatchMapping("/{id}/pagamento/aprovar") public PedidoResponseDto aprovarPagamento(@PathVariable Long id, @RequestParam(required=false) String observacao) { return pedidos.confirmarPagamento(id, true, observacao); }
    @PatchMapping("/{id}/pagamento/recusar") public PedidoResponseDto recusarPagamento(@PathVariable Long id, @RequestParam(required=false) String observacao) { return pedidos.confirmarPagamento(id, false, observacao); }
}
