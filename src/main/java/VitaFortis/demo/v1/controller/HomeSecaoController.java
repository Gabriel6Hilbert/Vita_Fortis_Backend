package VitaFortis.demo.v1.controller;

import VitaFortis.demo.v1.dto.HomeSecaoDto;
import VitaFortis.demo.v1.service.HomeSecaoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/home/secoes")
public class HomeSecaoController {
    private final HomeSecaoService service;
    public HomeSecaoController(HomeSecaoService service) { this.service = service; }
    @GetMapping public List<HomeSecaoDto> listar() { return service.listarPublicas(); }
}
