package lol.ysmu.ffa.stats.edit;

import lol.ysmu.ffa.stats.StatsManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import static lol.ysmu.ffa.Main.formatColors;

public class EditStats {

    public static void editKills(Player player, int newKills) {
        StatsManager.editKills(player.getUniqueId(), newKills);
        StatsManager.save(player.getUniqueId()); // Save to DB immediately
    }

    public static void editDeaths(Player player, int newDeaths) {
        StatsManager.editDeaths(player.getUniqueId(), newDeaths);
        StatsManager.save(player.getUniqueId());
    }

    public static void editStreak(Player player, int newStreak) {
        StatsManager.editStreak(player.getUniqueId(), newStreak);
        StatsManager.save(player.getUniqueId());
    }

    public static void editHighestStreak(Player player, int newHighestStreak) {
        StatsManager.editHighestStreak(player.getUniqueId(), newHighestStreak);
        StatsManager.save(player.getUniqueId());
    }

    public static void sendInvalidCommandMessage(@NotNull CommandSender sender) {
        sender.sendMessage(formatColors("\n&b&lFFA &8| &7Invalid Command\n"));
        sender.sendMessage(formatColors("&b• &7/ffa editstats <player> <statstype> <value>\n"));
    }
}