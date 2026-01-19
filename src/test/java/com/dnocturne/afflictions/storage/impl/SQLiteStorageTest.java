package com.dnocturne.afflictions.storage.impl;

import com.dnocturne.afflictions.storage.data.AfflictionData;
import com.dnocturne.afflictions.storage.data.PlayerAfflictionData;
import com.dnocturne.basalt.storage.Storage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.lang.reflect.Type;
import java.nio.file.Path;
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
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SQLiteStorage using direct JDBC without full plugin.
 * This tests the storage layer independently.
 */
@DisplayName("SQLiteStorage")
class SQLiteStorageTest {

    @TempDir
    Path tempDir;

    private TestSQLiteStorage storage;
    private File dbFile;

    @BeforeEach
    void setUp() throws Exception {
        dbFile = tempDir.resolve("test_afflictions.db").toFile();
        storage = new TestSQLiteStorage(dbFile);
        assertTrue(storage.init().get());
    }

    @AfterEach
    void tearDown() throws Exception {
        if (storage != null) {
            storage.shutdown().get();
        }
    }

    @Nested
    @DisplayName("Initialization")
    class Initialization {

        @Test
        @DisplayName("creates database file")
        void createsDatabaseFile() {
            assertTrue(dbFile.exists());
        }

        @Test
        @DisplayName("getType returns sqlite")
        void getType() {
            assertEquals("sqlite", storage.getType());
        }
    }

    @Nested
    @DisplayName("Save and Load Player")
    class SaveAndLoadPlayer {

        @Test
        @DisplayName("saves and loads player with no afflictions")
        void saveAndLoad_noAfflictions() throws ExecutionException, InterruptedException {
            UUID uuid = UUID.randomUUID();
            String username = "TestPlayer";

            PlayerAfflictionData dataToSave = new PlayerAfflictionData(uuid, username);
            storage.save(dataToSave).get();

            Optional<PlayerAfflictionData> loaded = storage.load(uuid).get();

            assertTrue(loaded.isPresent());
            assertEquals(uuid, loaded.get().uuid());
            assertEquals(username, loaded.get().username());
            assertTrue(loaded.get().afflictions().isEmpty());
        }

        @Test
        @DisplayName("saves and loads player with single affliction")
        void saveAndLoad_singleAffliction() throws ExecutionException, InterruptedException {
            UUID uuid = UUID.randomUUID();
            String username = "VampirePlayer";

            List<AfflictionData> afflictions = List.of(
                new AfflictionData("vampirism", 3, -1, 1234567890L)
            );
            PlayerAfflictionData dataToSave = new PlayerAfflictionData(uuid, username, afflictions);

            storage.save(dataToSave).get();

            Optional<PlayerAfflictionData> loaded = storage.load(uuid).get();

            assertTrue(loaded.isPresent());
            assertEquals(1, loaded.get().afflictions().size());

            AfflictionData affliction = loaded.get().afflictions().get(0);
            assertEquals("vampirism", affliction.afflictionId());
            assertEquals(3, affliction.level());
            assertEquals(-1, affliction.duration());
            assertEquals(1234567890L, affliction.contractedAt());
        }

        @Test
        @DisplayName("saves and loads player with multiple afflictions")
        void saveAndLoad_multipleAfflictions() throws ExecutionException, InterruptedException {
            UUID uuid = UUID.randomUUID();
            String username = "CursedPlayer";

            List<AfflictionData> afflictions = List.of(
                new AfflictionData("vampirism", 2, -1, 1000L),
                new AfflictionData("werewolf", 1, 5000L, 2000L),
                new AfflictionData("curse", 5, 10000L, 3000L)
            );
            PlayerAfflictionData dataToSave = new PlayerAfflictionData(uuid, username, afflictions);

            storage.save(dataToSave).get();

            Optional<PlayerAfflictionData> loaded = storage.load(uuid).get();

            assertTrue(loaded.isPresent());
            assertEquals(3, loaded.get().afflictions().size());
        }

        @Test
        @DisplayName("saves and loads custom data")
        void saveAndLoad_customData() throws ExecutionException, InterruptedException {
            UUID uuid = UUID.randomUUID();
            String username = "DataPlayer";

            Map<String, String> customData = new HashMap<>();
            customData.put("burning", "true");
            customData.put("blood_level", "50");
            customData.put("last_fed", "1234567890");

            List<AfflictionData> afflictions = List.of(
                new AfflictionData("vampirism", 1, -1, 1000L, customData)
            );
            PlayerAfflictionData dataToSave = new PlayerAfflictionData(uuid, username, afflictions);

            storage.save(dataToSave).get();

            Optional<PlayerAfflictionData> loaded = storage.load(uuid).get();

            assertTrue(loaded.isPresent());
            AfflictionData affliction = loaded.get().afflictions().get(0);
            assertEquals("true", affliction.getData("burning"));
            assertEquals("50", affliction.getData("blood_level"));
            assertEquals("1234567890", affliction.getData("last_fed"));
        }

        @Test
        @DisplayName("overwrites existing data on save")
        void save_overwritesExisting() throws ExecutionException, InterruptedException {
            UUID uuid = UUID.randomUUID();
            String username = "UpdatePlayer";

            // First save
            List<AfflictionData> afflictions1 = List.of(
                new AfflictionData("vampirism", 1, -1, 1000L)
            );
            PlayerAfflictionData data1 = new PlayerAfflictionData(uuid, username, afflictions1);
            storage.save(data1).get();

            // Second save with different data
            List<AfflictionData> afflictions2 = List.of(
                new AfflictionData("werewolf", 5, -1, 2000L)
            );
            PlayerAfflictionData data2 = new PlayerAfflictionData(uuid, username, afflictions2);
            storage.save(data2).get();

            Optional<PlayerAfflictionData> loaded = storage.load(uuid).get();

            assertTrue(loaded.isPresent());
            assertEquals(1, loaded.get().afflictions().size());
            assertEquals("werewolf", loaded.get().afflictions().get(0).afflictionId());
            assertEquals(5, loaded.get().afflictions().get(0).level());
        }

        @Test
        @DisplayName("updates username on save")
        void save_updatesUsername() throws ExecutionException, InterruptedException {
            UUID uuid = UUID.randomUUID();

            // First save with old name
            List<AfflictionData> afflictions = List.of(
                new AfflictionData("vampirism", 1, -1, 1000L)
            );
            PlayerAfflictionData data1 = new PlayerAfflictionData(uuid, "OldName", afflictions);
            storage.save(data1).get();

            // Second save with new name
            PlayerAfflictionData data2 = new PlayerAfflictionData(uuid, "NewName", afflictions);
            storage.save(data2).get();

            Optional<PlayerAfflictionData> loaded = storage.load(uuid).get();

            assertTrue(loaded.isPresent());
            assertEquals("NewName", loaded.get().username());
        }
    }

    @Nested
    @DisplayName("Load Player By Name")
    class LoadPlayerByName {

        @Test
        @DisplayName("loads player by username")
        void loadByName_exists() throws ExecutionException, InterruptedException {
            UUID uuid = UUID.randomUUID();
            String username = "NamedPlayer";

            List<AfflictionData> afflictions = List.of(
                new AfflictionData("vampirism", 2, -1, 1000L)
            );
            PlayerAfflictionData dataToSave = new PlayerAfflictionData(uuid, username, afflictions);
            storage.save(dataToSave).get();

            Optional<PlayerAfflictionData> loaded = storage.loadByName(username).get();

            assertTrue(loaded.isPresent());
            assertEquals(uuid, loaded.get().uuid());
            assertEquals(username, loaded.get().username());
            assertEquals(1, loaded.get().afflictions().size());
        }

        @Test
        @DisplayName("loads player by username case-insensitively")
        void loadByName_caseInsensitive() throws ExecutionException, InterruptedException {
            UUID uuid = UUID.randomUUID();
            String username = "CaseSensitivePlayer";

            PlayerAfflictionData dataToSave = new PlayerAfflictionData(uuid, username);
            storage.save(dataToSave).get();

            // Try different cases
            assertTrue(storage.loadByName("casesensitiveplayer").get().isPresent());
            assertTrue(storage.loadByName("CASESENSITIVEPLAYER").get().isPresent());
            assertTrue(storage.loadByName("CaseSensitivePlayer").get().isPresent());
        }

        @Test
        @DisplayName("returns empty for non-existent username")
        void loadByName_notExists() throws ExecutionException, InterruptedException {
            Optional<PlayerAfflictionData> loaded = storage.loadByName("NonExistentPlayer").get();
            assertTrue(loaded.isEmpty());
        }
    }

    @Nested
    @DisplayName("Delete Player")
    class DeletePlayer {

        @Test
        @DisplayName("deletes existing player")
        void deletePlayer_exists() throws ExecutionException, InterruptedException {
            UUID uuid = UUID.randomUUID();
            String username = "DeleteMe";

            List<AfflictionData> afflictions = List.of(
                new AfflictionData("vampirism", 1, -1, 1000L)
            );
            PlayerAfflictionData data = new PlayerAfflictionData(uuid, username, afflictions);
            storage.save(data).get();

            assertTrue(storage.exists(uuid).get());

            storage.delete(uuid).get();

            assertFalse(storage.exists(uuid).get());
            assertTrue(storage.load(uuid).get().isEmpty());
        }

        @Test
        @DisplayName("delete non-existent player does nothing")
        void deletePlayer_notExists() throws ExecutionException, InterruptedException {
            UUID uuid = UUID.randomUUID();

            // Should not throw
            storage.delete(uuid).get();

            assertFalse(storage.exists(uuid).get());
        }
    }

    @Nested
    @DisplayName("Has Player")
    class HasPlayer {

        @Test
        @DisplayName("returns true for existing player")
        void hasPlayer_exists() throws ExecutionException, InterruptedException {
            UUID uuid = UUID.randomUUID();

            PlayerAfflictionData data = new PlayerAfflictionData(uuid, "ExistingPlayer");
            storage.save(data).get();

            assertTrue(storage.exists(uuid).get());
        }

        @Test
        @DisplayName("returns false for non-existent player")
        void hasPlayer_notExists() throws ExecutionException, InterruptedException {
            UUID uuid = UUID.randomUUID();
            assertFalse(storage.exists(uuid).get());
        }
    }

    @Nested
    @DisplayName("Load Non-existent Player")
    class LoadNonExistent {

        @Test
        @DisplayName("loadPlayer returns empty for non-existent UUID")
        void loadPlayer_notExists() throws ExecutionException, InterruptedException {
            UUID uuid = UUID.randomUUID();
            Optional<PlayerAfflictionData> loaded = storage.load(uuid).get();
            assertTrue(loaded.isEmpty());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("handles empty custom data map")
        void emptyCustomData() throws ExecutionException, InterruptedException {
            UUID uuid = UUID.randomUUID();

            List<AfflictionData> afflictions = List.of(
                new AfflictionData("vampirism", 1, -1, 1000L, new HashMap<>())
            );
            PlayerAfflictionData data = new PlayerAfflictionData(uuid, "EmptyDataPlayer", afflictions);
            storage.save(data).get();

            Optional<PlayerAfflictionData> loaded = storage.load(uuid).get();

            assertTrue(loaded.isPresent());
            assertTrue(loaded.get().afflictions().get(0).data().isEmpty());
        }

        @Test
        @DisplayName("handles special characters in data values")
        void specialCharactersInData() throws ExecutionException, InterruptedException {
            UUID uuid = UUID.randomUUID();

            Map<String, String> specialData = new HashMap<>();
            specialData.put("message", "Hello \"World\"!");
            specialData.put("unicode", "\u00E9\u00E8\u00EA");
            specialData.put("newlines", "line1\nline2\nline3");

            List<AfflictionData> afflictions = List.of(
                new AfflictionData("test", 1, -1, 1000L, specialData)
            );
            PlayerAfflictionData data = new PlayerAfflictionData(uuid, "SpecialPlayer", afflictions);
            storage.save(data).get();

            Optional<PlayerAfflictionData> loaded = storage.load(uuid).get();

            assertTrue(loaded.isPresent());
            AfflictionData affliction = loaded.get().afflictions().get(0);
            assertEquals("Hello \"World\"!", affliction.getData("message"));
            assertEquals("\u00E9\u00E8\u00EA", affliction.getData("unicode"));
            assertEquals("line1\nline2\nline3", affliction.getData("newlines"));
        }

        @Test
        @DisplayName("handles zero duration")
        void zeroDuration() throws ExecutionException, InterruptedException {
            UUID uuid = UUID.randomUUID();

            List<AfflictionData> afflictions = List.of(
                new AfflictionData("test", 1, 0, 1000L)
            );
            PlayerAfflictionData data = new PlayerAfflictionData(uuid, "ZeroDuration", afflictions);
            storage.save(data).get();

            Optional<PlayerAfflictionData> loaded = storage.load(uuid).get();

            assertTrue(loaded.isPresent());
            assertEquals(0, loaded.get().afflictions().get(0).duration());
        }

        @Test
        @DisplayName("handles max level value")
        void maxLevel() throws ExecutionException, InterruptedException {
            UUID uuid = UUID.randomUUID();

            List<AfflictionData> afflictions = List.of(
                new AfflictionData("test", Integer.MAX_VALUE, -1, 1000L)
            );
            PlayerAfflictionData data = new PlayerAfflictionData(uuid, "MaxLevel", afflictions);
            storage.save(data).get();

            Optional<PlayerAfflictionData> loaded = storage.load(uuid).get();

            assertTrue(loaded.isPresent());
            assertEquals(Integer.MAX_VALUE, loaded.get().afflictions().get(0).level());
        }
    }

    /**
     * Test-friendly SQLite storage implementation that doesn't require the full plugin.
     * This is a standalone implementation for testing purposes only.
     * Mirrors the production AbstractSqlStorage/SQLiteStorage implementation.
     */
    private static class TestSQLiteStorage implements Storage<PlayerAfflictionData> {

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

        private final File dbFile;
        private final Gson gson = new Gson();
        private Connection connection;

        TestSQLiteStorage(File dbFile) {
            this.dbFile = dbFile;
        }

        @Override
        public CompletableFuture<Boolean> init() {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
                    connection = DriverManager.getConnection(url);

                    try (var stmt = connection.createStatement()) {
                        stmt.execute("PRAGMA foreign_keys = ON");
                        stmt.execute(CREATE_PLAYERS_TABLE);
                        stmt.execute(CREATE_AFFLICTIONS_TABLE);
                        stmt.execute(CREATE_INDEX);
                        stmt.execute(CREATE_USERNAME_INDEX);
                    }

                    return true;
                } catch (SQLException e) {
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
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        }

        @Override
        public CompletableFuture<Optional<PlayerAfflictionData>> load(UUID uuid) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    String checkSql = "SELECT uuid, username FROM afflicted_players WHERE uuid = ?";
                    String username;
                    try (PreparedStatement stmt = connection.prepareStatement(checkSql);
                         ResultSet rs = executeQuery(stmt, uuid.toString())) {
                        if (!rs.next()) {
                            return Optional.empty();
                        }
                        username = rs.getString("username");
                    }

                    List<AfflictionData> afflictions = loadAfflictions(uuid.toString());
                    return Optional.of(new PlayerAfflictionData(uuid, username, afflictions));
                } catch (SQLException e) {
                    e.printStackTrace();
                    return Optional.empty();
                }
            });
        }

        @Override
        public CompletableFuture<Void> save(PlayerAfflictionData data) {
            return CompletableFuture.runAsync(() -> {
                try {
                    connection.setAutoCommit(false);

                    String upsertPlayer = """
                            INSERT INTO afflicted_players (uuid, username, last_seen)
                            VALUES (?, ?, ?)
                            ON CONFLICT(uuid) DO UPDATE SET username = excluded.username, last_seen = excluded.last_seen
                            """;
                    try (PreparedStatement stmt = connection.prepareStatement(upsertPlayer)) {
                        stmt.setString(1, data.uuid().toString());
                        stmt.setString(2, data.username());
                        stmt.setLong(3, System.currentTimeMillis());
                        stmt.executeUpdate();
                    }

                    String deleteSql = "DELETE FROM player_afflictions WHERE player_uuid = ?";
                    try (PreparedStatement stmt = connection.prepareStatement(deleteSql)) {
                        stmt.setString(1, data.uuid().toString());
                        stmt.executeUpdate();
                    }

                    if (!data.afflictions().isEmpty()) {
                        String insertSql = """
                                INSERT INTO player_afflictions
                                (player_uuid, affliction_id, level, duration, contracted_at, data)
                                VALUES (?, ?, ?, ?, ?, ?)
                                """;
                        try (PreparedStatement stmt = connection.prepareStatement(insertSql)) {
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

                    connection.commit();
                } catch (SQLException e) {
                    e.printStackTrace();
                    try {
                        connection.rollback();
                    } catch (SQLException rollbackEx) {
                        rollbackEx.printStackTrace();
                    }
                } finally {
                    try {
                        connection.setAutoCommit(true);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        public CompletableFuture<Void> delete(UUID uuid) {
            return CompletableFuture.runAsync(() -> {
                try {
                    String sql = "DELETE FROM afflicted_players WHERE uuid = ?";
                    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                        stmt.setString(1, uuid.toString());
                        stmt.executeUpdate();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        }

        @Override
        public CompletableFuture<Boolean> exists(UUID uuid) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    String sql = "SELECT 1 FROM afflicted_players WHERE uuid = ?";
                    try (PreparedStatement stmt = connection.prepareStatement(sql);
                         ResultSet rs = executeQuery(stmt, uuid.toString())) {
                        return rs.next();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    return false;
                }
            });
        }

        @Override
        public CompletableFuture<Optional<PlayerAfflictionData>> loadByName(String username) {
            return CompletableFuture.supplyAsync(() -> {
                try {
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
                            e.printStackTrace();
                            return Optional.empty();
                        }
                        storedUsername = rs.getString("username");
                    }

                    List<AfflictionData> afflictions = loadAfflictions(playerUuid.toString());
                    return Optional.of(new PlayerAfflictionData(playerUuid, storedUsername, afflictions));
                } catch (SQLException e) {
                    e.printStackTrace();
                    return Optional.empty();
                }
            });
        }

        @Override
        public String getType() {
            return "sqlite";
        }

        // Helper methods matching production code

        private ResultSet executeQuery(PreparedStatement stmt, String param) throws SQLException {
            stmt.setString(1, param);
            return stmt.executeQuery();
        }

        private List<AfflictionData> loadAfflictions(String playerUuid) throws SQLException {
            String loadSql = """
                    SELECT affliction_id, level, duration, contracted_at, data
                    FROM player_afflictions
                    WHERE player_uuid = ?
                    """;

            List<AfflictionData> afflictions = new ArrayList<>();
            try (PreparedStatement stmt = connection.prepareStatement(loadSql);
                 ResultSet rs = executeQuery(stmt, playerUuid)) {

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
            return afflictions;
        }
    }
}
