package com.sanamo.welcomePlugin.Listeners;

import com.sanamo.welcomePlugin.WelcomePlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final WelcomePlugin plugin;

    public PlayerJoinListener(WelcomePlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getWelcomeManager().sendWelcomeMessage(player);
        event.setJoinMessage(null);
    }
}
