package lol.ysmu.ffa.commands;

import lol.ysmu.ffa.Main;
import lol.ysmu.ffa.lobby.SpawnManager;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetSpawnCommand implements CommandExecutor {
    private final Main main;
    private final SpawnManager spawnManager;

    public SetSpawnCommand(Main main, SpawnManager spawnManager) {
        this.main = main;
        this.spawnManager = spawnManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;
        if (!player.hasPermission("ffa.admin")) {
            player.sendMessage(Main.formatColors("&cNo permission."));
            return true;
        }

        Location loc = player.getLocation();
        main.getConfig().set("spawn.world", loc.getWorld().getName());
        main.getConfig().set("spawn.x", loc.getX());
        main.getConfig().set("spawn.y", loc.getY());
        main.getConfig().set("spawn.z", loc.getZ());
        main.getConfig().set("spawn.yaw", (double) loc.getYaw());
        main.getConfig().set("spawn.pitch", (double) loc.getPitch());
        main.saveConfig();

        player.sendMessage(Main.formatColors("&aSpawn location set successfully!"));
        return true;
    }
}