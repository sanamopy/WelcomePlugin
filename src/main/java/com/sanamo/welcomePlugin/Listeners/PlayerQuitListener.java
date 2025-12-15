package com.sanamo.welcomePlugin.Listeners;

import com.sanamo.welcomePlugin.WelcomePlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final WelcomePlugin plugin;

    public PlayerQuitListener(WelcomePlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getWelcomeManager().sendQuitMessage(player);
        event.setQuitMessage(null);
    }
}
