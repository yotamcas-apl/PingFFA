package lol.ysmu.ffa.scoreboard;

import lol.ysmu.ffa.Main;
import lol.ysmu.ffa.settings.SettingsManager;
import lol.ysmu.ffa.stats.StatsManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class ScoreboardManager {

    private FileConfiguration config;

    public ScoreboardManager() {
        File file = new File(Main.getInstance().getDataFolder(), "scoreboard.yml");
        if (!file.exists()) {
            Main.getInstance().saveResource("scoreboard.yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public void updateScoreboard(Player player) {
        UUID uuid = player.getUniqueId();
        boolean privacyEnabled = SettingsManager.hasEnabledSetting(player, "PrivacyMode");

        // --- TAB LIST & PRIVACY HANDLING ---
        if (privacyEnabled) {
            // Set name in Tab to "Hidden"
            player.setPlayerListName(Main.formatColors("&7&oHidden"));

            // Optional: If you want the actual scoreboard SIDEBAR to disappear completely when Privacy is on:
            // player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            // return;
        } else {
            // Reset to real name when Privacy Mode is OFF
            player.setPlayerListName(player.getName());
        }

        // --- SCOREBOARD BUILDING ---
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        String title = config.getString("settings.title", "&bScoreboard");
        Objective obj = board.registerNewObjective("ffa", "dummy", Main.formatColors(title));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        List<String> lines = config.getStringList("lines");

        // Use "Hidden" for the %player% placeholder if privacy is active
        String nameToDisplay = privacyEnabled ? "Hidden" : player.getName();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int scoreValue = lines.size() - i;

            // Replace Placeholders
            line = line.replace("%player%", nameToDisplay)
                    .replace("%ping%", String.valueOf(player.getPing()))
                    .replace("%kills%", String.valueOf(StatsManager.getCurrentKills(uuid)))
                    .replace("%deaths%", String.valueOf(StatsManager.getCurrentDeaths(uuid)))
                    .replace("%kdr%", String.valueOf(StatsManager.calculateKDR(uuid)))
                    .replace("%streak%", String.valueOf(StatsManager.getCurrentStreak(uuid)));

            Score score = obj.getScore(Main.formatColors(line));
            score.setScore(scoreValue);
        }

        player.setScoreboard(board);
    }
}