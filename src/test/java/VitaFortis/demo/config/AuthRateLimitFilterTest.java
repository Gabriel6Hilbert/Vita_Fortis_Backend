package VitaFortis.demo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class AuthRateLimitFilterTest {
    @Test
    void bloqueiaDepoisDoLimiteNaMesmaRotaEIp() throws Exception {
        var filter = new AuthRateLimitFilter(new ObjectMapper(), true, 2, 60);
        FilterChain chain = mock(FilterChain.class);

        var first = request("/api/v1/auth/login");
        var second = request("/api/v1/auth/login");
        var blocked = request("/api/v1/auth/login");

        filter.doFilter(first, new MockHttpServletResponse(), chain);
        filter.doFilter(second, new MockHttpServletResponse(), chain);
        var response = new MockHttpServletResponse();
        filter.doFilter(blocked, response, chain);

        verify(chain, times(2)).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        assertEquals(429, response.getStatus());
        assertTrue(response.getHeader("Retry-After") != null);
        assertTrue(response.getContentAsString().contains("Muitas tentativas"));
    }

    @Test
    void naoLimitaRotasForaDaAutenticacao() throws Exception {
        var filter = new AuthRateLimitFilter(new ObjectMapper(), true, 1, 60);
        FilterChain chain = mock(FilterChain.class);
        var request = request("/api/v1/produtos");

        filter.doFilter(request, new MockHttpServletResponse(), chain);
        filter.doFilter(request, new MockHttpServletResponse(), chain);

        verify(chain, times(2)).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    private MockHttpServletRequest request(String path) {
        var request = new MockHttpServletRequest("POST", path);
        request.setRemoteAddr("203.0.113.10");
        return request;
    }
}
