package VitaFortis.demo.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatNio2Config {

    @Bean
    @ConditionalOnProperty(name = "vita-fortis.tomcat.nio2", havingValue = "true", matchIfMissing = true)
    WebServerFactoryCustomizer<TomcatServletWebServerFactory> usarNio2() {
        return factory -> factory.setProtocol("org.apache.coyote.http11.Http11Nio2Protocol");
    }
}
