package kr.reciptopia.reciptopiaserver.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

public class H2DbCleaner {

    private static final String SYSTEM_CATALOG_SCHEMA = "INFORMATION_SCHEMA";

    public static void clean(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            setReferentialIntegrity(connection, false);
            for (String table : getClearingTables(connection)) {
                truncateTable(connection, table);
            }
            setReferentialIntegrity(connection, true);

            connection.commit();
        }
    }

    private static void setReferentialIntegrity(Connection connection, boolean value)
        throws SQLException {
        String sql = String.format("SET REFERENTIAL_INTEGRITY %s", value);
        connection.prepareStatement(sql).execute();
    }

    private static void truncateTable(Connection connection, String table)
        throws SQLException {
        String sql = String.format("TRUNCATE TABLE %s", table);
        connection.prepareStatement(sql).execute();
    }

    private static List<String> getClearingTables(Connection connection) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet rs = metaData.getTables(null, null, "%", null);

        List<String> tables = new ArrayList<>();
        while (rs.next()) {
            String schema = rs.getString("TABLE_SCHEM");
            String table = rs.getString("TABLE_NAME");
            if (!schema.equals(SYSTEM_CATALOG_SCHEMA)) {
                tables.add(String.format("%s.%s", schema, table));
            }
        }
        return tables;
    }

}
