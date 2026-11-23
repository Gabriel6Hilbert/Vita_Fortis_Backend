package VitaFortis.demo.v1.repository;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KitProdutoMigrationTest {

    @Test
    void adicionaValorComparativoOpcionalSemAlterarProdutosExistentes() throws Exception {
        String url = "jdbc:h2:mem:migracao_kit_produto;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
        try (var connection = DriverManager.getConnection(url, "sa", "");
             var statement = connection.createStatement()) {
            statement.execute("create table produto (produto_id bigint primary key, nome varchar(120) not null)");
            statement.execute("insert into produto values (1, 'Produto existente')");
        }

        Flyway flyway = Flyway.configure()
                .dataSource(url, "sa", "")
                .baselineOnMigrate(true)
                .baselineVersion("4")
                .target("5")
                .load();

        assertEquals(1, flyway.migrate().migrationsExecuted);
        try (var connection = DriverManager.getConnection(url, "sa", "");
             var columns = connection.getMetaData().getColumns(null, null, "produto", "valor_itens_separados")) {
            assertTrue(columns.next());
            assertEquals(java.sql.Types.DECIMAL, columns.getInt("DATA_TYPE"));
        }
        try (var connection = DriverManager.getConnection(url, "sa", "");
             var rows = connection.createStatement().executeQuery("select valor_itens_separados from produto where produto_id = 1")) {
            assertTrue(rows.next());
            assertEquals(null, rows.getBigDecimal(1));
        }
        assertEquals(0, flyway.migrate().migrationsExecuted);
    }
}
