package lol.ysmu.ffa.lobby;

import lol.ysmu.ffa.Main;
import lol.ysmu.ffa.spawnitems.Items;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class SpawnManager {

    private final Main main;

    public SpawnManager(Main main) {
        this.main = main;
    }

    public void giveSpawnItems(Player player) {
        player.getInventory().clear();
        Items.giveSpawnItems(player);
    }

    public void teleportToSpawn(Player player) {
        player.teleport(getSpawnLocation());
        giveSpawnItems(player);
        player.sendMessage(Main.formatColors("&aTeleported to spawn!"));
    }

    public Location getSpawnLocation() {
        String worldName = main.getConfig().getString("spawn.world", "world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) world = Bukkit.getWorlds().get(0);

        double x = main.getConfig().getDouble("spawn.x", 0.5);
        double y = main.getConfig().getDouble("spawn.y", 64.0);
        double z = main.getConfig().getDouble("spawn.z", 0.5);
        float yaw = (float) main.getConfig().getDouble("spawn.yaw", 0.0);
        float pitch = (float) main.getConfig().getDouble("spawn.pitch", 0.0);

        return new Location(world, x, y, z, yaw, pitch);
    }
}