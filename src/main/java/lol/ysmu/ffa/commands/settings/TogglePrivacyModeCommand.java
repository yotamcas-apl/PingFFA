package lol.ysmu.ffa.commands.settings;

import lol.ysmu.ffa.Main;
import lol.ysmu.ffa.settings.SettingsManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TogglePrivacyModeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Main.formatColors("&cOnly players can use this command."));
            return true;
        }

        SettingsManager.toggleSetting(player, "PrivacyMode");
        boolean enabled = SettingsManager.hasEnabledSetting(player, "PrivacyMode");

        player.sendMessage(Main.formatColors(Main.prefix + " &7Privacy Mode is now " + (enabled ? "&aEnabled" : "&cDisabled")));

        // Force immediate scoreboard/tab update
        Main.getInstance().getScoreboardManager().updateScoreboard(player);
        return true;
    }
}