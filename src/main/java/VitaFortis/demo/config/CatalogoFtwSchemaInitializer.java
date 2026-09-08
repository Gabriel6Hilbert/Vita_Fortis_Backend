package VitaFortis.demo.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
@Order(0)
@ConditionalOnProperty(name = "vita-fortis.catalogo-ftw.importar", havingValue = "true")
public class CatalogoFtwSchemaInitializer implements CommandLineRunner {
    private final JdbcTemplate jdbc;
    private final DataSource dataSource;

    public CatalogoFtwSchemaInitializer(JdbcTemplate jdbc, DataSource dataSource) {
        this.jdbc = jdbc;
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        try (var connection = dataSource.getConnection()) {
            String banco = connection.getMetaData().getDatabaseProductName();
            if (banco != null && banco.toLowerCase().contains("mysql")) {
                jdbc.execute("ALTER TABLE PRODUTO MODIFY COLUMN PRECO DECIMAL(12,2) NULL");
            }
        }
    }
}
