package VitaFortis.demo.v1.controller;

import VitaFortis.demo.v1.dto.*;
import VitaFortis.demo.v1.service.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/admin/produtos")
public class ProdutoAdminController {
    private final ProdutoService produtos;
    public ProdutoAdminController(ProdutoService produtos) { this.produtos = produtos; }
    @PostMapping @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public ProdutoResponseDto criar(@Valid @RequestBody ProdutoRequestDto dto) { return produtos.create(dto); }
    @PutMapping("/{id}") public ProdutoResponseDto atualizar(@PathVariable Long id, @Valid @RequestBody ProdutoRequestDto dto) { return produtos.update(id, dto); }
    @GetMapping("/{id}") public ProdutoResponseDto buscar(@PathVariable Long id) { return produtos.getByIdAdmin(id); }
    @PatchMapping("/{id}/ativo") public void ativo(@PathVariable Long id, @RequestParam boolean valor) { produtos.setAtivo(id, valor); }
    @PatchMapping("/{id}/estoque") public ProdutoResponseDto estoque(@PathVariable Long id, @RequestParam int quantidade) { return quantidade >= 0 ? produtos.reporEstoque(id, quantidade) : produtos.baixarEstoque(id, -quantidade); }
    @PatchMapping("/{id}/desconto-percentual") public ProdutoResponseDto percentual(@PathVariable Long id, @RequestParam BigDecimal valor) { return produtos.aplicarDescontoPercentual(id, valor); }
    @PatchMapping("/{id}/desconto-valor") public ProdutoResponseDto valor(@PathVariable Long id, @RequestParam BigDecimal valor) { return produtos.aplicarDescontoValor(id, valor); }
    @PatchMapping("/{id}/metadados-comerciais") public ProdutoResponseDto metadados(@PathVariable Long id, @Valid @RequestBody ProdutoMetadadosComerciaisDto dto) { return produtos.atualizarMetadados(id, dto); }
    @DeleteMapping("/{id}/desconto") public ProdutoResponseDto removerDesconto(@PathVariable Long id) { return produtos.removerDesconto(id); }
}
