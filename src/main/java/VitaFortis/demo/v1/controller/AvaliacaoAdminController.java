package VitaFortis.demo.v1.controller;
import VitaFortis.demo.v1.dto.AvaliacaoResponseDto; import VitaFortis.demo.v1.service.AvaliacaoService; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/admin/avaliacoes") public class AvaliacaoAdminController {private final AvaliacaoService service;public AvaliacaoAdminController(AvaliacaoService s){service=s;}@PatchMapping("/{id}/aprovada") public AvaliacaoResponseDto moderar(@PathVariable Long id,@RequestParam boolean valor){return service.moderar(id,valor);}}
