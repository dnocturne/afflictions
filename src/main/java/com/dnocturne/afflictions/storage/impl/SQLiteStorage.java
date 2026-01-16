package com.dnocturne.afflictions.storage.impl;

import com.dnocturne.afflictions.Afflictions;
import com.dnocturne.afflictions.storage.Storage;
import com.dnocturne.afflictions.storage.data.AfflictionData;
import com.dnocturne.afflictions.storage.data.PlayerAfflictionData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * SQLite storage implementation.
 */
public class SQLiteStorage implements Storage {

    private static final String CREATE_PLAYERS_TABLE = """
            CREATE TABLE IF NOT EXISTS afflicted_players (
                uuid TEXT PRIMARY KEY,
                username TEXT NOT NULL,
                last_seen INTEGER NOT NULL
            )
            """;

    private static final String CREATE_USERNAME_INDEX = """
            CREATE INDEX IF NOT EXISTS idx_afflicted_players_username
            ON afflicted_players(username COLLATE NOCASE)
            """;

    private static final String CREATE_AFFLICTIONS_TABLE = """
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

    private static final String CREATE_INDEX = """
            CREATE INDEX IF NOT EXISTS idx_player_afflictions_uuid
            ON player_afflictions(player_uuid)
            """;

    private final Afflictions plugin;
    private final Logger logger;
    private final Gson gson;
    private Connection connection;

    public SQLiteStorage(Afflictions plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.gson = new Gson();
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

                try (var stmt = connection.createStatement()) {
                    stmt.execute("PRAGMA foreign_keys = ON");
                    stmt.execute(CREATE_PLAYERS_TABLE);
                    stmt.execute(CREATE_AFFLICTIONS_TABLE);
                    stmt.execute(CREATE_INDEX);
                    stmt.execute(CREATE_USERNAME_INDEX);
                }

                // Migrate existing database if needed (add username column)
                migrateDatabase();

                logger.info("SQLite storage initialized: " + dbFile.getAbsolutePath());
                return true;
            } catch (SQLException e) {
                logger.severe("Failed to initialize SQLite storage: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        });
    }

    @Override
    public CompletableFuture<Void> shutdown() {
        return CompletableFuture.runAsync(() -> {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                    logger.info("SQLite connection closed");
                }
            } catch (SQLException e) {
                logger.severe("Error closing SQLite connection: " + e.getMessage());
            }
        });
    }

    @Override
    public CompletableFuture<Optional<PlayerAfflictionData>> loadPlayer(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Check if player exists and get username
                String checkSql = "SELECT uuid, username FROM afflicted_players WHERE uuid = ?";
                String username = null;
                try (PreparedStatement stmt = connection.prepareStatement(checkSql);
                     ResultSet rs = executeQuery(stmt, uuid.toString())) {
                    if (!rs.next()) {
                        return Optional.empty();
                    }
                    username = rs.getString("username");
                }

                // Load afflictions
                String loadSql = """
                        SELECT affliction_id, level, duration, contracted_at, data
                        FROM player_afflictions
                        WHERE player_uuid = ?
                        """;

                List<AfflictionData> afflictions = new ArrayList<>();
                try (PreparedStatement stmt = connection.prepareStatement(loadSql);
                     ResultSet rs = executeQuery(stmt, uuid.toString())) {

                    while (rs.next()) {
                        String afflictionId = rs.getString("affliction_id");
                        int level = rs.getInt("level");
                        long duration = rs.getLong("duration");
                        long contractedAt = rs.getLong("contracted_at");
                        String dataJson = rs.getString("data");

                        Map<String, String> data = new HashMap<>();
                        if (dataJson != null && !dataJson.isEmpty()) {
                            Type type = new TypeToken<Map<String, String>>() {}.getType();
                            data = gson.fromJson(dataJson, type);
                        }

                        afflictions.add(new AfflictionData(afflictionId, level, duration, contractedAt, data));
                    }
                }

                return Optional.of(new PlayerAfflictionData(uuid, username, afflictions));
            } catch (SQLException e) {
                logger.severe("Failed to load player " + uuid + ": " + e.getMessage());
                return Optional.empty();
            }
        });
    }

    @Override
    public CompletableFuture<Void> savePlayer(PlayerAfflictionData data) {
        return CompletableFuture.runAsync(() -> {
            try {
                connection.setAutoCommit(false);

                // Upsert player record
                String upsertPlayer = """
                        INSERT INTO afflicted_players (uuid, username, last_seen)
                        VALUES (?, ?, ?)
                        ON CONFLICT(uuid) DO UPDATE SET username = excluded.username, last_seen = excluded.last_seen
                        """;
                try (PreparedStatement stmt = connection.prepareStatement(upsertPlayer)) {
                    stmt.setString(1, data.getUuid().toString());
                    stmt.setString(2, data.getUsername());
                    stmt.setLong(3, System.currentTimeMillis());
                    stmt.executeUpdate();
                }

                // Delete existing afflictions
                String deleteSql = "DELETE FROM player_afflictions WHERE player_uuid = ?";
                try (PreparedStatement stmt = connection.prepareStatement(deleteSql)) {
                    stmt.setString(1, data.getUuid().toString());
                    stmt.executeUpdate();
                }

                // Insert current afflictions
                if (!data.getAfflictions().isEmpty()) {
                    String insertSql = """
                            INSERT INTO player_afflictions
                            (player_uuid, affliction_id, level, duration, contracted_at, data)
                            VALUES (?, ?, ?, ?, ?, ?)
                            """;
                    try (PreparedStatement stmt = connection.prepareStatement(insertSql)) {
                        for (AfflictionData affliction : data.getAfflictions()) {
                            stmt.setString(1, data.getUuid().toString());
                            stmt.setString(2, affliction.getAfflictionId());
                            stmt.setInt(3, affliction.getLevel());
                            stmt.setLong(4, affliction.getDuration());
                            stmt.setLong(5, affliction.getContractedAt());
                            stmt.setString(6, gson.toJson(affliction.getData()));
                            stmt.addBatch();
                        }
                        stmt.executeBatch();
                    }
                }

                connection.commit();
            } catch (SQLException e) {
                logger.severe("Failed to save player " + data.getUuid() + ": " + e.getMessage());
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    logger.severe("Rollback failed: " + rollbackEx.getMessage());
                }
            } finally {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    logger.severe("Failed to reset auto-commit: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public CompletableFuture<Void> deletePlayer(UUID uuid) {
        return CompletableFuture.runAsync(() -> {
            try {
                // Foreign key cascade will delete afflictions
                String sql = "DELETE FROM afflicted_players WHERE uuid = ?";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, uuid.toString());
                    stmt.executeUpdate();
                }
            } catch (SQLException e) {
                logger.severe("Failed to delete player " + uuid + ": " + e.getMessage());
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> hasPlayer(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String sql = "SELECT 1 FROM afflicted_players WHERE uuid = ?";
                try (PreparedStatement stmt = connection.prepareStatement(sql);
                     ResultSet rs = executeQuery(stmt, uuid.toString())) {
                    return rs.next();
                }
            } catch (SQLException e) {
                logger.severe("Failed to check player " + uuid + ": " + e.getMessage());
                return false;
            }
        });
    }

    @Override
    public CompletableFuture<Optional<PlayerAfflictionData>> loadPlayerByName(String username) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Find player by username (case-insensitive)
                String findSql = "SELECT uuid, username FROM afflicted_players WHERE username = ? COLLATE NOCASE";
                UUID playerUuid;
                String storedUsername;

                try (PreparedStatement stmt = connection.prepareStatement(findSql);
                     ResultSet rs = executeQuery(stmt, username)) {
                    if (!rs.next()) {
                        return Optional.empty();
                    }
                    String uuidString = rs.getString("uuid");
                    try {
                        playerUuid = UUID.fromString(uuidString);
                    } catch (IllegalArgumentException e) {
                        logger.severe("Invalid UUID in database: " + uuidString);
                        return Optional.empty();
                    }
                    storedUsername = rs.getString("username");
                }

                // Load afflictions
                String loadSql = """
                        SELECT affliction_id, level, duration, contracted_at, data
                        FROM player_afflictions
                        WHERE player_uuid = ?
                        """;

                List<AfflictionData> afflictions = new ArrayList<>();
                try (PreparedStatement stmt = connection.prepareStatement(loadSql);
                     ResultSet rs = executeQuery(stmt, playerUuid.toString())) {

                    while (rs.next()) {
                        String afflictionId = rs.getString("affliction_id");
                        int level = rs.getInt("level");
                        long duration = rs.getLong("duration");
                        long contractedAt = rs.getLong("contracted_at");
                        String dataJson = rs.getString("data");

                        Map<String, String> data = new HashMap<>();
                        if (dataJson != null && !dataJson.isEmpty()) {
                            Type type = new TypeToken<Map<String, String>>() {}.getType();
                            data = gson.fromJson(dataJson, type);
                        }

                        afflictions.add(new AfflictionData(afflictionId, level, duration, contractedAt, data));
                    }
                }

                return Optional.of(new PlayerAfflictionData(playerUuid, storedUsername, afflictions));
            } catch (SQLException e) {
                logger.severe("Failed to load player by name '" + username + "': " + e.getMessage());
                return Optional.empty();
            }
        });
    }

    @Override
    public String getType() {
        return "sqlite";
    }

    /**
     * Helper method to set a single string parameter and execute a query.
     * This allows the ResultSet to be used in try-with-resources.
     */
    private ResultSet executeQuery(PreparedStatement stmt, String param) throws SQLException {
        stmt.setString(1, param);
        return stmt.executeQuery();
    }

    /**
     * Migrate database schema for existing databases.
     * Adds username column if it doesn't exist.
     */
    private void migrateDatabase() {
        try (ResultSet columns = connection.getMetaData().getColumns(null, null, "afflicted_players", "username")) {
            if (!columns.next()) {
                // Column doesn't exist, add it
                logger.info("Migrating database: adding username column...");
                try (var stmt = connection.createStatement()) {
                    stmt.execute("ALTER TABLE afflicted_players ADD COLUMN username TEXT NOT NULL DEFAULT 'unknown'");
                }
                logger.info("Database migration complete");
            }
        } catch (SQLException e) {
            logger.warning("Database migration check failed: " + e.getMessage());
        }
    }
}
