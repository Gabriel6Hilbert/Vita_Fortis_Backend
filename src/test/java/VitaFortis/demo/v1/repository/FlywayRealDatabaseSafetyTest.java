package VitaFortis.demo.v1.repository;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class FlywayRealDatabaseSafetyTest {

    @Test
    void registraBaselineDoSchemaExistenteSemReexecutarMigracoes() {
        String url = System.getenv("VF_FLYWAY_URL");
        String user = System.getenv("VF_FLYWAY_USER");
        String password = System.getenv("VF_FLYWAY_PASSWORD");
        String expectedDatabase = System.getenv("VF_FLYWAY_EXPECTED_DATABASE");
        String confirmation = System.getenv("VF_FLYWAY_CONFIRM");

        assumeTrue(url != null && user != null && password != null && expectedDatabase != null,
                "Teste real desativado: variaveis VF_FLYWAY_* ausentes");
        assertEquals("BASELINE:" + expectedDatabase, confirmation,
                "Confirmacao explicita nao corresponde ao banco esperado");
        assertEquals(expectedDatabase, databaseName(url),
                "A URL nao aponta para o banco explicitamente confirmado");
        assertTrue(expectedDatabase.equals("defaultdb") || expectedDatabase.startsWith("vitafortis_restore_"),
                "Banco fora da lista permitida para esta operacao");

        Flyway flyway = Flyway.configure()
                .dataSource(url, user, password)
                .baselineOnMigrate(true)
                .baselineVersion("2")
                .load();

        assertEquals(0, flyway.migrate().migrationsExecuted,
                "O schema existente deveria receber apenas baseline 2");
        flyway.validate();

        MigrationInfo current = flyway.info().current();
        assertNotNull(current, "Historico Flyway nao foi criado");
        assertEquals("2", current.getVersion().toString());
        assertEquals(0, flyway.migrate().migrationsExecuted,
                "Uma segunda execucao deve ser idempotente");
    }

    private String databaseName(String jdbcUrl) {
        URI uri = URI.create(jdbcUrl.substring("jdbc:".length()));
        String path = uri.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }
}
