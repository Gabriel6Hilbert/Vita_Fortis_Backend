package VitaFortis.demo.v1.repository;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import java.sql.DriverManager;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FlywaySafetyTest {
    @Test
    void segundaExecucaoNaoRepeteMigracoes() throws Exception {
        String url = "jdbc:h2:mem:flyway_seguro;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
        try (var connection = DriverManager.getConnection(url, "sa", ""); var statement = connection.createStatement()) {
            statement.execute("create table usuario (usuario_id bigint primary key)");
            statement.execute("create table produto (produto_id bigint primary key, preco decimal(12,2), desc_percent decimal(5,2), desc_valor decimal(12,2))");
        }
        Flyway flyway = Flyway.configure().dataSource(url, "sa", "").baselineOnMigrate(true).baselineVersion("0").load();
        assertEquals(4, flyway.migrate().migrationsExecuted);
        flyway.validate();
        assertEquals(0, flyway.migrate().migrationsExecuted);
    }

    @Test
    void esquemaLegadoIntegralPodeSerBaselinedNaVersaoDois() throws Exception {
        String url = "jdbc:h2:mem:flyway_baseline2;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
        try (var connection = DriverManager.getConnection(url, "sa", ""); var statement = connection.createStatement()) {
            statement.execute("create table usuario (usuario_id bigint primary key, destino_cashback varchar(20))");
            statement.execute("create table solicitacao_privacidade (solicitacao_id bigint primary key)");
            statement.execute("create table auditoria_solicitacao_privacidade (auditoria_id bigint primary key)");
            statement.execute("create table produto (produto_id bigint primary key, preco decimal(12,2), desc_percent decimal(5,2), desc_valor decimal(12,2))");
        }
        Flyway flyway = Flyway.configure().dataSource(url, "sa", "").baselineOnMigrate(true).baselineVersion("2").load();
        assertEquals(2, flyway.migrate().migrationsExecuted);
        flyway.validate();
        assertEquals("4", flyway.info().current().getVersion().toString());
    }
}
