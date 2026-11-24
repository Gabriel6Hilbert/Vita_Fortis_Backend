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

        assertEquals(1, flyway.migrate().migrationsExecuted,
                "O schema existente deveria receber apenas a migracao comercial V3");
        flyway.validate();

        MigrationInfo current = flyway.info().current();
        assertNotNull(current, "Historico Flyway nao foi criado");
        assertEquals("3", current.getVersion().toString());
        assertEquals(0, flyway.migrate().migrationsExecuted,
                "Uma segunda execucao deve ser idempotente");
    }

    @Test
    void reparaHistoricoDoCloneDoIncidenteEMigraAteAVersaoSete() {
        String url = System.getenv("VF_FLYWAY_RECOVERY_URL");
        String user = System.getenv("VF_FLYWAY_RECOVERY_USER");
        String password = System.getenv("VF_FLYWAY_RECOVERY_PASSWORD");
        String expectedDatabase = System.getenv("VF_FLYWAY_RECOVERY_EXPECTED_DATABASE");
        String confirmation = System.getenv("VF_FLYWAY_RECOVERY_CONFIRM");
        String productionConfirmation = System.getenv("VF_FLYWAY_RECOVERY_PRODUCTION_CONFIRM");

        assumeTrue(url != null && user != null && password != null && expectedDatabase != null,
                "Ensaio de recuperacao desativado: variaveis VF_FLYWAY_RECOVERY_* ausentes");
        assertEquals("REPAIR_MIGRATE:" + expectedDatabase, confirmation,
                "Confirmacao explicita nao corresponde ao clone esperado");
        assertEquals(expectedDatabase, databaseName(url),
                "A URL nao aponta para o clone explicitamente confirmado");
        assertTrue(expectedDatabase.startsWith("vitafortis_restore_")
                        || (expectedDatabase.equals("defaultdb")
                        && ("BACKUP_VERIFIED:" + expectedDatabase).equals(productionConfirmation)),
                "Repair/migrate exige clone isolado ou confirmacao adicional de backup verificado");

        Flyway flyway = Flyway.configure()
                .dataSource(url, user, password)
                .cleanDisabled(true)
                .validateOnMigrate(true)
                .load();

        flyway.repair();
        assertEquals(4, flyway.migrate().migrationsExecuted,
                "O clone do incidente deve aplicar V4, V5, V6 e V7 depois do repair");
        flyway.validate();
        assertEquals("7", flyway.info().current().getVersion().toString());
        assertEquals(0, flyway.migrate().migrationsExecuted,
                "Uma segunda execucao deve permanecer idempotente");
    }

    private String databaseName(String jdbcUrl) {
        URI uri = URI.create(jdbcUrl.substring("jdbc:".length()));
        String path = uri.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }
}
