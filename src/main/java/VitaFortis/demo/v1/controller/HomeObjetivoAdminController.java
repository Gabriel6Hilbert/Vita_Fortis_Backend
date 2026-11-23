package VitaFortis.demo.v1.controller;
import VitaFortis.demo.v1.dto.HomeObjetivoDto;
import VitaFortis.demo.v1.service.HomeObjetivoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/v1/admin/home/objetivos")
public class HomeObjetivoAdminController { private final HomeObjetivoService service; public HomeObjetivoAdminController(HomeObjetivoService service){this.service=service;} @GetMapping public List<HomeObjetivoDto> listar(){return service.listarAdmin();} @PutMapping("/{chave}") public HomeObjetivoDto atualizar(@PathVariable String chave,@Valid @RequestBody HomeObjetivoDto dto){return service.atualizar(chave,dto);} }
