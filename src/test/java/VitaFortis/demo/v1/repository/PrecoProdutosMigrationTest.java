package VitaFortis.demo.v1.repository;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PrecoProdutosMigrationTest {

    @Test
    void defineUmRealSemDescontoParaTodosOsProdutosUmaUnicaVez() throws Exception {
        String url = "jdbc:h2:mem:migracao_preco_produtos;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
        try (var connection = DriverManager.getConnection(url, "sa", "");
             var statement = connection.createStatement()) {
            statement.execute("create table usuario (usuario_id bigint primary key)");
            statement.execute("create table produto (produto_id bigint primary key, preco decimal(12,2), desc_percent decimal(5,2), desc_valor decimal(12,2))");
            statement.execute("insert into produto values (1, 99.90, 10.00, null), (2, 45.00, null, 5.00)");
        }

        Flyway flyway = Flyway.configure()
                .dataSource(url, "sa", "")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .target("3")
                .load();
        assertEquals(3, flyway.migrate().migrationsExecuted);

        try (var connection = DriverManager.getConnection(url, "sa", "");
             var rows = connection.createStatement().executeQuery(
                     "select preco, desc_percent, desc_valor from produto order by produto_id")) {
            while (rows.next()) {
                assertEquals(0, new BigDecimal("1.00").compareTo(rows.getBigDecimal("preco")));
                assertNull(rows.getBigDecimal("desc_percent"));
                assertNull(rows.getBigDecimal("desc_valor"));
            }
        }

        assertEquals(0, flyway.migrate().migrationsExecuted);
    }
}
