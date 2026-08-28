package VitaFortis.demo.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardController {

    @GetMapping({
            "/",
            "/catalogo",
            "/produto/{id}",
            "/entrar",
            "/sacola",
            "/conta",
            "/pedidos",
            "/admin",
            "/favoritos",
            "/ofertas",
            "/novidades",
            "/kits",
            "/sobre",
            "/politicas",
            "/faq",
            "/trabalhe-conosco",
            "/contato"
    })
    public String forwardReactRoutes() {
        return "forward:/index.html";
    }
}
