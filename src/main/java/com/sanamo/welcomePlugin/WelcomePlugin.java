package com.sanamo.welcomePlugin;

import com.sanamo.welcomePlugin.Listeners.PlayerJoinListener;
import com.sanamo.welcomePlugin.Listeners.PlayerQuitListener;
import com.sanamo.welcomePlugin.Managers.WelcomeManager;
import com.sanamo.welcomePlugin.Utilities.MessageUtilities;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.event.Listener;
import java.util.logging.Level;
import java.util.Map;

/**
 * WelcomePlugin
 */
public final class WelcomePlugin extends JavaPlugin {

    // Plugin state
    private boolean enabled = false;
    private boolean debugMode = false;

    private WelcomeManager welcomeManager;
    private MessageUtilities messageUtilities;

    @Override
    public void onEnable() {
        getLogger().info("Initializing " + getName() + "...");

        try {
            // Step 1: Load configuration
            saveDefaultConfig();
            reloadConfig();
            debugMode = getConfig().getBoolean("debug", false);

            // Step 2: Register commands
            registerCommands();

            // Step 3: Register listeners
            registerListeners();

            // Step 4: Enable features
            enableFeatures();

            // Step 5: Initialize managers
            welcomeManager = new WelcomeManager(this);
            messageUtilities = new MessageUtilities(this);

            enabled = true;
            getLogger().info(getName() + " enabled successfully!");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to enable " + getName(), e);
            disablePlugin();
        }
    }

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

    // --------------------------
    // Commands
    // --------------------------
    private void registerCommands() {
        // TODO: Register your commands here
        debug("Commands registered");
    }

    // --------------------------
    // Listeners
    // --------------------------
    private void registerListeners() {
        registerListener(new PlayerJoinListener(this));
        registerListener(new PlayerQuitListener(this));
        debug("Listeners registered");
    }

    private void registerListener(Listener listener) {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(listener, this);
        debug("Listener registered: " + listener.getClass().getSimpleName());
    }

    // --------------------------
    // Features
    // --------------------------
    private void enableFeatures() {
        // TODO: Enable plugin-specific features here
        debug("Plugin features enabled");
    }

    private void disableFeatures() {
        // TODO: Disable plugin-specific features here
        debug("Plugin features disabled");
    }

    // --------------------------
    // Data
    // --------------------------
    private void saveData() {
        // TODO: Save plugin data here
        debug("Data saved");
    }

    // --------------------------
    // Utilities
    // --------------------------
    public void debug(String message) {
        if (debugMode) getLogger().info("[DEBUG] " + message);
    }

    private void sendMessage(String message) {
        getServer().broadcastMessage(colorize(message));
    }

    private void sendMessage(String message, Map<String, String> placeholders) {
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace(entry.getKey(), entry.getValue());
        }
        sendMessage(message);
    }

    private String colorize(String message) {
        if (message == null) return "";
        return message.replace("&", "§");
    }

    private void disablePlugin() {
        getServer().getPluginManager().disablePlugin(this);
    }

    public boolean isPluginEnabled() {
        return enabled;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    // --------------------------
    // Getters
    // --------------------------
    public WelcomeManager getWelcomeManager() {
        return welcomeManager;
    }
    public MessageUtilities getMessageUtilities() { return messageUtilities; }
}
