package lol.ysmu.ffa.listeners;

import lol.ysmu.ffa.Main;
import lol.ysmu.ffa.settings.SettingsManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PrivacyChatListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        if (SettingsManager.hasEnabledSetting(event.getPlayer(), "PrivacyMode")) {
            // Option: Change their name to "Player" in the chat format
            String originalFormat = event.getFormat();
            event.setFormat(Main.formatColors("&7[Privacy] &fPlayer&7: %2$s"));
        }
    }
}