package VitaFortis.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import VitaFortis.demo.v1.repository.UsuarioRepository;

@Configuration
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(UsuarioRepository usuarios) {
        return email -> usuarios.findByEmail(email.trim().toLowerCase())
                .filter(usuario -> usuario.isAtivo())
                .map(usuario -> User.withUsername(usuario.getEmail()).password(usuario.getSenha())
                        .roles(usuario.getTipo().name()).build())
                .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException("Usuario nao encontrado"));
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index.html", "/assets/**", "/*.html", "/catalogo", "/produto/**",
                                "/entrar", "/sacola", "/conta", "/pedidos", "/admin", "/colaborador", "/favoritos",
                                "/ofertas", "/novidades", "/kits", "/sobre", "/politicas", "/faq",
                                "/trabalhe-conosco", "/contato", "/error").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/produtos/**", "/api/v1/loja").permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/colaborador/**").hasRole("COLABORADOR")
                        .anyRequest().authenticated())
                // A autenticação da aplicação é feita pelo endpoint /auth/login e pela sessão.
                // Desabilitar Basic evita o popup nativo do navegador em respostas 401.
                .httpBasic(AbstractHttpConfigurer::disable)
                .build();
    }
}
