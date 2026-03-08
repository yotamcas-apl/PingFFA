package lol.ysmu.ffa.lobby;

import lol.ysmu.ffa.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.HashSet;
import java.util.Set;

public class VoidListener implements Listener {

    private final Main main;
    private final Set<Player> teleportedPlayers;

    public VoidListener(Main main) {
        this.main = main;
        this.teleportedPlayers = new HashSet<>();
    }
}
