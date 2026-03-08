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

    private void broadcastStreakMessage(String playerName, int streak, String configPath) {
        List<String> messages = config.getStringList(configPath);
        for (String message : messages) {
            message = message.replace("%player%", playerName).replace("%streak%", String.valueOf(streak));
            Bukkit.broadcastMessage(formatColors(message));
        }
    }
}