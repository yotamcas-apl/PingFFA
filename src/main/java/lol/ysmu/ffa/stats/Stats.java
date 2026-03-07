package lol.ysmu.ffa.stats;

import lol.ysmu.ffa.Main;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static lol.ysmu.ffa.Main.formatColors;

public class Stats implements Listener {

    private final int killStreakThreshold;
    private final int deathStreakThreshold;
    private final FileConfiguration config;

    public Stats(FileConfiguration config) {
        this.config = config;
        this.killStreakThreshold = config.getInt("StreakThreshold");
        this.deathStreakThreshold = config.getInt("StreakLoseThreshold");
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();

        // We run stats updates asynchronously to prevent lag spikes
        CompletableFuture.runAsync(() -> {
            // Handle Attacker
            if (victim.getKiller() != null && !victim.getKiller().equals(victim)) {
                Player attacker = victim.getKiller();
                int currentStreak = StatsManager.getCurrentStreak(attacker.getUniqueId()) + 1;

                StatsManager.updateKillStreak(attacker, currentStreak);
                StatsManager.updateMaxKillStreak(attacker, Math.max(currentStreak, StatsManager.getHighestStreak(attacker.getUniqueId())));
                StatsManager.addKills(attacker, 1);

                if (currentStreak % this.killStreakThreshold == 0) {
                    int finalStreak = currentStreak;
                    Bukkit.getScheduler().runTask(Main.getInstance(), () ->
                            broadcastStreakMessage(attacker.getName(), finalStreak, "StreakMessage"));
                }
            }

            // Handle Victim
            int victimStreak = StatsManager.getCurrentStreak(victim.getUniqueId());
            StatsManager.updateKillStreak(victim, 0);
            StatsManager.addDeaths(victim, 1);

            if (victimStreak >= this.deathStreakThreshold) {
                int finalVictimStreak = victimStreak;
                Bukkit.getScheduler().runTask(Main.getInstance(), () ->
                        broadcastStreakMessage(victim.getName(), finalVictimStreak, "StreakLose"));
            }
        });
    }

    private void broadcastStreakMessage(String playerName, int streak, String configPath) {
        List<String> messages = config.getStringList(configPath);
        for (String message : messages) {
            message = message.replace("%player%", playerName).replace("%streak%", String.valueOf(streak));
            Bukkit.broadcastMessage(formatColors(message));
        }
    }
}