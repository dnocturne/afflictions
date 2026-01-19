package com.dnocturne.afflictions.storage.impl;

import com.dnocturne.afflictions.storage.data.AfflictionData;
import com.dnocturne.afflictions.storage.data.PlayerAfflictionData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Abstract base class for Afflictions SQL storage.
 * Extends Basalt's AbstractSqlStorage with multi-table schema support.
 *
 * <p>Uses a two-table schema:</p>
 * <ul>
 *   <li>afflicted_players - player UUID, username, last seen</li>
 *   <li>player_afflictions - affliction data with FK to players</li>
 * </ul>
 */
public abstract class AbstractSqlStorage
        extends com.dnocturne.basalt.storage.impl.AbstractSqlStorage<PlayerAfflictionData> {

    protected final Gson gson;

    protected AbstractSqlStorage(@NotNull Plugin plugin) {
        super(plugin);
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
    // Basalt AbstractSqlStorage overrides
    // ============================================================

    @Override
    protected void createTables() throws SQLException {
        try (var stmt = connection.createStatement()) {
            stmt.execute(getCreatePlayersTableSql());
            stmt.execute(getCreateAfflictionsTableSql());
            for (String indexSql : getCreateIndexesSql()) {
                stmt.execute(indexSql);
            }
        }
    }

    @Override
    protected Optional<PlayerAfflictionData> loadSync(@NotNull UUID uuid) throws SQLException {
        // Check if player exists and get username
        String username;
        try (PreparedStatement stmt = requireConnection().prepareStatement(SELECT_PLAYER_SQL)) {
            stmt.setString(1, uuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                username = rs.getString("username");
            }
        }

        List<AfflictionData> afflictions = loadAfflictions(uuid.toString());
        return Optional.of(new PlayerAfflictionData(uuid, username, afflictions));
    }

    @Override
    protected Optional<PlayerAfflictionData> loadByNameSync(@NotNull String username) throws SQLException {
        UUID playerUuid;
        String storedUsername;

        try (PreparedStatement stmt = requireConnection().prepareStatement(getFindPlayerByNameSql())) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
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
        }

        List<AfflictionData> afflictions = loadAfflictions(playerUuid.toString());
        return Optional.of(new PlayerAfflictionData(playerUuid, storedUsername, afflictions));
    }

    @Override
    protected void saveSync(@NotNull PlayerAfflictionData data) throws SQLException {
        executeInTransaction(() -> {
            // Upsert player record
            try (PreparedStatement stmt = requireConnection().prepareStatement(getUpsertPlayerSql())) {
                stmt.setString(1, data.uuid().toString());
                stmt.setString(2, data.username());
                stmt.setLong(3, System.currentTimeMillis());
                stmt.executeUpdate();
            }

            // Delete existing afflictions
            try (PreparedStatement stmt = requireConnection().prepareStatement(DELETE_AFFLICTIONS_SQL)) {
                stmt.setString(1, data.uuid().toString());
                stmt.executeUpdate();
            }

            // Insert current afflictions
            if (!data.afflictions().isEmpty()) {
                try (PreparedStatement stmt = requireConnection().prepareStatement(INSERT_AFFLICTION_SQL)) {
                    for (AfflictionData affliction : data.afflictions()) {
                        stmt.setString(1, data.uuid().toString());
                        stmt.setString(2, affliction.afflictionId());
                        stmt.setInt(3, affliction.level());
                        stmt.setLong(4, affliction.duration());
                        stmt.setLong(5, affliction.contractedAt());
                        stmt.setString(6, gson.toJson(affliction.data()));
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                }
            }
        });
    }

    @Override
    protected void deleteSync(@NotNull UUID uuid) throws SQLException {
        // Foreign key cascade will delete afflictions
        try (PreparedStatement stmt = requireConnection().prepareStatement(DELETE_PLAYER_SQL)) {
            stmt.setString(1, uuid.toString());
            stmt.executeUpdate();
        }
    }

    @Override
    protected boolean existsSync(@NotNull UUID uuid) throws SQLException {
        try (PreparedStatement stmt = requireConnection().prepareStatement(HAS_PLAYER_SQL)) {
            stmt.setString(1, uuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ============================================================
    // Helper methods
    // ============================================================

    /**
     * Load afflictions for a player UUID.
     */
    private List<AfflictionData> loadAfflictions(String playerUuid) throws SQLException {
        List<AfflictionData> afflictions = new ArrayList<>();
        try (PreparedStatement stmt = requireConnection().prepareStatement(SELECT_AFFLICTIONS_SQL)) {
            stmt.setString(1, playerUuid);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    afflictions.add(parseAfflictionData(rs));
                }
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
}
