package com.sanamo.welcomePlugin.Managers;

import com.sanamo.welcomePlugin.WelcomePlugin;
import org.bukkit.entity.Player;

public class WelcomeManager {

    private final WelcomePlugin plugin;

    public WelcomeManager(WelcomePlugin plugin) { this.plugin = plugin; }

    public void sendWelcomeMessage(Player player) {
        String message = plugin.getConfig().getString("messages.welcome");
        if (message == null) {
            plugin.debug("Missing messages.welcome config value");
            return;
        }

        player.sendMessage(plugin.getMessageUtilities().colorize(message.replace("%player_name%", player.getName())));
    }

    public void sendQuitMessage(Player player) {
        String message = plugin.getConfig().getString("messages.quit");
        if (message == null) {
            plugin.debug("Missing messages.quit config value");
            return;
        }

        player.sendMessage(plugin.getMessageUtilities().colorize(message.replace("%player_name%", player.getName())));
    }
}
