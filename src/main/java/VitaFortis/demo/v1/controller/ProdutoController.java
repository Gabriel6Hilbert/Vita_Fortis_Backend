package VitaFortis.demo.v1.controller;

import VitaFortis.demo.v1.dto.*;
import VitaFortis.demo.v1.service.ProdutoService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/produtos")
public class ProdutoController {
    private final ProdutoService produtos;
    public ProdutoController(ProdutoService produtos) { this.produtos = produtos; }

    @GetMapping
    public Page<ProdutoResponseDto> listar(@ModelAttribute ProdutoFiltroDto filtro) { return produtos.listarPublicos(filtro); }

    @GetMapping("/{id}")
    public ProdutoResponseDto buscar(@PathVariable Long id) { return produtos.getByIdPublico(id); }

    @GetMapping("/categorias")
    public List<CategoriaResumoDto> categorias() { return produtos.listarCategorias(); }
}
