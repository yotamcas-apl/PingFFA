package lol.ysmu.ffa.utils;

import lol.ysmu.ffa.Main;
import lol.ysmu.ffa.lobby.SpawnManager;
import lol.ysmu.ffa.settings.SettingsManager;
import lol.ysmu.ffa.spawnitems.Items;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class MiscListener implements Listener {

    private final Main main;
    private final SpawnManager spawnManager;

    public MiscListener(Main main, SpawnManager spawnManager) {
        this.main = main;
        this.spawnManager = spawnManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        player.teleport(spawnManager.getSpawnLocation());

        spawnManager.giveSpawnItems(player);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player player = e.getPlayer();

        e.setRespawnLocation(spawnManager.getSpawnLocation());

        Bukkit.getScheduler().runTaskLater(main, () -> {
            spawnManager.giveSpawnItems(player);
        }, 1L);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player sender = event.getPlayer();
        event.getRecipients().removeIf(recipient -> {
            if (recipient.equals(sender)) return false;
            return SettingsManager.hasEnabledSetting(recipient, "PrivacyMode");
        });
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        if (main.getConfig().getBoolean("disableDeathDrops", true)) {
            e.getDrops().clear();
            e.setDroppedExp(0);
        }
    }
}