package VitaFortis.demo.v1.repository;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.assertTrue;

class Rcp02MigrationTest {
    @Test
    void criaTabelasVersionadasDeSolicitacaoEAuditoria() throws Exception {
        String url = "jdbc:h2:mem:migracao_rcp02;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
        try (var connection = DriverManager.getConnection(url, "sa", "");
             var statement = connection.createStatement()) {
            statement.execute("create table usuario (usuario_id bigint primary key)");
        }

        Flyway.configure().dataSource(url, "sa", "").baselineOnMigrate(true).baselineVersion("0").load().migrate();

        try (var connection = DriverManager.getConnection(url, "sa", "")) {
            try (var table = connection.getMetaData().getTables(null, null, "solicitacao_privacidade", new String[]{"TABLE"})) {
                assertTrue(table.next());
            }
            try (var table = connection.getMetaData().getTables(null, null, "auditoria_solicitacao_privacidade", new String[]{"TABLE"})) {
                assertTrue(table.next());
            }
        }
    }
}
