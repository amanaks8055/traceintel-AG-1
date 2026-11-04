package com.traceintel.database;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Checks the migrated PostgreSQL schema. Skipped when no database is configured. */
@EnabledIfEnvironmentVariable(named = "SPRING_DATASOURCE_PASSWORD", matches = ".+")
class MigrationDatabaseTest {

    @Test
    void allApplicationTablesExist() throws SQLException {
        assertEquals(19, count("""
                SELECT count(*) FROM information_schema.tables
                WHERE table_schema = 'public' AND table_name <> 'flyway_schema_history'
                """));
    }

    @Test
    void serialNumbersAreUnique() throws SQLException {
        assertEquals(1, constraints("serial_numbers", "u"));
    }

    @Test
    void lotCanOnlyBeLinkedToBatchOnce() throws SQLException {
        assertEquals(1, constraints("batch_material_consumption", "u"));
    }

    @Test
    void batchesReferenceRealParts() throws SQLException {
        assertTrue(constraints("production_batches", "f") >= 3);
    }

    @Test
    void traceIndexesExist() throws SQLException {
        assertEquals(2, count("""
                SELECT count(*) FROM pg_indexes
                WHERE indexname IN ('idx_bmc_lot', 'idx_shipment_items_batch')
                """));
    }

    private long constraints(String table, String type) throws SQLException {
        return count("SELECT count(*) FROM pg_constraint WHERE conrelid = '" + table + "'::regclass AND contype = '" + type + "'");
    }

    private long count(String sql) throws SQLException {
        try (Connection connection = DriverManager.getConnection(
                System.getenv("SPRING_DATASOURCE_URL"),
                System.getenv("SPRING_DATASOURCE_USERNAME"),
                System.getenv("SPRING_DATASOURCE_PASSWORD"));
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            result.next();
            return result.getLong(1);
        }
    }
}
