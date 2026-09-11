package VitaFortis.demo.v1.controller;

import VitaFortis.demo.v1.dto.*;
import VitaFortis.demo.v1.service.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import java.util.List;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/v1/admin/produtos")
public class ProdutoAdminController {
    private final ProdutoService produtos;
    public ProdutoAdminController(ProdutoService produtos) { this.produtos = produtos; }
    @GetMapping public Page<ProdutoResponseDto> listar(@RequestParam(required=false) String busca,
            @RequestParam(defaultValue="0") int pagina, @RequestParam(defaultValue="100") int tamanho) {
        return produtos.listarAdmin(busca, pagina, tamanho);
    }
    @PostMapping @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public ProdutoResponseDto criar(@Valid @RequestBody ProdutoRequestDto dto) { return produtos.create(dto); }
    @PutMapping("/{id}") public ProdutoResponseDto atualizar(@PathVariable Long id, @Valid @RequestBody ProdutoRequestDto dto) { return produtos.update(id, dto); }
    @GetMapping("/{id}") public ProdutoResponseDto buscar(@PathVariable Long id) { return produtos.getByIdAdmin(id); }
    @PatchMapping("/{id}/ativo") public void ativo(@PathVariable Long id, @RequestParam boolean valor) { produtos.setAtivo(id, valor); }
    @PatchMapping("/{id}/estoque") public ProdutoResponseDto estoque(@PathVariable Long id, @RequestParam int quantidade) { return quantidade >= 0 ? produtos.reporEstoque(id, quantidade) : produtos.baixarEstoque(id, -quantidade); }
    @PutMapping("/{id}/estoque") public ProdutoResponseDto ajustarEstoque(@PathVariable Long id,@RequestParam int quantidade,@RequestParam String motivo,Authentication auth){return produtos.ajustarEstoque(id,quantidade,motivo,auth.getName());}
    @GetMapping("/{id}/estoque/movimentacoes") public List<MovimentacaoEstoqueDto> historicoEstoque(@PathVariable Long id){return produtos.historicoEstoque(id);}
    @PatchMapping("/{id}/desconto-percentual") public ProdutoResponseDto percentual(@PathVariable Long id, @RequestParam BigDecimal valor) { return produtos.aplicarDescontoPercentual(id, valor); }
    @PatchMapping("/{id}/desconto-valor") public ProdutoResponseDto valor(@PathVariable Long id, @RequestParam BigDecimal valor) { return produtos.aplicarDescontoValor(id, valor); }
    @PatchMapping("/{id}/metadados-comerciais") public ProdutoResponseDto metadados(@PathVariable Long id, @Valid @RequestBody ProdutoMetadadosComerciaisDto dto) { return produtos.atualizarMetadados(id, dto); }
    @DeleteMapping("/{id}/desconto") public ProdutoResponseDto removerDesconto(@PathVariable Long id) { return produtos.removerDesconto(id); }
    @DeleteMapping("/{id}") public java.util.Map<String,String> excluir(@PathVariable Long id) { produtos.arquivar(id); return java.util.Map.of("resultado", "Produto arquivado para preservar pedidos, carrinhos, favoritos e estoque."); }
    @PostMapping(value="/importacao",consumes=org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public java.util.Map<String,Object> importar(@RequestPart("arquivo") org.springframework.web.multipart.MultipartFile arquivo,@RequestParam(defaultValue="true") boolean preVisualizar) throws java.io.IOException { return produtos.importar(arquivo,preVisualizar); }
    @GetMapping(value="/importacao/modelo",produces="text/csv") public ResponseEntity<byte[]> modelo(){byte[] body="codigo;nome;descricao;marca;unidade;preco;estoque;categoria;imagemUrl;ativo\r\nSKU-001;Produto exemplo;Descricao;Marca;300g;99.90;10;PROTEINAS;;true\r\n".getBytes(java.nio.charset.StandardCharsets.UTF_8);return ResponseEntity.ok().header("Content-Disposition","attachment; filename=vita-fortis-modelo-produtos.csv").body(body);}
}
