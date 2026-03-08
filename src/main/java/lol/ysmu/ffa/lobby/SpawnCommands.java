package lol.ysmu.ffa.lobby;

import lol.ysmu.ffa.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommands implements CommandExecutor {
    private final Main main;
    private final SpawnManager spawnManager;

    public SpawnCommands(Main main, SpawnManager spawnManager) {
        this.main = main;
        this.spawnManager = spawnManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        spawnManager.teleportToSpawn(player);
        return true;
    }
}