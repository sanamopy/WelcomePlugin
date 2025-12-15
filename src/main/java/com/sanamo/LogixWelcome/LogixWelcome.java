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
 * This is the main plugin class that serves as the entry point for the LogixWelcome.
 * It handles plugin initialization, shutdown, command registration, listener registration,
 * and provides access to core plugin components including managers and utilities.
 * The plugin provides a comprehensive welcome system with configurable messages, titles,
 * sounds, effects, fireworks, kits, commands, boss bars, action bars, and welcome books.
 * It features a beautiful GUI configuration menu accessible via /welcomeconfig command.
 */

package com.sanamo.LogixWelcome;

import com.sanamo.LogixWelcome.Commands.WelcomeConfigCommand;
import com.sanamo.LogixWelcome.Listeners.MenuListener;
import com.sanamo.LogixWelcome.Listeners.PlayerJoinListener;
import com.sanamo.LogixWelcome.Listeners.PlayerQuitListener;
import com.sanamo.LogixWelcome.Managers.WelcomeManager;
import com.sanamo.LogixWelcome.Utilities.DataUtilities;
import com.sanamo.LogixWelcome.Utilities.MessageUtilities;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.event.Listener;

import java.util.Objects;
import java.util.logging.Level;

/**
 * Main plugin class for LogixWelcome.
 * 
 * <p>This class extends JavaPlugin and serves as the core entry point for the plugin.
 * It manages the plugin lifecycle, initializes all components, registers commands and
 * listeners, and provides access to plugin managers and utilities throughout the
 * application lifecycle.</p>
 * 
 * <p>The plugin provides a comprehensive welcome system with the following features:
 * <ul>
 *   <li>Configurable welcome and quit messages</li>
 *   <li>First-join detection and special messages</li>
 *   <li>Welcome titles and subtitles</li>
 *   <li>Sound effects on join</li>
 *   <li>Particle effects</li>
 *   <li>Fireworks display</li>
 *   <li>Welcome kits for new players</li>
 *   <li>Custom command execution</li>
 *   <li>Boss bar messages</li>
 *   <li>Action bar messages</li>
 *   <li>Welcome books with custom content</li>
 *   <li>Live activity feed in configuration GUI</li>
 *   <li>Beautiful GUI configuration menu</li>
 * </ul>
 * </p>
 * 
 * <p>All features are fully configurable via config.yml and can be toggled through
 * the in-game GUI or configuration file.</p>
 * 
 * @author Karter Sanamo
 * @version 1.0.0
 * @since 1.0.0
 */
public final class LogixWelcome extends JavaPlugin {

    // ============================================
    // Plugin State Variables
    // ============================================
    
    /**
     * Indicates whether the plugin has been successfully enabled.
     * Used to prevent operations when plugin is disabled or failed to enable.
     */
    private boolean enabled = false;
    
    /**
     * Debug mode flag. When true, additional debug messages are logged.
     * Controlled by the 'debug' setting in config.yml.
     */
    private boolean debugMode = false;

    // ============================================
    // Core Component References
    // ============================================
    
    /**
     * Manager responsible for handling all welcome-related features including
     * messages, titles, sounds, effects, fireworks, kits, commands, boss bars,
     * action bars, and books.
     */
    private WelcomeManager welcomeManager;
    
    /**
     * Utility class for message formatting and color code conversion.
     */
    private MessageUtilities messageUtilities;
    
    /**
     * Utility class for managing persistent player data, particularly
     * first-join tracking.
     */
    private DataUtilities dataUtilities;

    // ============================================
    // Plugin Lifecycle Methods
    // ============================================
    
    /**
     * Called when the plugin is enabled.
     * 
     * <p>This method performs the following initialization steps:
     * <ol>
     *   <li>Loads and validates the plugin configuration</li>
     *   <li>Registers all commands</li>
     *   <li>Registers all event listeners</li>
     *   <li>Enables plugin-specific features</li>
     *   <li>Initializes managers and utilities</li>
     * </ol>
     * </p>
     * 
     * <p>If any step fails, the plugin will be disabled and an error will be logged.</p>
     *
     */
    @Override
    public void onEnable() {
        getLogger().info("Initializing " + getName() + "...");

        try {
            // Step 1: Load and validate configuration
            saveDefaultConfig();
            reloadConfig();
            debugMode = getConfig().getBoolean("debug", false);

            // Step 2: Register commands
            registerCommands();

            // Step 3: Register event listeners
            registerListeners();

            // Step 4: Enable plugin-specific features
            enableFeatures();

            // Step 5: Initialize managers and utilities
            welcomeManager = new WelcomeManager(this);
            messageUtilities = new MessageUtilities();
            dataUtilities = new DataUtilities(this);

            enabled = true;
            getLogger().info(getName() + " enabled successfully!");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to enable " + getName(), e);
            disablePlugin();
        }
    }

    /**
     * Called when the plugin is disabled.
     * 
     * <p>This method performs cleanup operations:
     * <ol>
     *   <li>Disables plugin-specific features</li>
     *   <li>Saves all persistent data</li>
     *   <li>Sets the enabled flag to false</li>
     * </ol>
     * </p>
     * 
     * <p>Errors during shutdown are logged but do not prevent shutdown.</p>
     */
    @Override
    public void onDisable() {
        getLogger().info("Shutting down " + getName() + "...");

        try {
            disableFeatures();
            saveData();

            enabled = false;

            getLogger().info(getName() + " disabled successfully!");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error during plugin shutdown", e);
        }
    }

    // ============================================
    // Command Registration
    // ============================================
    
    /**
     * Registers all plugin commands with the server.
     * 
     * <p>Currently registers:
     * <ul>
     *   <li>/welcomeconfig - Opens the configuration GUI</li>
     * </ul>
     * </p>
     * 
     * <p>Commands are defined in plugin.yml and must be registered here
     * to be functional.</p>
     */
    private void registerCommands() {
        debug("Registering commands...");
        Objects.requireNonNull(getCommand("welcomeconfig")).setExecutor(new WelcomeConfigCommand(this));
        debug("Commands registered successfully");
    }

    // ============================================
    // Listener Registration
    // ============================================
    
    /**
     * Registers all event listeners with the server's plugin manager.
     * 
     * <p>Registers the following listeners:
     * <ul>
     *   <li>PlayerJoinListener - Handles player join events</li>
     *   <li>PlayerQuitListener - Handles player quit events</li>
     *   <li>MenuListener - Handles inventory interactions for GUI menus</li>
     * </ul>
     * </p>
     */
    private void registerListeners() {
        registerListener(new PlayerJoinListener(this));
        registerListener(new PlayerQuitListener(this));
        registerListener(new MenuListener());
        debug("Listeners registered successfully");
    }

    /**
     * Registers a single event listener with the server's plugin manager.
     * 
     * <p>This is a helper method that wraps the Bukkit listener registration
     * and adds debug logging.</p>
     * 
     * @param listener The listener instance to register. Must not be null.
     */
    private void registerListener(Listener listener) {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(listener, this);
        debug("Listener registered: " + listener.getClass().getSimpleName());
    }

    // ============================================
    // Feature Management
    // ============================================
    
    /**
     * Enables plugin-specific features during initialization.
     * 
     * <p>This method is called during plugin enable and can be used to
     * initialize any features that require setup before the plugin is
     * fully operational.</p>
     * 
     * <p>Currently, this method is a placeholder for future feature initialization.</p>
     */
    private void enableFeatures() {
        // Placeholder for future feature initialization
        debug("Plugin features enabled");
    }

    /**
     * Disables plugin-specific features during shutdown.
     * 
     * <p>This method is called during plugin disable and should be used to
     * clean up any resources or features that were initialized in enableFeatures().</p>
     * 
     * <p>Currently, this method is a placeholder for future feature cleanup.</p>
     */
    private void disableFeatures() {
        // Placeholder for future feature cleanup
        debug("Plugin features disabled");
    }

    // ============================================
    // Data Management
    // ============================================
    
    /**
     * Saves all persistent plugin data to disk.
     * 
     * <p>This method ensures that all player data, particularly first-join
     * tracking information, is persisted before the plugin shuts down.</p>
     * 
     * <p>If dataUtilities is null (plugin failed to initialize), this method
     * will safely skip saving.</p>
     */
    private void saveData() {
        if (dataUtilities != null) {
            dataUtilities.saveData();
        }
        debug("Data saved");
    }

    /**
     * Reloads the plugin configuration and all dependent components.
     * 
     * <p>This method:
     * <ol>
     *   <li>Reloads the main config.yml file</li>
     *   <li>Reloads the WelcomeManager (updates time formatter, etc.)</li>
     *   <li>Reloads the DataUtilities (reloads player data)</li>
     * </ol>
     * </p>
     * 
     * <p>This method is called when the refresh button is clicked in the
     * configuration GUI.</p>
     */
    public void reload() {
        reloadConfig();
        if (welcomeManager != null) {
            welcomeManager.reload();
        }
        if (dataUtilities != null) {
            dataUtilities.reload();
        }
        debug("Plugin configuration reloaded");
    }

    // ============================================
    // Utility Methods
    // ============================================
    
    /**
     * Logs a debug message if debug mode is enabled.
     * 
     * <p>Debug messages are prefixed with "[DEBUG]" and only logged when
     * the debug setting in config.yml is set to true.</p>
     * 
     * @param message The debug message to log. Should not be null.
     */
    public void debug(String message) {
        if (debugMode) {
            getLogger().info("[DEBUG] " + message);
        }
    }

    /**
     * Disables the plugin programmatically.
     * 
     * <p>This method is called when a critical error occurs during
     * plugin initialization to prevent the plugin from running in a
     * broken state.</p>
     */
    private void disablePlugin() {
        getServer().getPluginManager().disablePlugin(this);
    }

    // ============================================
    // Getters
    // ============================================

    /**
     * Returns the WelcomeManager instance.
     * 
     * <p>The WelcomeManager handles all welcome-related features including
     * messages, titles, sounds, effects, fireworks, kits, commands, boss bars,
     * action bars, and books.</p>
     * 
     * @return The WelcomeManager instance, or null if not initialized
     */
    public WelcomeManager getWelcomeManager() {
        return welcomeManager;
    }

    /**
     * Returns the MessageUtilities instance.
     * 
     * <p>The MessageUtilities provides methods for message formatting and
     * color code conversion.</p>
     * 
     * @return The MessageUtilities instance, or null if not initialized
     */
    public MessageUtilities getMessageUtilities() {
        return messageUtilities;
    }

    /**
     * Returns the DataUtilities instance.
     * 
     * <p>The DataUtilities manages persistent player data, particularly
     * first-join tracking.</p>
     * 
     * @return The DataUtilities instance, or null if not initialized
     */
    public DataUtilities getDataUtilities() {
        return dataUtilities;
    }
}
