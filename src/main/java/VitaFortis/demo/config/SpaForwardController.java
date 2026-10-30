package VitaFortis.demo.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardController {

    @GetMapping({
            "/",
            "/catalogo",
            "/marcas",
            "/objetivos",
            "/produto/{id}",
            "/entrar",
            "/sacola",
            "/conta",
            "/pedidos",
            "/admin",
            "/colaborador",
            "/favoritos",
            "/ofertas",
            "/novidades",
            "/kits",
            "/sobre",
            "/politicas",
            "/politica-de-privacidade",
            "/politica-de-cookies",
            "/termos-de-uso-e-compra",
            "/entrega-e-retirada",
            "/trocas-devolucoes-e-arrependimento",
            "/faq",
            "/trabalhe-conosco",
            "/contato"
    })
    public String forwardReactRoutes() {
        return "forward:/index.html";
    }
}
