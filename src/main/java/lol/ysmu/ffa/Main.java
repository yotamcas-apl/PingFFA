package lol.ysmu.ffa;

import lol.ysmu.ffa.combat.CombatTagger;
import lol.ysmu.ffa.commands.*;
import lol.ysmu.ffa.commands.settings.*;
import lol.ysmu.ffa.kits.KitManager;
import lol.ysmu.ffa.lobby.VoidListener;
import lol.ysmu.ffa.lobby.SpawnCommands;
import lol.ysmu.ffa.lobby.SpawnManager;
import lol.ysmu.ffa.spawnitems.Items;
import lol.ysmu.ffa.stats.Stats;
import lol.ysmu.ffa.expansion.Placeholders;
import lol.ysmu.ffa.stats.StatsManager;
import lol.ysmu.ffa.tasks.ClipboardCleaner;
import lol.ysmu.ffa.tasks.UpdateTask;
import lol.ysmu.ffa.utils.MiscListener;
import lol.ysmu.ffa.utils.gui.GuiManager;
import lol.ysmu.ffa.scoreboard.ScoreboardManager; // Added this
import lol.ysmu.ffa.settings.SettingsManager; // Added this
import dev.darkxx.xyriskits.api.XyrisKitsAPI; // Updated package to your new one
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Main extends JavaPlugin {

    private static Main instance;
    private DatabaseManager dbm;
    public static String prefix;
    private FileConfiguration config;
    private Stats stats;
    private SpawnManager spawnManager;
    private MessageCommand messageCommand;
    private static File kitsFolder;
    private CombatTagger combatTagger;
    private ScoreboardManager scoreboardManager; // Added field

    @Override
    public void onEnable() {
        instance = this;

        // 1. Initialize Essential Managers
        saveDefaultConfig();
        config = getConfig();
        prefix = config.getString("prefix", "&b&lFFA &7|&r");

        // 2. Setup Data & Database
        DatabaseManager.connect();
        kitsFolder = KitManager.createKitsFolder();

        // 3. Setup Scoreboard
        this.scoreboardManager = new ScoreboardManager();

        // 4. Register Everything
        PlaceholderAPI();
        GuiManager.register(this);
        Register();
        Commands();

        // 5. External API Support
        if (getServer().getPluginManager().isPluginEnabled("XyrisKits")) {
            XyrisKitsAPI.initialize();
            Bukkit.getConsoleSender().sendMessage(formatColors(prefix + "&7XyrisKits detected. &7Enabled support for &bXyrisKits &7plugin."));
        }

        // 6. Ensure Menu Files Exist
        setupFiles();

        // 7. Start Scoreboard Update Task
        startTasks();
    }

    private void setupFiles() {
        try {
            File menuDir = new File(getDataFolder(), "menus");
            if (!menuDir.exists()) menuDir.mkdirs();

            File settingsFile = new File(getDataFolder(), "menus/settings_menu.yml");
            if (!settingsFile.exists()) {
                saveResource("menus/settings_menu.yml", false);
            }

            // Ensure scoreboard.yml is created
            File sbFile = new File(getDataFolder(), "scoreboard.yml");
            if (!sbFile.exists()) {
                saveResource("scoreboard.yml", false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startTasks() {
        // Scoreboard update task (every 1 second)
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            Bukkit.getOnlinePlayers().forEach(player -> scoreboardManager.updateScoreboard(player));
        }, 0L, 20L);

        UpdateTask.run();
        new ClipboardCleaner().runTaskTimerAsynchronously(this, 0L, 10800L * 20L);
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage(formatColors(prefix + "&7Saving data..."));

        try {
            StatsManager.saveAll();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            DatabaseManager.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        HandlerList.unregisterAll(this);
        getServer().getScheduler().cancelTasks(this);
    }

    private void PlaceholderAPI() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new Placeholders().register();
        } else {
            // ... (Your existing PAPI warning logic)
            Bukkit.getScheduler().runTaskLater(this, () -> Bukkit.getPluginManager().disablePlugin(this), 20L);
        }
    }

    private void Register() {
        new Items(this);
        dbm = new DatabaseManager();

        getCommand("ffa").setExecutor(new Commands(this));
        getCommand("ffa").setTabCompleter(new Commands(this));

        stats = new Stats(config);
        getServer().getPluginManager().registerEvents(stats, this);

        getServer().getPluginManager().registerEvents(new MiscListener(this), this);

        spawnManager = new SpawnManager();
        getServer().getPluginManager().registerEvents(spawnManager, this);
        getCommand("setspawn").setExecutor(new SpawnCommands(spawnManager, this));
        getCommand("spawn").setExecutor(new SpawnCommands(spawnManager, this));
        getServer().getPluginManager().registerEvents(new VoidListener(this), this);

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

        messageCommand = new MessageCommand(config);
        getCommand("message").setExecutor(messageCommand);
        getCommand("reply").setExecutor(new ReplyCommand(messageCommand));

        // Settings Sub-Commands
        getCommand("toggleautogg").setExecutor(new ToggleAutoGGCommand());
        getCommand("togglementionsound").setExecutor(new ToggleMentionSoundCommand());
        getCommand("toggleprivatemessages").setExecutor(new TogglePrivateMessagesCommand());
        getCommand("togglequickrespawn").setExecutor(new ToggleQuickRespawnCommand());

        // UPDATED: Privacy Mode instead of Damage Tilt
        getCommand("toggledirectionaldamagetilt").setExecutor(new TogglePrivacyModeCommand());
    }

    public static Main getInstance() {
        return instance;
    }

    public static File getKitsFolder() {
        return kitsFolder;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
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