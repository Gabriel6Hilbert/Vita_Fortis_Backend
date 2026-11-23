package VitaFortis.demo.v1.controller;

import VitaFortis.demo.v1.dto.HomeSecaoDto;
import VitaFortis.demo.v1.dto.HomeOrdemRequestDto;
import VitaFortis.demo.v1.service.ProdutoImagemService;
import VitaFortis.demo.v1.service.HomeSecaoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/home/secoes")
public class HomeSecaoAdminController {
    private final HomeSecaoService service;
    private final ProdutoImagemService imagens;
    public HomeSecaoAdminController(HomeSecaoService service, ProdutoImagemService imagens) { this.service = service; this.imagens=imagens; }
    @GetMapping public List<HomeSecaoDto> listar() { return service.listarAdmin(); }
    @PutMapping("/{chave}") public HomeSecaoDto atualizar(@PathVariable String chave, @Valid @RequestBody HomeSecaoDto dto) { return service.atualizar(chave, dto); }
    @PutMapping("/ordem") public List<HomeSecaoDto> ordem(@Valid @RequestBody HomeOrdemRequestDto dto){return service.atualizarOrdem(dto);}
    @PostMapping("/imagens") public java.util.Map<String,String> imagem(@RequestPart("imagem") org.springframework.web.multipart.MultipartFile imagem) throws java.io.IOException{return java.util.Map.of("url",imagens.salvar(imagem));}
}
