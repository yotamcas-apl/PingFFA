package lol.ysmu.ffa.stats;

import lol.ysmu.ffa.Main;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.List;

import static lol.ysmu.ffa.Main.formatColors;

public class Stats implements Listener {

    private final int killStreakThreshold;
    private final FileConfiguration config;

    public Stats(FileConfiguration config) {
        this.config = config;
        this.killStreakThreshold = config.getInt("stats.streak-threshold", 5);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        StatsManager.addDeaths(victim, 1);

        int victimCurrentStreak = StatsManager.getCurrentStreak(victim.getUniqueId());

        if (victimCurrentStreak >= killStreakThreshold) {
            String endMsg = config.getString("stats.messages.streak-ended", "&6%player%'s &7streak of &b%streak% &7was ended!")
                    .replace("%player%", victim.getName())
                    .replace("%streak%", String.valueOf(victimCurrentStreak));
            Bukkit.broadcastMessage(formatColors(endMsg));
        }

        StatsManager.updateKillStreak(victim, 0);

        StatsManager.save(victim.getUniqueId());

        if (killer != null && !killer.equals(victim)) {
            StatsManager.addKills(killer, 1);
            int oldStreak = StatsManager.getCurrentStreak(killer.getUniqueId());
            int newStreak = oldStreak + 1;

            StatsManager.updateKillStreak(killer, newStreak);

            if (newStreak % killStreakThreshold == 0) {
                broadcastStreakMessage(killer.getName(), newStreak, "stats.messages.kill-streak");
            }

            StatsManager.save(killer.getUniqueId());
        }
    }

    private void broadcastStreakMessage(String playerName, int streak, String configPath) {
        if (config.isList(configPath)) {
            List<String> messages = config.getStringList(configPath);
            for (String message : messages) {
                String formatted = message.replace("%player%", playerName).replace("%streak%", String.valueOf(streak));
                Bukkit.broadcastMessage(formatColors(formatted));
            }
        } else {
            String message = config.getString(configPath, "&b%player% &7is on a &3%streak% &7kill streak!");
            String formatted = message.replace("%player%", playerName).replace("%streak%", String.valueOf(streak));
            Bukkit.broadcastMessage(formatColors(formatted));
        }
    }
}