package VitaFortis.demo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {
    private static final Set<String> ROTAS_PROTEGIDAS = Set.of(
            "/api/v1/auth/login",
            "/api/v1/auth/cadastro",
            "/api/v1/auth/recuperacao-senha",
            "/api/v1/auth/redefinicao-senha"
    );

    private final ObjectMapper objectMapper;
    private final boolean enabled;
    private final int maxRequests;
    private final long windowSeconds;
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();
    private final AtomicLong requestCounter = new AtomicLong();

    public AuthRateLimitFilter(ObjectMapper objectMapper,
                               @Value("${vita-fortis.security.auth-rate-limit.enabled:true}") boolean enabled,
                               @Value("${vita-fortis.security.auth-rate-limit.max-requests:10}") int maxRequests,
                               @Value("${vita-fortis.security.auth-rate-limit.window-seconds:900}") long windowSeconds) {
        this.objectMapper = objectMapper;
        this.enabled = enabled;
        this.maxRequests = Math.max(1, maxRequests);
        this.windowSeconds = Math.max(1, windowSeconds);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !enabled || !"POST".equalsIgnoreCase(request.getMethod())
                || !ROTAS_PROTEGIDAS.contains(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long now = Instant.now().getEpochSecond();
        String key = request.getRemoteAddr() + ':' + request.getRequestURI();
        Window current = windows.compute(key, (ignored, previous) -> {
            if (previous == null || now - previous.startedAt() >= windowSeconds) {
                return new Window(now, 1);
            }
            return new Window(previous.startedAt(), previous.requests() + 1);
        });

        if ((requestCounter.incrementAndGet() & 255) == 0) {
            windows.entrySet().removeIf(entry -> now - entry.getValue().startedAt() >= windowSeconds);
        }

        if (current.requests() > maxRequests) {
            long retryAfter = Math.max(1, windowSeconds - (now - current.startedAt()));
            response.setStatus(429);
            response.setHeader("Retry-After", Long.toString(retryAfter));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            objectMapper.writeValue(response.getWriter(), Map.of(
                    "status", 429,
                    "erro", "Too Many Requests",
                    "mensagem", "Muitas tentativas. Aguarde antes de tentar novamente."
            ));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private record Window(long startedAt, int requests) { }
}
