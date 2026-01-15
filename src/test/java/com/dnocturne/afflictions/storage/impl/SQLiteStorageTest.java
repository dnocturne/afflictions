package com.dnocturne.afflictions.storage.impl;

import com.dnocturne.afflictions.storage.Storage;
import com.dnocturne.afflictions.storage.data.AfflictionData;
import com.dnocturne.afflictions.storage.data.PlayerAfflictionData;
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
            storage.savePlayer(dataToSave).get();

            Optional<PlayerAfflictionData> loaded = storage.loadPlayer(uuid).get();

            assertTrue(loaded.isPresent());
            assertEquals(uuid, loaded.get().getUuid());
            assertEquals(username, loaded.get().getUsername());
            assertTrue(loaded.get().getAfflictions().isEmpty());
        }

        @Test
        @DisplayName("saves and loads player with single affliction")
        void saveAndLoad_singleAffliction() throws ExecutionException, InterruptedException {
            UUID uuid = UUID.randomUUID();
            String username = "VampirePlayer";

            PlayerAfflictionData dataToSave = new PlayerAfflictionData(uuid, username);
            dataToSave.addAffliction(new AfflictionData("vampirism", 3, -1, 1234567890L));

            storage.savePlayer(dataToSave).get();

            Optional<PlayerAfflictionData> loaded = storage.loadPlayer(uuid).get();

            assertTrue(loaded.isPresent());
            assertEquals(1, loaded.get().getAfflictions().size());

            AfflictionData affliction = loaded.get().getAfflictions().get(0);
            assertEquals("vampirism", affliction.getAfflictionId());
            assertEquals(3, affliction.getLevel());
            assertEquals(-1, affliction.getDuration());
            assertEquals(1234567890L, affliction.getContractedAt());
        }

        @Test
        @DisplayName("saves and loads player with multiple afflictions")
        void saveAndLoad_multipleAfflictions() throws ExecutionException, InterruptedException {
            UUID uuid = UUID.randomUUID();
            String username = "CursedPlayer";

            PlayerAfflictionData dataToSave = new PlayerAfflictionData(uuid, username);
            dataToSave.addAffliction(new AfflictionData("vampirism", 2, -1, 1000L));
            dataToSave.addAffliction(new AfflictionData("werewolf", 1, 5000L, 2000L));
            dataToSave.addAffliction(new AfflictionData("curse", 5, 10000L, 3000L));

            storage.savePlayer(dataToSave).get();

            Optional<PlayerAfflictionData> loaded = storage.loadPlayer(uuid).get();

            assertTrue(loaded.isPresent());
            assertEquals(3, loaded.get().getAfflictions().size());
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

            PlayerAfflictionData dataToSave = new PlayerAfflictionData(uuid, username);
            dataToSave.addAffliction(new AfflictionData("vampirism", 1, -1, 1000L, customData));

            storage.savePlayer(dataToSave).get();

            Optional<PlayerAfflictionData> loaded = storage.loadPlayer(uuid).get();

            assertTrue(loaded.isPresent());
            AfflictionData affliction = loaded.get().getAfflictions().get(0);
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
            PlayerAfflictionData data1 = new PlayerAfflictionData(uuid, username);
            data1.addAffliction(new AfflictionData("vampirism", 1, -1, 1000L));
            storage.savePlayer(data1).get();

            // Second save with different data
            PlayerAfflictionData data2 = new PlayerAfflictionData(uuid, username);
            data2.addAffliction(new AfflictionData("werewolf", 5, -1, 2000L));
            storage.savePlayer(data2).get();

            Optional<PlayerAfflictionData> loaded = storage.loadPlayer(uuid).get();

            assertTrue(loaded.isPresent());
            assertEquals(1, loaded.get().getAfflictions().size());
            assertEquals("werewolf", loaded.get().getAfflictions().get(0).getAfflictionId());
            assertEquals(5, loaded.get().getAfflictions().get(0).getLevel());
        }

        @Test
        @DisplayName("updates username on save")
        void save_updatesUsername() throws ExecutionException, InterruptedException {
            UUID uuid = UUID.randomUUID();

            // First save with old name
            PlayerAfflictionData data1 = new PlayerAfflictionData(uuid, "OldName");
            data1.addAffliction(new AfflictionData("vampirism", 1, -1, 1000L));
            storage.savePlayer(data1).get();

            // Second save with new name
            PlayerAfflictionData data2 = new PlayerAfflictionData(uuid, "NewName");
            data2.addAffliction(new AfflictionData("vampirism", 1, -1, 1000L));
            storage.savePlayer(data2).get();

            Optional<PlayerAfflictionData> loaded = storage.loadPlayer(uuid).get();

            assertTrue(loaded.isPresent());
            assertEquals("NewName", loaded.get().getUsername());
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

            PlayerAfflictionData dataToSave = new PlayerAfflictionData(uuid, username);
            dataToSave.addAffliction(new AfflictionData("vampirism", 2, -1, 1000L));
            storage.savePlayer(dataToSave).get();

            Optional<PlayerAfflictionData> loaded = storage.loadPlayerByName(username).get();

            assertTrue(loaded.isPresent());
            assertEquals(uuid, loaded.get().getUuid());
            assertEquals(username, loaded.get().getUsername());
            assertEquals(1, loaded.get().getAfflictions().size());
        }

        @Test
        @DisplayName("loads player by username case-insensitively")
        void loadByName_caseInsensitive() throws ExecutionException, InterruptedException {
            UUID uuid = UUID.randomUUID();
            String username = "CaseSensitivePlayer";

            PlayerAfflictionData dataToSave = new PlayerAfflictionData(uuid, username);
            storage.savePlayer(dataToSave).get();

            // Try different cases
            assertTrue(storage.loadPlayerByName("casesensitiveplayer").get().isPresent());
            assertTrue(storage.loadPlayerByName("CASESENSITIVEPLAYER").get().isPresent());
            assertTrue(storage.loadPlayerByName("CaseSensitivePlayer").get().isPresent());
        }

        @Test
        @DisplayName("returns empty for non-existent username")
        void loadByName_notExists() throws ExecutionException, InterruptedException {
            Optional<PlayerAfflictionData> loaded = storage.loadPlayerByName("NonExistentPlayer").get();
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

            PlayerAfflictionData data = new PlayerAfflictionData(uuid, username);
            data.addAffliction(new AfflictionData("vampirism", 1, -1, 1000L));
            storage.savePlayer(data).get();

            assertTrue(storage.hasPlayer(uuid).get());

            storage.deletePlayer(uuid).get();

            assertFalse(storage.hasPlayer(uuid).get());
            assertTrue(storage.loadPlayer(uuid).get().isEmpty());
        }

        @Test
        @DisplayName("delete non-existent player does nothing")
        void deletePlayer_notExists() throws ExecutionException, InterruptedException {
            UUID uuid = UUID.randomUUID();

            // Should not throw
            storage.deletePlayer(uuid).get();

            assertFalse(storage.hasPlayer(uuid).get());
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
            storage.savePlayer(data).get();

            assertTrue(storage.hasPlayer(uuid).get());
        }

        @Test
        @DisplayName("returns false for non-existent player")
        void hasPlayer_notExists() throws ExecutionException, InterruptedException {
            UUID uuid = UUID.randomUUID();
            assertFalse(storage.hasPlayer(uuid).get());
        }
    }

    @Nested
    @DisplayName("Load Non-existent Player")
    class LoadNonExistent {

        @Test
        @DisplayName("loadPlayer returns empty for non-existent UUID")
        void loadPlayer_notExists() throws ExecutionException, InterruptedException {
            UUID uuid = UUID.randomUUID();
            Optional<PlayerAfflictionData> loaded = storage.loadPlayer(uuid).get();
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

            PlayerAfflictionData data = new PlayerAfflictionData(uuid, "EmptyDataPlayer");
            data.addAffliction(new AfflictionData("vampirism", 1, -1, 1000L, new HashMap<>()));
            storage.savePlayer(data).get();

            Optional<PlayerAfflictionData> loaded = storage.loadPlayer(uuid).get();

            assertTrue(loaded.isPresent());
            assertTrue(loaded.get().getAfflictions().get(0).getData().isEmpty());
        }

        @Test
        @DisplayName("handles special characters in data values")
        void specialCharactersInData() throws ExecutionException, InterruptedException {
            UUID uuid = UUID.randomUUID();

            Map<String, String> specialData = new HashMap<>();
            specialData.put("message", "Hello \"World\"!");
            specialData.put("unicode", "\u00E9\u00E8\u00EA");
            specialData.put("newlines", "line1\nline2\nline3");

            PlayerAfflictionData data = new PlayerAfflictionData(uuid, "SpecialPlayer");
            data.addAffliction(new AfflictionData("test", 1, -1, 1000L, specialData));
            storage.savePlayer(data).get();

            Optional<PlayerAfflictionData> loaded = storage.loadPlayer(uuid).get();

            assertTrue(loaded.isPresent());
            AfflictionData affliction = loaded.get().getAfflictions().get(0);
            assertEquals("Hello \"World\"!", affliction.getData("message"));
            assertEquals("\u00E9\u00E8\u00EA", affliction.getData("unicode"));
            assertEquals("line1\nline2\nline3", affliction.getData("newlines"));
        }

        @Test
        @DisplayName("handles zero duration")
        void zeroDuration() throws ExecutionException, InterruptedException {
            UUID uuid = UUID.randomUUID();

            PlayerAfflictionData data = new PlayerAfflictionData(uuid, "ZeroDuration");
            data.addAffliction(new AfflictionData("test", 1, 0, 1000L));
            storage.savePlayer(data).get();

            Optional<PlayerAfflictionData> loaded = storage.loadPlayer(uuid).get();

            assertTrue(loaded.isPresent());
            assertEquals(0, loaded.get().getAfflictions().get(0).getDuration());
        }

        @Test
        @DisplayName("handles max level value")
        void maxLevel() throws ExecutionException, InterruptedException {
            UUID uuid = UUID.randomUUID();

            PlayerAfflictionData data = new PlayerAfflictionData(uuid, "MaxLevel");
            data.addAffliction(new AfflictionData("test", Integer.MAX_VALUE, -1, 1000L));
            storage.savePlayer(data).get();

            Optional<PlayerAfflictionData> loaded = storage.loadPlayer(uuid).get();

            assertTrue(loaded.isPresent());
            assertEquals(Integer.MAX_VALUE, loaded.get().getAfflictions().get(0).getLevel());
        }
    }

    /**
     * Test-friendly SQLite storage implementation that doesn't require the full plugin.
     * This is a standalone implementation for testing purposes only.
     */
    private static class TestSQLiteStorage implements Storage {

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
                    connection.createStatement().execute("PRAGMA foreign_keys = ON");

                    connection.createStatement().execute(CREATE_PLAYERS_TABLE);
                    connection.createStatement().execute(CREATE_AFFLICTIONS_TABLE);
                    connection.createStatement().execute(CREATE_INDEX);
                    connection.createStatement().execute(CREATE_USERNAME_INDEX);

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
        public CompletableFuture<Optional<PlayerAfflictionData>> loadPlayer(UUID uuid) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    String checkSql = "SELECT uuid, username FROM afflicted_players WHERE uuid = ?";
                    String username = null;
                    try (PreparedStatement stmt = connection.prepareStatement(checkSql)) {
                        stmt.setString(1, uuid.toString());
                        ResultSet rs = stmt.executeQuery();
                        if (!rs.next()) {
                            return Optional.empty();
                        }
                        username = rs.getString("username");
                    }

                    String loadSql = """
                            SELECT affliction_id, level, duration, contracted_at, data
                            FROM player_afflictions
                            WHERE player_uuid = ?
                            """;

                    List<AfflictionData> afflictions = new ArrayList<>();
                    try (PreparedStatement stmt = connection.prepareStatement(loadSql)) {
                        stmt.setString(1, uuid.toString());
                        ResultSet rs = stmt.executeQuery();

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
                    e.printStackTrace();
                    return Optional.empty();
                }
            });
        }

        @Override
        public CompletableFuture<Void> savePlayer(PlayerAfflictionData data) {
            return CompletableFuture.runAsync(() -> {
                try {
                    connection.setAutoCommit(false);

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

                    String deleteSql = "DELETE FROM player_afflictions WHERE player_uuid = ?";
                    try (PreparedStatement stmt = connection.prepareStatement(deleteSql)) {
                        stmt.setString(1, data.getUuid().toString());
                        stmt.executeUpdate();
                    }

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
        public CompletableFuture<Void> deletePlayer(UUID uuid) {
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
        public CompletableFuture<Boolean> hasPlayer(UUID uuid) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    String sql = "SELECT 1 FROM afflicted_players WHERE uuid = ?";
                    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                        stmt.setString(1, uuid.toString());
                        return stmt.executeQuery().next();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    return false;
                }
            });
        }

        @Override
        public CompletableFuture<Optional<PlayerAfflictionData>> loadPlayerByName(String username) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    String findSql = "SELECT uuid, username FROM afflicted_players WHERE username = ? COLLATE NOCASE";
                    UUID playerUuid = null;
                    String storedUsername = null;

                    try (PreparedStatement stmt = connection.prepareStatement(findSql)) {
                        stmt.setString(1, username);
                        ResultSet rs = stmt.executeQuery();
                        if (!rs.next()) {
                            return Optional.empty();
                        }
                        playerUuid = UUID.fromString(rs.getString("uuid"));
                        storedUsername = rs.getString("username");
                    }

                    String loadSql = """
                            SELECT affliction_id, level, duration, contracted_at, data
                            FROM player_afflictions
                            WHERE player_uuid = ?
                            """;

                    List<AfflictionData> afflictions = new ArrayList<>();
                    try (PreparedStatement stmt = connection.prepareStatement(loadSql)) {
                        stmt.setString(1, playerUuid.toString());
                        ResultSet rs = stmt.executeQuery();

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
                    e.printStackTrace();
                    return Optional.empty();
                }
            });
        }

        @Override
        public String getType() {
            return "sqlite";
        }
    }
}
