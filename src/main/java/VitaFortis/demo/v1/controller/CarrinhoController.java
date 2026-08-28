package VitaFortis.demo.v1.controller;

import VitaFortis.demo.v1.dto.Carrinho.*;
import VitaFortis.demo.v1.repository.OnCreate;
import VitaFortis.demo.v1.repository.OnUpdate;
import VitaFortis.demo.v1.service.CarrinhoService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/v1/carrinhos/{usuarioId}")
public class CarrinhoController {
    private final CarrinhoService carrinhos;
    public CarrinhoController(CarrinhoService carrinhos) { this.carrinhos = carrinhos; }
    @GetMapping public CarrinhoResponseDto obter(@PathVariable Long usuarioId, Authentication a) { return carrinhos.obterCarrinho(usuarioId, a.getName()); }
    @PostMapping("/itens") public CarrinhoResponseDto adicionar(@PathVariable Long usuarioId, @Validated(OnCreate.class) @RequestBody CarrinhoItemRequestDto dto, Authentication a) { return carrinhos.adcionarItem(usuarioId, dto, a.getName()); }
    @PutMapping("/itens") public CarrinhoResponseDto atualizar(@PathVariable Long usuarioId, @Validated(OnUpdate.class) @RequestBody CarrinhoItemRequestDto dto, Authentication a) { return carrinhos.atualizarQuantidade(usuarioId, dto, a.getName()); }
    @DeleteMapping("/itens/{itemId}") public CarrinhoResponseDto remover(@PathVariable Long usuarioId, @PathVariable Long itemId, @RequestParam(defaultValue = "1") int quantidade, Authentication a) { return carrinhos.removeItem(usuarioId, itemId, quantidade, a.getName()); }
    @DeleteMapping("/itens") public CarrinhoResponseDto limpar(@PathVariable Long usuarioId, Authentication a) { return carrinhos.limparCarrinho(usuarioId, a.getName()); }
    @PostMapping("/cupom/{codigo}") public CarrinhoResponseDto cupom(@PathVariable Long usuarioId, @PathVariable String codigo, Authentication a) { return carrinhos.aplicarCupom(usuarioId, codigo, a.getName()); }
    @DeleteMapping("/cupom") public CarrinhoResponseDto removerCupom(@PathVariable Long usuarioId, Authentication a) { return carrinhos.removerCupom(usuarioId, a.getName()); }
}
