package lol.ysmu.ffa.stats;

import lol.ysmu.ffa.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class StatsManager {
    private static final Connection connection = DatabaseManager.getConnection();
    private static final ConcurrentMap<UUID, PlayerStats> cache = new ConcurrentHashMap<>();

    // --- TRACKING METHODS ---
    public static void addKills(Player player, int killsToAdd) {
        PlayerStats stats = cache.computeIfAbsent(player.getUniqueId(), StatsManager::load);
        stats.kills += killsToAdd;
    }

    public static void addDeaths(Player player, int deathsToAdd) {
        PlayerStats stats = cache.computeIfAbsent(player.getUniqueId(), StatsManager::load);
        stats.deaths += deathsToAdd;
    }

    public static void updateKillStreak(Player player, int newKillStreak) {
        PlayerStats stats = cache.computeIfAbsent(player.getUniqueId(), StatsManager::load);
        stats.killStreak = newKillStreak;
        if (newKillStreak > stats.maxKillStreak) {
            stats.maxKillStreak = newKillStreak;
        }
    }

    // --- GETTERS (Fixes Placeholder & Command Errors) ---
    public static int getCurrentKills(UUID playerUUID) {
        return cache.computeIfAbsent(playerUUID, StatsManager::load).kills;
    }

    public static int getCurrentDeaths(UUID playerUUID) {
        return cache.computeIfAbsent(playerUUID, StatsManager::load).deaths;
    }

    public static int getCurrentStreak(UUID playerUUID) {
        return cache.computeIfAbsent(playerUUID, StatsManager::load).killStreak;
    }

    public static int getHighestStreak(UUID playerUUID) {
        return cache.computeIfAbsent(playerUUID, StatsManager::load).maxKillStreak;
    }

    public static double calculateKDR(UUID playerUUID) {
        PlayerStats stats = cache.computeIfAbsent(playerUUID, StatsManager::load);
        if (stats.deaths == 0) return (double) stats.kills;
        return Math.round(((double) stats.kills / stats.deaths) * 100.0) / 100.0;
    }

    // --- ADMIN EDIT METHODS ---
    public static void editKills(UUID playerUUID, int newKills) {
        cache.computeIfAbsent(playerUUID, StatsManager::load).kills = newKills;
    }

    public static void editDeaths(UUID playerUUID, int newDeaths) {
        cache.computeIfAbsent(playerUUID, StatsManager::load).deaths = newDeaths;
    }

    public static void editStreak(UUID playerUUID, int newStreak) {
        cache.computeIfAbsent(playerUUID, StatsManager::load).killStreak = newStreak;
    }

    public static void editHighestStreak(UUID playerUUID, int newHighestStreak) {
        cache.computeIfAbsent(playerUUID, StatsManager::load).maxKillStreak = newHighestStreak;
    }

    // --- LEADERBOARD & VISIBILITY ---
    public static void setHidden(UUID uuid, boolean hide) {
        try (PreparedStatement stmt = connection.prepareStatement("UPDATE player_stats SET hidden = ? WHERE uuid = ?")) {
            stmt.setInt(1, hide ? 1 : 0);
            stmt.setString(2, uuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static List<String> getTopKills() { return fetchTopData("kills"); }
    public static List<String> getTopDeaths() { return fetchTopData("deaths"); }
    public static List<String> getTopStreaks() { return fetchTopData("max_kill_streak"); }

    private static List<String> fetchTopData(String col) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT uuid, " + col + " FROM player_stats WHERE hidden = 0 ORDER BY " + col + " DESC LIMIT 10";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            int rank = 1;
            while (rs.next()) {
                String name = Bukkit.getOfflinePlayer(UUID.fromString(rs.getString("uuid"))).getName();
                list.add("&7#" + rank + " &b" + (name != null ? name : "Unknown") + " &8- &f" + rs.getInt(col));
                rank++;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // --- DATABASE OPS ---
    public static void save(UUID playerUUID) {
        PlayerStats stats = cache.get(playerUUID);
        if (stats == null) return;
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO player_stats (uuid, kills, deaths, kill_streak, max_kill_streak) VALUES (?, ?, ?, ?, ?) " +
                        "ON CONFLICT(uuid) DO UPDATE SET kills = ?, deaths = ?, kill_streak = ?, max_kill_streak = ?")) {
            stmt.setString(1, playerUUID.toString());
            stmt.setInt(2, stats.kills); stmt.setInt(3, stats.deaths);
            stmt.setInt(4, stats.killStreak); stmt.setInt(5, stats.maxKillStreak);
            stmt.setInt(6, stats.kills); stmt.setInt(7, stats.deaths);
            stmt.setInt(8, stats.killStreak); stmt.setInt(9, stats.maxKillStreak);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void saveAll() { cache.keySet().forEach(StatsManager::save); }

    public static PlayerStats load(UUID playerUUID) {
        PlayerStats stats = new PlayerStats();
        try (PreparedStatement stmt = connection.prepareStatement("SELECT kills, deaths, kill_streak, max_kill_streak FROM player_stats WHERE uuid = ?")) {
            stmt.setString(1, playerUUID.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                stats.kills = rs.getInt("kills");
                stats.deaths = rs.getInt("deaths");
                stats.killStreak = rs.getInt("kill_streak");
                stats.maxKillStreak = rs.getInt("max_kill_streak");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return stats;
    }

    public static class PlayerStats {
        public int kills = 0, deaths = 0, killStreak = 0, maxKillStreak = 0;
    }
}