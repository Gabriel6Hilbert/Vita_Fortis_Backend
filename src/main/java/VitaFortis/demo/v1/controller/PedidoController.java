package VitaFortis.demo.v1.controller;

import VitaFortis.demo.v1.dto.*;
import VitaFortis.demo.v1.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/v1/pedidos")
public class PedidoController {
    private final PedidoService pedidos;
    public PedidoController(PedidoService pedidos) { this.pedidos = pedidos; }
    @PostMapping @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public PedidoResponseDto criar(@Valid @RequestBody PedidoRequestDto dto) { return pedidos.criar(dto); }
    @GetMapping("/usuario/{usuarioId}") public List<PedidoResponseDto> usuario(@PathVariable Long usuarioId) { return pedidos.listarUsuario(usuarioId); }
    @GetMapping("/{id}") public PedidoResponseDto acompanhar(@PathVariable Long id, Authentication auth) { return pedidos.acompanhar(id, auth.getName()); }
}
