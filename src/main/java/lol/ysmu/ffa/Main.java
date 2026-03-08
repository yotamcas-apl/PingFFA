package lol.ysmu.ffa;

import lol.ysmu.ffa.combat.CombatTagger;
import lol.ysmu.ffa.commands.*;
import lol.ysmu.ffa.commands.settings.*;
import lol.ysmu.ffa.kits.KitManager;
import lol.ysmu.ffa.spawnitems.Items;
import lol.ysmu.ffa.stats.Stats;
import lol.ysmu.ffa.expansion.Placeholders;
import lol.ysmu.ffa.stats.StatsManager;
import lol.ysmu.ffa.tasks.ClipboardCleaner;
import lol.ysmu.ffa.tasks.UpdateTask;
import lol.ysmu.ffa.utils.MiscListener;
import lol.ysmu.ffa.utils.gui.GuiManager;
import lol.ysmu.ffa.scoreboard.ScoreboardManager;
import lol.ysmu.ffa.lobby.SpawnManager;
import lol.ysmu.ffa.lobby.SpawnCommands;
import dev.darkxx.xyriskits.api.XyrisKitsAPI;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;
import lol.ysmu.ffa.utils.gui.LeaderboardGUI;

import java.io.File;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Main extends JavaPlugin {

    private static Main instance;
    private DatabaseManager dbm;
    public static String prefix;
    private FileConfiguration config;
    private FileConfiguration leaderboardConfig;
    private Stats stats;
    private MessageCommand messageCommand;
    private static File kitsFolder;
    private CombatTagger combatTagger;
    private ScoreboardManager scoreboardManager;
    private SpawnManager spawnManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        config = getConfig();
        prefix = config.getString("prefix", "&b&lFFA &7|&r");

        DatabaseManager.connect();
        kitsFolder = KitManager.createKitsFolder();

        this.scoreboardManager = new ScoreboardManager();
        this.spawnManager = new SpawnManager(this);

        Bukkit.getWorlds().forEach(world -> world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true));

        PlaceholderAPI();
        GuiManager.register(this);
        Register();
        Commands();

        if (getServer().getPluginManager().isPluginEnabled("XyrisKits")) {
            XyrisKitsAPI.initialize();
            Bukkit.getConsoleSender().sendMessage(formatColors(prefix + "&7XyrisKits detected. &7Enabled support for &bXyrisKits &7plugin."));
        }

        setupFiles();
        startTasks();
    }

    private void setupFiles() {
        try {
            File menuDir = new File(getDataFolder(), "menus");
            if (!menuDir.exists()) menuDir.mkdirs();

            File settingsFile = new File(getDataFolder(), "menus/settings_menu.yml");
            if (!settingsFile.exists()) saveResource("menus/settings_menu.yml", false);

            File sbFile = new File(getDataFolder(), "scoreboard.yml");
            if (!sbFile.exists()) saveResource("scoreboard.yml", false);

            File lbFile = new File(getDataFolder(), "menus/leaderboard_menu.yml");
            if (!lbFile.exists()) saveResource("menus/leaderboard_menu.yml", false);
            leaderboardConfig = YamlConfiguration.loadConfiguration(lbFile);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- GETTERS ---

    public static Main getInstance() {
        return instance;
    }

    public static File getKitsFolder() {
        return kitsFolder;
    }

    public FileConfiguration getLeaderboardConfig() {
        return leaderboardConfig;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public SpawnManager getSpawnManager() {
        return spawnManager;
    }

    // --- LOGIC ---

    private void startTasks() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            Bukkit.getOnlinePlayers().forEach(player -> scoreboardManager.updateScoreboard(player));
        }, 0L, 20L);

        UpdateTask.run();
        new ClipboardCleaner().runTaskTimerAsynchronously(this, 0L, 10800L * 20L);
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage(formatColors(prefix + "&7Saving data..."));
        try { StatsManager.saveAll(); } catch (Exception e) { e.printStackTrace(); }
        try { DatabaseManager.disconnect(); } catch (Exception e) { e.printStackTrace(); }
        HandlerList.unregisterAll(this);
        getServer().getScheduler().cancelTasks(this);
    }

    private void PlaceholderAPI() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new Placeholders().register();
        } else {
            Bukkit.getScheduler().runTaskLater(this, () -> Bukkit.getPluginManager().disablePlugin(this), 20L);
        }
    }

    private void Register() {
        new Items(this);
        dbm = new DatabaseManager();

        stats = new Stats(config);
        getServer().getPluginManager().registerEvents(stats, this);
        getServer().getPluginManager().registerEvents(new MiscListener(this, spawnManager), this);

        int combatTimer = getConfig().getInt("combat-tagger.combat-timer", 10);
        combatTagger = new CombatTagger(this, combatTimer);
        getServer().getPluginManager().registerEvents(combatTagger, this);
    }

    private void Commands() {
        getCommand("suicide").setExecutor(new SuicideCommand());
        getCommand("broadcast").setExecutor(new BroadcastCommand(getConfig()));
        getCommand("rules").setExecutor(new RulesCommand(getConfig()));
        getCommand("stats").setExecutor(new StatsCommand());
        getCommand("heal").setExecutor(new HealCommand());
        getCommand("fly").setExecutor(new FlyCommand());
        getCommand("ping").setExecutor(new PingCommand());
        getCommand("settings").setExecutor(new SettingsCommand());
        getCommand("spawn").setExecutor(new SpawnCommands(this, spawnManager));
        getCommand("setspawn").setExecutor(new SetSpawnCommand(this, spawnManager));

        messageCommand = new MessageCommand(config);
        getCommand("message").setExecutor(messageCommand);
        getCommand("reply").setExecutor(new ReplyCommand(messageCommand));

        getCommand("toggleautogg").setExecutor(new ToggleAutoGGCommand());
        getCommand("togglementionsound").setExecutor(new ToggleMentionSoundCommand());
        getCommand("toggleprivatemessages").setExecutor(new TogglePrivateMessagesCommand());
        getCommand("togglequickrespawn").setExecutor(new ToggleQuickRespawnCommand());
        getCommand("toggleprivacymode").setExecutor(new TogglePrivacyModeCommand());

        getCommand("ffa").setExecutor((sender, command, label, args) -> {
            if (args.length >= 2 && args[0].equalsIgnoreCase("leaderboard")) {
                if (!sender.hasPermission("ffa.admin")) {
                    sender.sendMessage(formatColors("&cNo permission."));
                    return true;
                }
                String sub = args[1].toLowerCase();
                if (sub.equals("refresh")) {
                    StatsManager.saveAll();
                    sender.sendMessage(formatColors("&aLeaderboard data synchronized."));
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage(formatColors("&cUsage: /ffa leaderboard <ban|unban> <player>"));
                    return true;
                }
                UUID targetUUID = Bukkit.getOfflinePlayer(args[2]).getUniqueId();
                if (sub.equals("ban")) {
                    StatsManager.setHidden(targetUUID, true);
                    sender.sendMessage(formatColors("&cPlayer " + args[2] + " hidden from leaderboards."));
                } else if (sub.equals("unban")) {
                    StatsManager.setHidden(targetUUID, false);
                    sender.sendMessage(formatColors("&aPlayer " + args[2] + " restored to leaderboards."));
                }
                return true;
            }
            return true;
        });

        getCommand("leaderboard").setExecutor((sender, command, label, args) -> {
            if (sender instanceof Player player) {
                LeaderboardGUI.open(player);
            }
            return true;
        });
    }

    public static String formatColors(String message) {
        if (message == null) return "";
        message = ChatColor.translateAlternateColorCodes('&', message);
        Pattern pattern = Pattern.compile("&#([A-Fa-f0-9]{6})");
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            String colorCode = matcher.group();
            ChatColor color = ChatColor.of(colorCode.substring(1));
            message = message.replace(colorCode, color.toString());
        }
        return message;
    }
}