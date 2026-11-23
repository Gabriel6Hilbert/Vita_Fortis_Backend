package VitaFortis.demo.v1.controller;
import VitaFortis.demo.v1.dto.HomeObjetivoDto;
import VitaFortis.demo.v1.service.HomeObjetivoService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/v1/home/objetivos")
public class HomeObjetivoController { private final HomeObjetivoService service; public HomeObjetivoController(HomeObjetivoService service){this.service=service;} @GetMapping public List<HomeObjetivoDto> listar(){return service.listarPublicos();} }
