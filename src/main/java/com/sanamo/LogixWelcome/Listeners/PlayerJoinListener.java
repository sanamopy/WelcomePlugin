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
 * This listener handles PlayerJoinEvent events fired by the Bukkit/Spigot API
 * when a player joins the server. It logs the join action to the live activity
 * feed and schedules the welcome features to be executed after a configurable
 * delay. It also clears the default join message to prevent duplicate messages.
 */

package com.sanamo.LogixWelcome.Listeners;

import com.sanamo.LogixWelcome.LogixWelcome;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

/**
 * Event listener for player join events.
 * 
 * <p>This listener handles the PlayerJoinEvent fired when a player joins the
 * server. It performs the following actions:
 * <ol>
 *   <li>Logs the join action to the live activity feed</li>
 *   <li>Schedules welcome features to execute after a configurable delay</li>
 *   <li>Clears the default Minecraft join message</li>
 * </ol>
 * </p>
 * 
 * <p>The delay before executing welcome features is configurable via
 * {@code features.join_delay} in config.yml. This delay is useful for:
 * <ul>
 *   <li>Ensuring the player is fully loaded before showing effects</li>
 *   <li>Preventing lag spikes on join</li>
 *   <li>Allowing other plugins to process the join event first</li>
 * </ul>
 * </p>
 * 
 * <p>The welcome features are executed asynchronously via BukkitRunnable
 * to prevent blocking the main server thread. The task checks if the player
 * is still online before executing to prevent errors if they disconnect
 * during the delay period.</p>
 * 
 * @author Karter Sanamo
 * @version 1.0.0
 * @since 1.0.0
 */
public class PlayerJoinListener implements Listener {

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
     * Constructs a new PlayerJoinListener instance.
     * 
     * @param plugin The main plugin instance. Must not be null.
     */
    public PlayerJoinListener(@NotNull LogixWelcome plugin) {
        this.plugin = plugin;
    }

    // ============================================
    // Event Handlers
    // ============================================
    
    /**
     * Handles the PlayerJoinEvent when a player joins the server.
     * 
     * <p>This method is called by the Bukkit event system whenever a player
     * joins the server. It performs the following operations:
     * <ol>
     *   <li>Extracts the player from the event</li>
     *   <li>Logs the join action to the live activity feed (always, regardless
     *       of feature toggles)</li>
     *   <li>Reads the join delay from configuration</li>
     *   <li>Schedules welcome features to execute after the delay</li>
     *   <li>Clears the default Minecraft join message</li>
     * </ol>
     * </p>
     * 
     * <p>The join action text is configurable via
     * {@code welcome_config.feed.join_action} in config.yml. If this value
     * is missing or empty, a default value of "joined!" is used and a warning
     * is logged.</p>
     * 
     * <p>The join delay is read from {@code features.join_delay} in config.yml.
     * If the value is negative, it is set to 0 and a warning is logged. The
     * delay is specified in server ticks (20 ticks = 1 second).</p>
     * 
     * <p>Welcome features are executed via WelcomeManager.handlePlayerJoin(),
     * which handles all welcome-related features including messages, titles,
     * sounds, effects, fireworks, kits, commands, boss bars, action bars,
     * and books.</p>
     * 
     * <p>Event Priority: This listener uses the default priority (NORMAL),
     * which means it will execute after most other plugins have processed
     * the join event. This ensures compatibility with other plugins that
     * may modify player state during join.</p>
     * 
     * @param event The PlayerJoinEvent fired when a player joins. Must not be null.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Log join action to live activity feed
        // This happens regardless of feature toggles so the feed always shows activity
        String joinAction = plugin.getConfig().getString("welcome_config.feed.join_action", "joined!");
        if (joinAction.isEmpty()) {
            plugin.getLogger().warning("Missing welcome_config.feed.join_action config value. Using default: 'joined!'");
            joinAction = "joined!";
        }
        plugin.getWelcomeManager().addFeedEntry(joinAction, player.getName());
        
        // Read join delay from configuration
        // Delay is in server ticks (20 ticks = 1 second)
        int delay = plugin.getConfig().getInt("features.join_delay", 0);
        if (delay < 0) {
            plugin.getLogger().warning("features.join_delay cannot be negative. Using default: 0");
            delay = 0;
        }

        // Schedule welcome features to execute after delay
        // This allows the player to fully load and prevents lag spikes
        new BukkitRunnable() {
            @Override
            public void run() {
                // Check if player is still online before executing
                // Prevents errors if player disconnects during delay
                if (player.isOnline()) {
                    // Execute all welcome features (messages, titles, sounds, etc.)
                    plugin.getWelcomeManager().handlePlayerJoin(player);
                }
            }
        }.runTaskLater(plugin, delay);

        // Clear default Minecraft join message
        // This prevents duplicate messages if the plugin sends its own join message
        event.setJoinMessage("");
    }
}
