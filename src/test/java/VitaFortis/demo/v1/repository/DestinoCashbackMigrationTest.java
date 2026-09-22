package VitaFortis.demo.v1.repository;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DestinoCashbackMigrationTest {

    @Test
    void adicionaColunaDestinoCashbackEmBancoExistente() throws Exception {
        String url = "jdbc:h2:mem:migracao_cashback;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
        try (var connection = DriverManager.getConnection(url, "sa", "");
             var statement = connection.createStatement()) {
            statement.execute("create table usuario (usuario_id bigint primary key)");
        }

        Flyway.configure()
                .dataSource(url, "sa", "")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .load()
                .migrate();

        try (var connection = DriverManager.getConnection(url, "sa", "");
             var columns = connection.getMetaData().getColumns(null, null, "usuario", "destino_cashback")) {
            assertEquals(true, columns.next());
            assertEquals("CHARACTER VARYING", columns.getString("TYPE_NAME"));
            assertEquals(20, columns.getInt("COLUMN_SIZE"));
        }
    }
}
