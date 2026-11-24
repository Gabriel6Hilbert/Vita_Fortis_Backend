package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

public class V5__adiciona_valor_itens_separados_kit extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        if (columnExists(context)) {
            return;
        }
        try (var statement = context.getConnection().createStatement()) {
            statement.execute("ALTER TABLE produto ADD COLUMN valor_itens_separados DECIMAL(12,2) NULL");
        }
    }

    private boolean columnExists(Context context) throws Exception {
        DatabaseMetaData metadata = context.getConnection().getMetaData();
        String catalog = context.getConnection().getCatalog();
        try (ResultSet columns = metadata.getColumns(catalog, null, "%", "%")) {
            while (columns.next()) {
                if ("produto".equalsIgnoreCase(columns.getString("TABLE_NAME"))
                        && "valor_itens_separados".equalsIgnoreCase(columns.getString("COLUMN_NAME"))) {
                    return true;
                }
            }
        }
        return false;
    }
}
