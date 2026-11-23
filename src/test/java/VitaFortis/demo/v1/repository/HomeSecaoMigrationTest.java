package VitaFortis.demo.v1.repository;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HomeSecaoMigrationTest {
    @Test
    void criaNoveSecoesComPadroesAtivosSemSelecionarProdutos() throws Exception {
        String url = "jdbc:h2:mem:migracao_home_secao;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
        try (var connection = DriverManager.getConnection(url, "sa", "")) {
            connection.createStatement().execute("create table produto (produto_id bigint primary key)");
        }
        Flyway flyway = Flyway.configure().dataSource(url, "sa", "").baselineOnMigrate(true).baselineVersion("5").target("6").load();
        assertEquals(1, flyway.migrate().migrationsExecuted);
        try (var connection = DriverManager.getConnection(url, "sa", "");
             var rows = connection.createStatement().executeQuery("select count(*), sum(case when ativa then 1 else 0 end) from home_secao")) {
            rows.next();
            assertEquals(9, rows.getInt(1));
            assertEquals(9, rows.getInt(2));
        }
        try (var connection = DriverManager.getConnection(url, "sa", "");
             var rows = connection.createStatement().executeQuery("select count(*) from home_secao_produto")) {
            rows.next();
            assertEquals(0, rows.getInt(1));
        }
    }
}
