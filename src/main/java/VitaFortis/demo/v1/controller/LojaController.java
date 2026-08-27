package VitaFortis.demo.v1.controller;

import VitaFortis.demo.v1.dto.LojaInfoDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/loja")
public class LojaController {
    private final LojaInfoDto loja;

    public LojaController(
            @Value("${vitafortis.loja.nome:Vita Fortis Suplementos}") String nome,
            @Value("${vitafortis.loja.descricao:Suplementos para sua evolucao}") String descricao,
            @Value("${vitafortis.loja.telefone:}") String telefone,
            @Value("${vitafortis.loja.email:}") String email,
            @Value("${vitafortis.loja.instagram:}") String instagram,
            @Value("${vitafortis.loja.endereco:}") String endereco) {
        this.loja = new LojaInfoDto(nome, descricao, telefone, email, instagram, endereco);
    }

    @GetMapping
    public LojaInfoDto obter() { return loja; }
}
