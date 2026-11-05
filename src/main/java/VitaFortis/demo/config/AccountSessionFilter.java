package VitaFortis.demo.config;

import VitaFortis.demo.v1.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public final class AccountSessionFilter extends OncePerRequestFilter {
    public static final String LOGIN_SESSION_ATTRIBUTE = AccountSessionFilter.class.getName() + ".login";

    private final UsuarioRepository usuarios;

    AccountSessionFilter(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return !path.startsWith("/api/v1/") || path.startsWith("/api/v1/auth/")
                || ("GET".equals(request.getMethod()) && (path.startsWith("/api/v1/produtos")
                || path.startsWith("/api/v1/loja")));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        var session = request.getSession(false);
        if (session != null && Boolean.TRUE.equals(session.getAttribute(LOGIN_SESSION_ATTRIBUTE))
                && session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY)
                instanceof SecurityContext context && context.getAuthentication() != null) {
            var auth = context.getAuthentication();
            boolean vigente = usuarios.findByEmail(auth.getName())
                    .filter(usuario -> usuario.isAtivo() && auth.getAuthorities()
                            .contains(new SimpleGrantedAuthority("ROLE_" + usuario.getTipo().name())))
                    .isPresent();
            if (!vigente) {
                session.invalidate();
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
