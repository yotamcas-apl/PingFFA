package lol.ysmu.ffa.lobby;

import lol.ysmu.ffa.Main;
import lol.ysmu.ffa.api.events.TeleportToSpawnEvent;
import lol.ysmu.ffa.settings.SettingsManager;
import lol.ysmu.ffa.spawnitems.Items;
import lol.ysmu.ffa.utils.MiscListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;

import static lol.ysmu.ffa.Main.formatColors;
import static lol.ysmu.ffa.Main.prefix;
import static lol.ysmu.ffa.utils.MiscListener.createQuickRespawnItem;

public class SpawnManager implements Listener {

    private static final Main main = Main.getInstance();
    private static final File configFile = new File(main.getDataFolder(), "arenas/spawn.yml");
    private static FileConfiguration spawnConfig;

    public SpawnManager() {
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            main.saveResource("arenas/spawn.yml", false);
        }
        spawnConfig = YamlConfiguration.loadConfiguration(configFile);
    }

    // Static helper for getting the spawn location (Used by teleportToSpawn)
    public static Location getSpawnLocation() {
        String worldName = spawnConfig.getString("spawn.world");
        if (worldName == null) return Bukkit.getWorlds().get(0).getSpawnLocation();

        World world = Bukkit.getWorld(worldName);
        double x = spawnConfig.getDouble("spawn.x");
        double y = spawnConfig.getDouble("spawn.y");
        double z = spawnConfig.getDouble("spawn.z");
        float yaw = (float) spawnConfig.getDouble("spawn.yaw");
        float pitch = (float) spawnConfig.getDouble("spawn.pitch");
        return new Location(world, x, y, z, yaw, pitch);
    }

    public void setSpawn(Player player) {
        Location location = player.getLocation();
        spawnConfig.set("spawn.world", location.getWorld().getName());
        spawnConfig.set("spawn.x", location.getX());
        spawnConfig.set("spawn.y", location.getY());
        spawnConfig.set("spawn.z", location.getZ());
        spawnConfig.set("spawn.yaw", location.getYaw());
        spawnConfig.set("spawn.pitch", location.getPitch());
        saveSpawnConfig();
        player.sendMessage(formatColors(prefix + "&7Spawn point has been successfully set."));
    }

    public static void teleportToSpawn(Player p) {
        Location tspawnLocation = getSpawnLocation();
        TeleportToSpawnEvent spawnTpEvent = new TeleportToSpawnEvent(p, tspawnLocation);
        Bukkit.getServer().getPluginManager().callEvent(spawnTpEvent);

        if (spawnTpEvent.isCancelled()) return;

        p.teleport(tspawnLocation);
        MiscListener.heal(p);
        Items.giveSpawnItems(p);

        if (main.getConfig().getBoolean("smoothSpawnTeleport")) {
            Bukkit.getScheduler().runTaskLater(main, () -> {
                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 8, 255, false, false));
                p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 8, 255, false, false));
            }, 2);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        teleportToSpawn(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        // --- THE GHOST FIX ---
        // We wait 1 tick (0.05 seconds) so the client is ready to receive packets again
        Bukkit.getScheduler().runTaskLater(main, () -> {
            if (!player.isOnline()) return;

            player.spigot().respawn(); // Skips the death screen
            teleportToSpawn(player); // Teleports, Heals, and gives Items

            // Handle Quick Respawn Item
            if (SettingsManager.hasEnabledSetting(player, "toggleQuickRespawn")) {
                if (main.getConfig().getBoolean("quick-respawn.enabled")) {
                    ItemStack item = createQuickRespawnItem();
                    int slot = main.getConfig().getInt("quick-respawn.slot");
                    MiscListener.giveQuickRespawn(player, item, slot);
                }
            }
        }, 1L);
    }

    private void saveSpawnConfig() {
        try {
            spawnConfig.save(configFile);
        } catch (IOException e) {
            main.getLogger().warning("Could not save spawn config.");
        }
    }
}