package lol.ysmu.ffa.utils.gui;

import lol.ysmu.ffa.Main;
import lol.ysmu.ffa.stats.StatsManager;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class LeaderboardGUI {

    public static void open(Player player) {
        ConfigurationSection config = Main.getInstance().getLeaderboardConfig();
        if (config == null) return;

        GuiBuilder gui = new GuiBuilder(27, Main.formatColors(config.getString("title", "&8Leaderboards")));

        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta gMeta = glass.getItemMeta();
        if (gMeta != null) { gMeta.setDisplayName(" "); glass.setItemMeta(gMeta); }
        gui.fillEmptySlots(glass);

        GuiUpdater updater = new GuiUpdater();
        updater.refreshInterval(config.getLong("update-interval", 2400));

        setupItem(config, "kills", StatsManager::getTopKills, updater);
        setupItem(config, "deaths", StatsManager::getTopDeaths, updater);
        setupItem(config, "moststreak", StatsManager::getTopStreaks, updater);

        updater.startUpdating(gui);
        gui.addCloseHandler(e -> updater.stopUpdating());
        gui.open(player);
    }

    private static void setupItem(ConfigurationSection config, String key, Supplier<List<String>> data, GuiUpdater updater) {
        ConfigurationSection sec = config.getConfigurationSection("items." + key);
        if (sec == null) return;

        int slot = sec.getInt("slot");
        Material mat = Material.matchMaterial(sec.getString("material", "BARRIER"));
        String name = Main.formatColors(sec.getString("name"));
        List<String> rawLore = sec.getStringList("lore");

        updater.updateItem(slot, () -> {
            ItemStack item = new ItemStack(mat != null ? mat : Material.BARRIER);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(name);
                List<String> lore = new ArrayList<>();
                List<String> stats = data.get();
                for (String line : rawLore) {
                    if (line.contains("%top_data%")) {
                        if (stats.isEmpty()) lore.add(Main.formatColors("&7No data..."));
                        for (String s : stats) lore.add(Main.formatColors(s));
                    } else lore.add(Main.formatColors(line));
                }
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            return item;
        });
    }
}