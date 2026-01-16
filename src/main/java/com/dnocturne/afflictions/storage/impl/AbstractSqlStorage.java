package com.dnocturne.afflictions.storage.impl;

import com.dnocturne.afflictions.Afflictions;
import com.dnocturne.afflictions.storage.Storage;
import com.dnocturne.afflictions.storage.data.AfflictionData;
import com.dnocturne.afflictions.storage.data.PlayerAfflictionData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.sql.Connection;
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
 * Abstract base class for SQL-based storage implementations.
 * Provides common CRUD operations while allowing subclasses to define
 * dialect-specific SQL and connection handling.
 */
public abstract class AbstractSqlStorage implements Storage {

    protected final Afflictions plugin;
    protected final Logger logger;
    protected final Gson gson;
    protected Connection connection;

    protected AbstractSqlStorage(Afflictions plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.gson = new Gson();
    }

    // ============================================================
    // Abstract methods for dialect-specific SQL
    // ============================================================

    /**
     * Get the SQL for creating the players table.
     */
    protected abstract String getCreatePlayersTableSql();

    /**
     * Get the SQL for creating the afflictions table.
     */
    protected abstract String getCreateAfflictionsTableSql();

    /**
     * Get the SQL for creating indexes.
     */
    protected abstract String[] getCreateIndexesSql();

    /**
     * Get the SQL for upserting a player record.
     * Parameters: uuid, username, last_seen
     */
    protected abstract String getUpsertPlayerSql();

    /**
     * Get the SQL for case-insensitive username lookup.
     * Parameter: username
     */
    protected abstract String getFindPlayerByNameSql();

    /**
     * Called after connection is established to run any dialect-specific setup.
     */
    protected void onConnectionEstablished() throws SQLException {
        // Override if needed (e.g., SQLite PRAGMA statements)
    }

    /**
     * Perform any necessary database migrations.
     */
    protected void migrateDatabase() throws SQLException {
        // Override if needed
    }

    // ============================================================
    // Common SQL (same across dialects)
    // ============================================================

    private static final String SELECT_PLAYER_SQL =
            "SELECT uuid, username FROM afflicted_players WHERE uuid = ?";

    private static final String SELECT_AFFLICTIONS_SQL = """
            SELECT affliction_id, level, duration, contracted_at, data
            FROM player_afflictions
            WHERE player_uuid = ?
            """;

    private static final String DELETE_AFFLICTIONS_SQL =
            "DELETE FROM player_afflictions WHERE player_uuid = ?";

    private static final String INSERT_AFFLICTION_SQL = """
            INSERT INTO player_afflictions
            (player_uuid, affliction_id, level, duration, contracted_at, data)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

    private static final String DELETE_PLAYER_SQL =
            "DELETE FROM afflicted_players WHERE uuid = ?";

    private static final String HAS_PLAYER_SQL =
            "SELECT 1 FROM afflicted_players WHERE uuid = ?";

    // ============================================================
    // Storage interface implementation
    // ============================================================

    @Override
    public CompletableFuture<Void> shutdown() {
        return CompletableFuture.runAsync(() -> {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                    logger.info(getType() + " connection closed");
                }
            } catch (SQLException e) {
                logger.severe("Error closing " + getType() + " connection: " + e.getMessage());
            }
        });
    }

    @Override
    public CompletableFuture<Optional<PlayerAfflictionData>> loadPlayer(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Check if player exists and get username
                String username;
                try (PreparedStatement stmt = connection.prepareStatement(SELECT_PLAYER_SQL);
                     ResultSet rs = executeQuery(stmt, uuid.toString())) {
                    if (!rs.next()) {
                        return Optional.empty();
                    }
                    username = rs.getString("username");
                }

                List<AfflictionData> afflictions = loadAfflictions(uuid.toString());
                return Optional.of(new PlayerAfflictionData(uuid, username, afflictions));
            } catch (SQLException e) {
                logger.severe("Failed to load player " + uuid + ": " + e.getMessage());
                return Optional.empty();
            }
        });
    }

    @Override
    public CompletableFuture<Optional<PlayerAfflictionData>> loadPlayerByName(String username) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                UUID playerUuid;
                String storedUsername;

                try (PreparedStatement stmt = connection.prepareStatement(getFindPlayerByNameSql());
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

                List<AfflictionData> afflictions = loadAfflictions(playerUuid.toString());
                return Optional.of(new PlayerAfflictionData(playerUuid, storedUsername, afflictions));
            } catch (SQLException e) {
                logger.severe("Failed to load player by name '" + username + "': " + e.getMessage());
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
                try (PreparedStatement stmt = connection.prepareStatement(getUpsertPlayerSql())) {
                    stmt.setString(1, data.getUuid().toString());
                    stmt.setString(2, data.getUsername());
                    stmt.setLong(3, System.currentTimeMillis());
                    stmt.executeUpdate();
                }

                // Delete existing afflictions
                try (PreparedStatement stmt = connection.prepareStatement(DELETE_AFFLICTIONS_SQL)) {
                    stmt.setString(1, data.getUuid().toString());
                    stmt.executeUpdate();
                }

                // Insert current afflictions
                if (!data.getAfflictions().isEmpty()) {
                    try (PreparedStatement stmt = connection.prepareStatement(INSERT_AFFLICTION_SQL)) {
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
                try (PreparedStatement stmt = connection.prepareStatement(DELETE_PLAYER_SQL)) {
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
                try (PreparedStatement stmt = connection.prepareStatement(HAS_PLAYER_SQL);
                     ResultSet rs = executeQuery(stmt, uuid.toString())) {
                    return rs.next();
                }
            } catch (SQLException e) {
                logger.severe("Failed to check player " + uuid + ": " + e.getMessage());
                return false;
            }
        });
    }

    // ============================================================
    // Helper methods
    // ============================================================

    /**
     * Load afflictions for a player UUID.
     */
    private List<AfflictionData> loadAfflictions(String playerUuid) throws SQLException {
        List<AfflictionData> afflictions = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_AFFLICTIONS_SQL);
             ResultSet rs = executeQuery(stmt, playerUuid)) {

            while (rs.next()) {
                afflictions.add(parseAfflictionData(rs));
            }
        }
        return afflictions;
    }

    /**
     * Parse an AfflictionData from a ResultSet row.
     */
    private AfflictionData parseAfflictionData(ResultSet rs) throws SQLException {
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

        return new AfflictionData(afflictionId, level, duration, contractedAt, data);
    }

    /**
     * Helper method to set a single string parameter and execute a query.
     * This allows the ResultSet to be used in try-with-resources.
     */
    protected ResultSet executeQuery(PreparedStatement stmt, String param) throws SQLException {
        stmt.setString(1, param);
        return stmt.executeQuery();
    }

    /**
     * Create all tables and indexes.
     */
    protected void createTables() throws SQLException {
        try (var stmt = connection.createStatement()) {
            stmt.execute(getCreatePlayersTableSql());
            stmt.execute(getCreateAfflictionsTableSql());
            for (String indexSql : getCreateIndexesSql()) {
                stmt.execute(indexSql);
            }
        }
    }
}
