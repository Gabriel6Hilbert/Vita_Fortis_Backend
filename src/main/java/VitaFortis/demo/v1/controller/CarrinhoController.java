package VitaFortis.demo.v1.controller;

import VitaFortis.demo.v1.dto.Carrinho.*;
import VitaFortis.demo.v1.repository.OnCreate;
import VitaFortis.demo.v1.repository.OnUpdate;
import VitaFortis.demo.v1.service.CarrinhoService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/carrinhos/{usuarioId}")
public class CarrinhoController {
    private final CarrinhoService carrinhos;
    public CarrinhoController(CarrinhoService carrinhos) { this.carrinhos = carrinhos; }
    @GetMapping public CarrinhoResponseDto obter(@PathVariable Long usuarioId) { return carrinhos.obterCarrinho(usuarioId); }
    @PostMapping("/itens") public CarrinhoResponseDto adicionar(@PathVariable Long usuarioId, @Validated(OnCreate.class) @RequestBody CarrinhoItemRequestDto dto) { return carrinhos.adcionarItem(usuarioId, dto); }
    @PutMapping("/itens") public CarrinhoResponseDto atualizar(@PathVariable Long usuarioId, @Validated(OnUpdate.class) @RequestBody CarrinhoItemRequestDto dto) { return carrinhos.atualizarQuantidade(usuarioId, dto); }
    @DeleteMapping("/itens/{itemId}") public CarrinhoResponseDto remover(@PathVariable Long usuarioId, @PathVariable Long itemId, @RequestParam(defaultValue = "1") int quantidade) { return carrinhos.removeItem(usuarioId, itemId, quantidade); }
    @DeleteMapping("/itens") public CarrinhoResponseDto limpar(@PathVariable Long usuarioId) { return carrinhos.limparCarrinho(usuarioId); }
    @PostMapping("/cupom/{codigo}") public CarrinhoResponseDto cupom(@PathVariable Long usuarioId, @PathVariable String codigo) { return carrinhos.aplicarCupom(usuarioId, codigo); }
    @DeleteMapping("/cupom") public CarrinhoResponseDto removerCupom(@PathVariable Long usuarioId) { return carrinhos.removerCupom(usuarioId); }
}
