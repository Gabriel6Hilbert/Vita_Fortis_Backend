package VitaFortis.demo.v1.controller;

import VitaFortis.demo.config.SecurityConfig;
import VitaFortis.demo.v1.dto.CashbackSaldoDto;
import VitaFortis.demo.v1.repository.UsuarioRepository;
import VitaFortis.demo.v1.service.CashbackService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CashbackController.class)
@Import(SecurityConfig.class)
class CashbackControllerSecurityTest {
    @Autowired MockMvc mvc;
    @MockitoBean CashbackService cashback;
    @MockitoBean UsuarioRepository usuarios;

    @Test
    @WithMockUser(username = "cliente@teste.com", roles = "CLIENTE")
    void clienteNaoAcessaAreaDoColaborador() throws Exception {
        mvc.perform(get("/api/v1/colaborador/cashback")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "colaborador@teste.com", roles = "COLABORADOR")
    void colaboradorAcessaSeuSaldo() throws Exception {
        when(cashback.meuSaldo("colaborador@teste.com")).thenReturn(new CashbackSaldoDto(1L, new BigDecimal("25.00")));
        mvc.perform(get("/api/v1/colaborador/cashback")).andExpect(status().isOk());
    }
}
