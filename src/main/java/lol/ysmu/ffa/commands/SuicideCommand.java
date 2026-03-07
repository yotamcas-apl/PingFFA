package lol.ysmu.ffa.commands;

import lol.ysmu.ffa.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static lol.ysmu.ffa.Main.formatColors;

public class SuicideCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        if (!player.hasPermission("ffa.commands.suicide")) {
            String noPermission = Main.getInstance().getConfig().getString("messages.no-permission", "&cNo Permission.");
            player.sendMessage(formatColors(noPermission));
            return true;
        }

        player.damage(player.getHealth());

        String suicideMsg = Main.getInstance().getConfig().getString("messages.i-want-to-die", "&cYou have committed suicide.");
        player.sendMessage(formatColors(Objects.requireNonNull(suicideMsg).replace("%player%", player.getName())));

        return true;
    }
}