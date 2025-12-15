/*
 * LogixWelcome - A comprehensive and feature-rich welcome plugin for Minecraft servers
 * 
 * Copyright (c) 2024 Karter Sanamo
 * 
 * This file is part of LogixWelcome. All rights reserved.
 * 
 * LogixWelcome is proprietary software. Unauthorized copying, modification,
 * distribution, or use of this software, via any medium, is strictly prohibited.
 * 
 * This software is owned by Karter Sanamo. Any use of this software without
 * explicit permission from the owner is a violation of copyright law.
 * 
 * Version: 1.0.0
 * Date: 2024
 * Author: Karter Sanamo
 * 
 * Description:
 * This listener handles PlayerQuitEvent events fired by the Bukkit/Spigot API
 * when a player leaves the server. It logs the quit action to the live activity
 * feed and sends a quit message if enabled. It also clears the default quit
 * message to prevent duplicate messages.
 */

package com.sanamo.LogixWelcome.Listeners;

import com.sanamo.LogixWelcome.LogixWelcome;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Event listener for player quit events.
 * 
 * <p>This listener handles the PlayerQuitEvent fired when a player leaves the
 * server. It performs the following actions:
 * <ol>
 *   <li>Logs the quit action to the live activity feed</li>
 *   <li>Sends a quit message if enabled in configuration</li>
 *   <li>Clears the default Minecraft quit message</li>
 * </ol>
 * </p>
 * 
 * <p>The quit action text is configurable via
 * {@code welcome_config.feed.quit_action} in config.yml. If this value is
 * missing or empty, a default value of "quit!" is used and a warning is logged.</p>
 * 
 * <p>Quit messages are controlled by the {@code features.quit_messages} setting
 * in config.yml. If enabled, a quit message is sent via WelcomeManager.sendQuitMessage().
 * The message can be broadcast to all players or sent individually, depending on
 * the {@code features.broadcast_quit} setting.</p>
 * 
 * <p>Event Priority: This listener uses the default priority (NORMAL), which
 * means it will execute after most other plugins have processed the quit event.
 * This ensures compatibility with other plugins that may modify player state
 * during quit.</p>
 * 
 * @author Karter Sanamo
 * @version 1.0.0
 * @since 1.0.0
 */
public class PlayerQuitListener implements Listener {

    // ============================================
    // Fields
    // ============================================
    
    /**
     * Reference to the main plugin instance.
     * Used for accessing plugin managers and configuration.
     */
    private final LogixWelcome plugin;

    // ============================================
    // Constructor
    // ============================================
    
    /**
     * Constructs a new PlayerQuitListener instance.
     * 
     * @param plugin The main plugin instance. Must not be null.
     */
    public PlayerQuitListener(@NotNull LogixWelcome plugin) {
        this.plugin = plugin;
    }

    // ============================================
    // Event Handlers
    // ============================================
    
    /**
     * Handles the PlayerQuitEvent when a player leaves the server.
     * 
     * <p>This method is called by the Bukkit event system whenever a player
     * leaves the server. It performs the following operations:
     * <ol>
     *   <li>Extracts the player from the event</li>
     *   <li>Logs the quit action to the live activity feed (always, regardless
     *       of feature toggles)</li>
     *   <li>Sends a quit message if enabled in configuration</li>
     *   <li>Clears the default Minecraft quit message if quit messages are enabled</li>
     * </ol>
     * </p>
     * 
     * <p>The quit action text is configurable via
     * {@code welcome_config.feed.quit_action} in config.yml. If this value
     * is missing or empty, a default value of "quit!" is used and a warning
     * is logged.</p>
     * 
     * <p>Quit messages are sent immediately when the player quits (there is no
     * delay like with join messages, as the player is already offline). The quit
     * message is handled by WelcomeManager.sendQuitMessage(), which respects the
     * broadcast settings in the configuration.</p>
     * 
     * <p>Note: The player object is still valid during this event, but the player
     * is considered offline. Attempting to send messages directly to the player
     * will not work, which is why quit messages are handled by the WelcomeManager
     * which broadcasts them to other players or logs them to console.</p>
     * 
     * @param event The PlayerQuitEvent fired when a player leaves. Must not be null.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Log quit action to live activity feed
        // This happens regardless of feature toggles so the feed always shows activity
        String quitAction = plugin.getConfig().getString("welcome_config.feed.quit_action", "quit!");
        if (quitAction.isEmpty()) {
            plugin.getLogger().warning("Missing welcome_config.feed.quit_action config value. Using default: 'quit!'");
            quitAction = "quit!";
        }
        plugin.getWelcomeManager().addFeedEntry(quitAction, player.getName());
        
        // Send quit message if enabled
        // Note: Player is already offline, so message is broadcast to other players or logged
        if (plugin.getConfig().getBoolean("features.quit_messages", true)) {
            plugin.getWelcomeManager().sendQuitMessage(player);
        }

        // Clear default Minecraft quit message if quit messages are enabled
        // This prevents duplicate messages if the plugin sends its own quit message
        if (plugin.getConfig().getBoolean("features.quit_messages", true)) {
            event.setQuitMessage("");
        }
    }
}
