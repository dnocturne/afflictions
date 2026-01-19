package com.dnocturne.afflictions.storage.impl;

import com.dnocturne.afflictions.Afflictions;

import java.io.File;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

/**
 * SQLite storage implementation.
 */
public class SQLiteStorage extends AbstractSqlStorage {

    public SQLiteStorage(Afflictions plugin) {
        super(plugin);
    }

    @Override
    public CompletableFuture<Boolean> init() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                File dataFolder = plugin.getDataFolder();
                if (!dataFolder.exists()) {
                    dataFolder.mkdirs();
                }

                File dbFile = new File(dataFolder, "afflictions.db");
                String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();

                connection = DriverManager.getConnection(url);
                onConnectionEstablished();
                createTables();
                migrateDatabase();

                logger.info("SQLite storage initialized: " + dbFile.getAbsolutePath());
                return true;
            } catch (SQLException e) {
                logger.severe("Failed to initialize SQLite storage: " + e.getMessage());
                logger.severe("SQL State: " + e.getSQLState() + ", Error Code: " + e.getErrorCode());
                return false;
            }
        });
    }

    @Override
    public String getType() {
        return "sqlite";
    }

    // ============================================================
    // SQLite-specific SQL
    // ============================================================

    @Override
    protected void onConnectionEstablished() throws SQLException {
        try (var stmt = connection.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
        }
    }

    @Override
    protected String getCreatePlayersTableSql() {
        return """
                CREATE TABLE IF NOT EXISTS afflicted_players (
                    uuid TEXT PRIMARY KEY,
                    username TEXT NOT NULL,
                    last_seen INTEGER NOT NULL
                )
                """;
    }

    @Override
    protected String getCreateAfflictionsTableSql() {
        return """
                CREATE TABLE IF NOT EXISTS player_afflictions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    player_uuid TEXT NOT NULL,
                    affliction_id TEXT NOT NULL,
                    level INTEGER NOT NULL DEFAULT 1,
                    duration INTEGER NOT NULL DEFAULT -1,
                    contracted_at INTEGER NOT NULL,
                    data TEXT,
                    FOREIGN KEY (player_uuid) REFERENCES afflicted_players(uuid) ON DELETE CASCADE,
                    UNIQUE(player_uuid, affliction_id)
                )
                """;
    }

    @Override
    protected String[] getCreateIndexesSql() {
        return new String[]{
                """
                CREATE INDEX IF NOT EXISTS idx_player_afflictions_uuid
                ON player_afflictions(player_uuid)
                """,
                """
                CREATE INDEX IF NOT EXISTS idx_afflicted_players_username
                ON afflicted_players(username COLLATE NOCASE)
                """
        };
    }

    @Override
    protected String getUpsertPlayerSql() {
        return """
                INSERT INTO afflicted_players (uuid, username, last_seen)
                VALUES (?, ?, ?)
                ON CONFLICT(uuid) DO UPDATE SET username = excluded.username, last_seen = excluded.last_seen
                """;
    }

    @Override
    protected String getFindPlayerByNameSql() {
        return "SELECT uuid, username FROM afflicted_players WHERE username = ? COLLATE NOCASE";
    }

    @Override
    protected void migrateDatabase() throws SQLException {
        try (ResultSet columns = connection.getMetaData().getColumns(null, null, "afflicted_players", "username")) {
            if (!columns.next()) {
                // Column doesn't exist, add it
                logger.info("Migrating database: adding username column...");
                try (var stmt = connection.createStatement()) {
                    stmt.execute("ALTER TABLE afflicted_players ADD COLUMN username TEXT NOT NULL DEFAULT 'unknown'");
                }
                logger.info("Database migration complete");
            }
        }
    }
}
