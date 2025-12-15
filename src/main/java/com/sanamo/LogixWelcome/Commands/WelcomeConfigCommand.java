/*
 * LogixWelcome - A comprehensive and feature-rich welcome plugin for Minecraft servers
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
 * Date: 2025
 * Author: Karter Sanamo
 * 
 * Description:
 * This command executor handles the /welcomeconfig command and provides a beautiful,
 * interactive GUI for configuring all plugin features. The GUI displays toggle buttons
 * for each feature, a live activity feed showing recent player joins/quits, and a refresh
 * button to reload configuration. All toggles update the configuration file immediately
 * and refresh the menu in place for a smooth user experience. The menu layout is
 * organized and centered, with placeholder panes filling empty slots for a clean appearance.
 */

package com.sanamo.LogixWelcome.Commands;

import com.sanamo.LogixWelcome.Managers.WelcomeManager;
import com.sanamo.LogixWelcome.Utilities.MenuUtilities;
import com.sanamo.LogixWelcome.LogixWelcome;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Command executor for the /welcomeconfig command and GUI configuration system.
 * 
 * <p>This class provides a comprehensive GUI-based configuration system that allows
 * server administrators to configure all plugin features without editing config files.
 * The GUI includes:
 * <ul>
 *   <li>Toggle buttons for all features (welcome messages, quit messages, titles,
 *       sounds, effects, fireworks, kits, commands, boss bars, action bars, books)</li>
 *   <li>Live activity feed showing recent player joins and quits</li>
 *   <li>Refresh button to reload configuration without server restart</li>
 *   <li>Beautiful, organized layout with placeholder panes</li>
 * </ul>
 * </p>
 * 
 * <p>All toggle actions:
 * <ul>
 *   <li>Update the configuration file immediately</li>
 *   <li>Save the configuration to disk</li>
 *   <li>Refresh the menu in place (no closing/reopening)</li>
 *   <li>Play a click sound for user feedback</li>
 *   <li>Display a status message to the player</li>
 * </ul>
 * </p>
 * 
 * <p>The menu uses a 6-row (54-slot) layout with:
 * <ul>
 *   <li>Row 0: Placeholder panes</li>
 *   <li>Row 1: Live activity feed (centered)</li>
 *   <li>Row 2: First row of feature toggles (5 items, centered)</li>
 *   <li>Row 3: Second row of feature toggles (5 items, centered)</li>
 *   <li>Row 4: Action bar toggle (centered)</li>
 *   <li>Row 5: Placeholder panes and refresh button (bottom right)</li>
 * </ul>
 * </p>
 * 
 * <p>Performance:
 * <ul>
 *   <li>Menu updates are done in place to avoid closing/reopening</li>
 *   <li>Config values are read with defaults to prevent null errors</li>
 *   <li>Material and sound validation with fallbacks</li>
 * </ul>
 * </p>
 * 
 * @author Karter Sanamo
 * @version 1.0.0
 * @since 1.0.0
 */
public class WelcomeConfigCommand implements CommandExecutor {

    private final LogixWelcome plugin;

    public WelcomeConfigCommand(LogixWelcome plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles the /welcomeconfig command execution.
     * 
     * <p>This method is called by Bukkit when a player executes the /welcomeconfig command.
     * It performs the following checks:
     * <ol>
     *   <li>Verifies the sender is a player (not console)</li>
     *   <li>Checks if the player has the required permission (welcome.config)</li>
     *   <li>Opens the configuration GUI if all checks pass</li>
     * </ol>
     * </p>
     * 
     * <p>If the sender is not a player or lacks permission, an appropriate error
     * message is sent and the method returns true (to prevent the default "unknown command" message).</p>
     * 
     * @param sender The command sender (player or console). Must not be null.
     * @param command The command that was executed. Must not be null.
     * @param label The alias used to execute the command. Must not be null.
     * @param args The command arguments (not used for this command). May be null or empty.
     * @return true to indicate the command was handled, false to show usage message.
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        // Check if sender is a player (not console)
        if (!(sender instanceof Player player)) {
            String message = getConfigString("messages.ran_by_player", 
                "&cThis command can only be ran by players.");
            sender.sendMessage(plugin.getMessageUtilities().colorize(message));
            return true;
        }

        // Check if player has required permission
        if (!player.hasPermission("welcome.config")) {
            String message = getConfigString("messages.missing_permissions",
                "&cYou don't have the required permissions to do that.");
            player.sendMessage(plugin.getMessageUtilities().colorize(message));
                return true;
        }

        // Open the configuration GUI
        openWelcomeConfig(player);
        return true;
    }

    /**
     * Opens the welcome configuration GUI menu for a player.
     * 
     * <p>This method creates and displays the configuration menu with all feature
     * toggles, the live activity feed, and the refresh button. The menu layout is:
     * <ul>
     *   <li>Row 0: Placeholder panes</li>
     *   <li>Row 1: Live activity feed (centered at slot 13)</li>
     *   <li>Row 2: First row of toggles (slots 20-24)</li>
     *   <li>Row 3: Second row of toggles (slots 29-33)</li>
     *   <li>Row 4: Action bar toggle (centered at slot 40)</li>
     *   <li>Row 5: Placeholder panes and refresh button (slot 53)</li>
     * </ul>
     * </p>
     * 
     * <p>The menu size is validated to ensure it's a multiple of 9 (standard
     * Minecraft inventory row size) and between 9 and 54 slots. Invalid sizes
     * are corrected to 54 (6 rows) with a warning logged.</p>
     * 
     * <p>The menu title is read from configuration and color codes are converted
     * from '&' format to '§' format for Minecraft compatibility.</p>
     * 
     * @param player The player to open the menu for. Must not be null and must be online.
     */
    private void openWelcomeConfig(@NotNull Player player) {
        // Read menu title from config and convert color codes
        String title = getConfigString("welcome_config.title", "Welcome Config");
        title = title.replace("&", "§"); // Convert & to § for menu title
        
        // Read and validate menu size
        int menuSize = plugin.getConfig().getInt("welcome_config.menu_size", 54);
        // Menu size must be a multiple of 9 (standard row size) and between 9-54
        if (menuSize < 9 || menuSize > 54 || menuSize % 9 != 0) {
            plugin.getLogger().warning("Invalid welcome_config.menu_size: " + menuSize + 
                ". Must be a multiple of 9 between 9-54. Using default: 54");
            menuSize = 54;
        }
        plugin.debug("Opening menu with size: " + menuSize);

        // Create and populate the menu
        MenuUtilities.openMenu(player, title, menuSize, menu -> {
            // Setup all menu items in order
            setupWelcomeToggle(menu, player);
            setupQuitToggle(menu, player);
            setupFirstJoinToggle(menu, player);
            setupTitlesToggle(menu, player);
            setupSoundsToggle(menu, player);
            setupEffectsToggle(menu, player);
            setupBroadcastWelcomeToggle(menu, player);
            setupFireworksToggle(menu, player);
            setupKitToggle(menu, player);
            setupBossBarToggle(menu, player);
            setupActionBarToggle(menu, player);
            setupFeedDisplay(menu, player);
            setupRefreshButton(menu, player);
            // Fill empty slots with placeholder panes
            setupPlaceholderPanes(menu);
        });
    }

    /**
     * Sets up the welcome messages toggle button in the menu.
     * 
     * <p>Creates a toggle button that allows players to enable/disable welcome
     * messages. The button displays:
     * <ul>
     *   <li>Green stained glass when enabled, red when disabled</li>
     *   <li>Appropriate name and description from configuration</li>
     *   <li>Current status (enabled/disabled)</li>
     * </ul>
     * </p>
     * 
     * <p>When clicked, the button:
     * <ol>
     *   <li>Toggles the {@code features.welcome_messages} setting</li>
     *   <li>Saves the configuration to disk</li>
     *   <li>Displays a status message to the player</li>
     *   <li>Plays a click sound</li>
     *   <li>Refreshes the menu in place</li>
     * </ol>
     * </p>
     * 
     * @param menu The menu holder to add the button to. Must not be null.
     * @param player The player viewing the menu. Must not be null.
     */
    private void setupWelcomeToggle(@NotNull MenuUtilities.MenuHolder menu, @NotNull Player player) {
        int slot = plugin.getConfig().getInt("welcome_config.welcome_toggle.slot", 20);
        boolean welcomeEnabled = plugin.getConfig().getBoolean("features.welcome_messages", true);
        
        Material welcomeMaterial = getMaterial(
            welcomeEnabled ? "welcome_config.welcome_toggle.material_enabled" : "welcome_config.welcome_toggle.material_disabled",
            welcomeEnabled ? "GREEN_STAINED_GLASS" : "RED_STAINED_GLASS"
        );
        
        String welcomeName = getConfigString(
            welcomeEnabled ? "welcome_config.welcome_toggle.name_enabled" : "welcome_config.welcome_toggle.name_disabled",
            welcomeEnabled ? "&a&lToggle Welcome Messages" : "&c&lToggle Welcome Messages"
        ).replace("&", "§");
        
        List<String> welcomeLore = buildWelcomeToggleLore(welcomeEnabled);

        // Create menu item with click handler
        menu.setItem(slot, MenuUtilities.createItem(welcomeMaterial, welcomeName, welcomeLore), event -> {
            // Toggle the feature state
            boolean current = plugin.getConfig().getBoolean("features.welcome_messages", true);
            plugin.getConfig().set("features.welcome_messages", !current);
            plugin.saveConfig(); // Save immediately to persist changes
            
            // Display status message to player
            String message = getConfigString(
                !current ? "messages.welcome_enabled" : "messages.welcome_disabled",
                !current ? "&a&l[!]&r&7 Welcome messages are now &aenabled&7." : "&c&l[!]&r&7 Welcome messages are now &cdisabled&7."
            );
            player.sendMessage(plugin.getMessageUtilities().colorize(message));
            
            // Provide audio feedback and refresh menu
            playClickSound(player, false);
            refreshMenu(player); // Refresh in place to show updated state
        });
    }

    /**
     * Builds the lore (description) for the welcome messages toggle button.
     * 
     * <p>Reads the description from configuration and appends the current status.
     * If no description is configured, uses default text.</p>
     * 
     * @param enabled Whether welcome messages are currently enabled.
     * @return A list of lore strings with color codes converted to § format.
     *         Never returns null.
     */
    @NotNull
    private List<String> buildWelcomeToggleLore(boolean enabled) {
        List<String> lore = plugin.getConfig().getStringList("welcome_config.welcome_toggle.description");
        if (lore.isEmpty()) {
            lore = new ArrayList<>();
            lore.add("&7The welcome message is a greeting for any");
            lore.add("&7player coming online. Click here to toggle it.");
            lore.add("&7");
        }
        
        String status = getConfigString(
            enabled ? "welcome_config.welcome_toggle.status_enabled" : "welcome_config.welcome_toggle.status_disabled",
            enabled ? "&a&lStatus: Enabled" : "&c&lStatus: Disabled"
        );
        lore.add(status);
        
        return colorizeList(lore);
    }

    /**
     * Sets up the quit messages toggle button in the menu.
     * 
     * <p>Creates a toggle button for enabling/disabling quit messages.
     * Functionality is identical to setupWelcomeToggle() but for quit messages.</p>
     * 
     * @param menu The menu holder to add the button to. Must not be null.
     * @param player The player viewing the menu. Must not be null.
     */
    private void setupQuitToggle(@NotNull MenuUtilities.MenuHolder menu, @NotNull Player player) {
        int slot = plugin.getConfig().getInt("welcome_config.quit_toggle.slot", 21);
        boolean quitEnabled = plugin.getConfig().getBoolean("features.quit_messages", true);
        
        Material quitMaterial = getMaterial(
            quitEnabled ? "welcome_config.quit_toggle.material_enabled" : "welcome_config.quit_toggle.material_disabled",
            quitEnabled ? "GREEN_STAINED_GLASS" : "RED_STAINED_GLASS"
        );
        
        String quitName = getConfigString(
            quitEnabled ? "welcome_config.quit_toggle.name_enabled" : "welcome_config.quit_toggle.name_disabled",
            quitEnabled ? "&a&lToggle Quit Messages" : "&c&lToggle Quit Messages"
        ).replace("&", "§");
        
        List<String> quitLore = buildQuitToggleLore(quitEnabled);

        menu.setItem(slot, MenuUtilities.createItem(quitMaterial, quitName, quitLore), event -> {
            boolean current = plugin.getConfig().getBoolean("features.quit_messages", true);
            plugin.getConfig().set("features.quit_messages", !current);
            plugin.saveConfig();
            
            String message = getConfigString(
                !current ? "messages.quit_enabled" : "messages.quit_disabled",
                !current ? "&a&l[!]&r&7 Quit messages are now &aenabled&7." : "&c&l[!]&r&7 Quit messages are now &cdisabled&7."
            );
            player.sendMessage(plugin.getMessageUtilities().colorize(message));
            
            playClickSound(player, false);
            refreshMenu(player);
        });
    }

    /**
     * Builds the lore for the quit messages toggle button.
     * 
     * @param enabled Whether quit messages are currently enabled.
     * @return A list of lore strings with color codes converted. Never returns null.
     */
    @NotNull
    private List<String> buildQuitToggleLore(boolean enabled) {
        List<String> lore = plugin.getConfig().getStringList("welcome_config.quit_toggle.description");
        if (lore.isEmpty()) {
            lore = new ArrayList<>();
            lore.add("&7The quit message is a goodbye to any");
            lore.add("&7player who leaves. Click here to toggle it.");
            lore.add("&7");
        }
        
        String status = getConfigString(
            enabled ? "welcome_config.quit_toggle.status_enabled" : "welcome_config.quit_toggle.status_disabled",
            enabled ? "&a&lStatus: Enabled" : "&c&lStatus: Disabled"
        );
        lore.add(status);
        
        return colorizeList(lore);
    }

    /**
     * Sets up the first-join messages toggle button in the menu.
     * 
     * <p>Creates a toggle button for enabling/disabling special messages
     * shown only to players joining for the first time.</p>
     * 
     * @param menu The menu holder to add the button to. Must not be null.
     * @param player The player viewing the menu. Must not be null.
     */
    private void setupFirstJoinToggle(@NotNull MenuUtilities.MenuHolder menu, @NotNull Player player) {
        int slot = plugin.getConfig().getInt("welcome_config.first_join_toggle.slot", 22);
        boolean enabled = plugin.getConfig().getBoolean("features.first_join_messages", true);
        
        Material material = getMaterial(
            enabled ? "welcome_config.first_join_toggle.material_enabled" : "welcome_config.first_join_toggle.material_disabled",
            enabled ? "GOLD_BLOCK" : "GRAY_CONCRETE"
        );
        
        String name = getConfigString(
            enabled ? "welcome_config.first_join_toggle.name_enabled" : "welcome_config.first_join_toggle.name_disabled",
            enabled ? "&e&lToggle First Join Messages" : "&7&lToggle First Join Messages"
        ).replace("&", "§");
        
        List<String> lore = plugin.getConfig().getStringList("welcome_config.first_join_toggle.description");
        if (lore.isEmpty()) {
            lore = new ArrayList<>();
            lore.add("&7Special message for players joining");
            lore.add("&7for the first time. Click to toggle.");
            lore.add("&7");
        }
        
        String status = getConfigString(
            enabled ? "welcome_config.first_join_toggle.status_enabled" : "welcome_config.first_join_toggle.status_disabled",
            enabled ? "&a&lStatus: Enabled" : "&c&lStatus: Disabled"
        );
        lore.add(status);
        lore = colorizeList(lore);

        menu.setItem(slot, MenuUtilities.createItem(material, name, lore), event -> {
            boolean current = plugin.getConfig().getBoolean("features.first_join_messages", true);
            plugin.getConfig().set("features.first_join_messages", !current);
            plugin.saveConfig();
            
            String message = getConfigString(
                !current ? "messages.first_join_enabled" : "messages.first_join_disabled",
                !current ? "&a&l[!]&r&7 First join messages are now &aenabled&7." : "&c&l[!]&r&7 First join messages are now &cdisabled&7."
            );
            player.sendMessage(plugin.getMessageUtilities().colorize(message));
            
            playClickSound(player, false);
            refreshMenu(player);
        });
    }

    /**
     * Sets up the welcome titles toggle button in the menu.
     * 
     * <p>Creates a toggle button for enabling/disabling welcome titles and subtitles.</p>
     * 
     * @param menu The menu holder to add the button to. Must not be null.
     * @param player The player viewing the menu. Must not be null.
     */
    private void setupTitlesToggle(@NotNull MenuUtilities.MenuHolder menu, @NotNull Player player) {
        int slot = plugin.getConfig().getInt("welcome_config.titles_toggle.slot", 23);
        boolean enabled = plugin.getConfig().getBoolean("features.welcome_titles", true);
        
        Material material = getMaterial(
            enabled ? "welcome_config.titles_toggle.material_enabled" : "welcome_config.titles_toggle.material_disabled",
            enabled ? "PAPER" : "BARRIER"
        );
        
        String name = getConfigString(
            enabled ? "welcome_config.titles_toggle.name_enabled" : "welcome_config.titles_toggle.name_disabled",
            enabled ? "&b&lToggle Welcome Titles" : "&7&lToggle Welcome Titles"
        ).replace("&", "§");
        
        List<String> lore = plugin.getConfig().getStringList("welcome_config.titles_toggle.description");
        if (lore.isEmpty()) {
            lore = new ArrayList<>();
            lore.add("&7Shows a title and subtitle when");
            lore.add("&7players join the server. Click to toggle.");
            lore.add("&7");
        }
        
        String status = getConfigString(
            enabled ? "welcome_config.titles_toggle.status_enabled" : "welcome_config.titles_toggle.status_disabled",
            enabled ? "&a&lStatus: Enabled" : "&c&lStatus: Disabled"
        );
        lore.add(status);
        lore = colorizeList(lore);

        menu.setItem(slot, MenuUtilities.createItem(material, name, lore), event -> {
            boolean current = plugin.getConfig().getBoolean("features.welcome_titles", true);
            plugin.getConfig().set("features.welcome_titles", !current);
            plugin.saveConfig();
            
            String message = getConfigString(
                !current ? "messages.welcome_titles_enabled" : "messages.welcome_titles_disabled",
                !current ? "&a&l[!]&r&7 Welcome titles are now &aenabled&7." : "&c&l[!]&r&7 Welcome titles are now &cdisabled&7."
            );
            player.sendMessage(plugin.getMessageUtilities().colorize(message));
            
            playClickSound(player, false);
            refreshMenu(player);
        });
    }

    /**
     * Sets up the welcome sounds toggle button in the menu.
     * 
     * <p>Creates a toggle button for enabling/disabling welcome sound effects.</p>
     * 
     * @param menu The menu holder to add the button to. Must not be null.
     * @param player The player viewing the menu. Must not be null.
     */
    private void setupSoundsToggle(@NotNull MenuUtilities.MenuHolder menu, @NotNull Player player) {
        int slot = plugin.getConfig().getInt("welcome_config.sounds_toggle.slot", 24);
        boolean enabled = plugin.getConfig().getBoolean("features.welcome_sounds", true);
        
        Material material = getMaterial(
            enabled ? "welcome_config.sounds_toggle.material_enabled" : "welcome_config.sounds_toggle.material_disabled",
            enabled ? "NOTE_BLOCK" : "MUSIC_DISC_STAL"
        );
        
        String name = getConfigString(
            enabled ? "welcome_config.sounds_toggle.name_enabled" : "welcome_config.sounds_toggle.name_disabled",
            enabled ? "&d&lToggle Welcome Sounds" : "&7&lToggle Welcome Sounds"
        ).replace("&", "§");
        
        List<String> lore = plugin.getConfig().getStringList("welcome_config.sounds_toggle.description");
        if (lore.isEmpty()) {
            lore = new ArrayList<>();
            lore.add("&7Plays a sound when players join.");
            lore.add("&7Click to toggle.");
            lore.add("&7");
        }
        
        String status = getConfigString(
            enabled ? "welcome_config.sounds_toggle.status_enabled" : "welcome_config.sounds_toggle.status_disabled",
            enabled ? "&a&lStatus: Enabled" : "&c&lStatus: Disabled"
        );
        lore.add(status);
        lore = colorizeList(lore);

        menu.setItem(slot, MenuUtilities.createItem(material, name, lore), event -> {
            boolean current = plugin.getConfig().getBoolean("features.welcome_sounds", true);
            plugin.getConfig().set("features.welcome_sounds", !current);
            plugin.saveConfig();
            
            String message = getConfigString(
                !current ? "messages.sounds_enabled" : "messages.sounds_disabled",
                !current ? "&a&l[!]&r&7 Welcome sounds are now &aenabled&7." : "&c&l[!]&r&7 Welcome sounds are now &cdisabled&7."
            );
            player.sendMessage(plugin.getMessageUtilities().colorize(message));
            
            playClickSound(player, false);
            refreshMenu(player);
        });
    }

    /**
     * Sets up the welcome effects (particles) toggle button in the menu.
     * 
     * <p>Creates a toggle button for enabling/disabling particle effects on join.</p>
     * 
     * @param menu The menu holder to add the button to. Must not be null.
     * @param player The player viewing the menu. Must not be null.
     */
    private void setupEffectsToggle(@NotNull MenuUtilities.MenuHolder menu, @NotNull Player player) {
        int slot = plugin.getConfig().getInt("welcome_config.effects_toggle.slot", 29);
        boolean enabled = plugin.getConfig().getBoolean("features.welcome_effects", true);
        
        Material material = getMaterial(
            enabled ? "welcome_config.effects_toggle.material_enabled" : "welcome_config.effects_toggle.material_disabled",
            enabled ? "FIREWORK_ROCKET" : "GUNPOWDER"
        );
        
        String name = getConfigString(
            enabled ? "welcome_config.effects_toggle.name_enabled" : "welcome_config.effects_toggle.name_disabled",
            enabled ? "&6&lToggle Welcome Effects" : "&7&lToggle Welcome Effects"
        ).replace("&", "§");
        
        List<String> lore = plugin.getConfig().getStringList("welcome_config.effects_toggle.description");
        if (lore.isEmpty()) {
            lore = new ArrayList<>();
            lore.add("&7Shows particle effects when");
            lore.add("&7players join. Click to toggle.");
            lore.add("&7");
        }
        
        String status = getConfigString(
            enabled ? "welcome_config.effects_toggle.status_enabled" : "welcome_config.effects_toggle.status_disabled",
            enabled ? "&a&lStatus: Enabled" : "&c&lStatus: Disabled"
        );
        lore.add(status);
        lore = colorizeList(lore);

        menu.setItem(slot, MenuUtilities.createItem(material, name, lore), event -> {
            boolean current = plugin.getConfig().getBoolean("features.welcome_effects", true);
            plugin.getConfig().set("features.welcome_effects", !current);
            plugin.saveConfig();
            
            String message = getConfigString(
                !current ? "messages.effects_enabled" : "messages.effects_disabled",
                !current ? "&a&l[!]&r&7 Welcome effects are now &aenabled&7." : "&c&l[!]&r&7 Welcome effects are now &cdisabled&7."
            );
            player.sendMessage(plugin.getMessageUtilities().colorize(message));
            
            playClickSound(player, false);
            refreshMenu(player);
        });
    }

    /**
     * Sets up the broadcast welcome messages toggle button in the menu.
     * 
     * <p>Creates a toggle button for enabling/disabling broadcasting welcome
     * messages to all players instead of just the joining player.</p>
     * 
     * @param menu The menu holder to add the button to. Must not be null.
     * @param player The player viewing the menu. Must not be null.
     */
    private void setupBroadcastWelcomeToggle(@NotNull MenuUtilities.MenuHolder menu, @NotNull Player player) {
        int slot = plugin.getConfig().getInt("welcome_config.broadcast_welcome_toggle.slot", 30);
        boolean enabled = plugin.getConfig().getBoolean("features.broadcast_welcome", false);
        
        Material material = getMaterial(
            enabled ? "welcome_config.broadcast_welcome_toggle.material_enabled" : "welcome_config.broadcast_welcome_toggle.material_disabled",
            enabled ? "BELL" : "IRON_BARS"
        );
        
        String name = getConfigString(
            enabled ? "welcome_config.broadcast_welcome_toggle.name_enabled" : "welcome_config.broadcast_welcome_toggle.name_disabled",
            enabled ? "&a&lBroadcast Welcome Messages" : "&7&lBroadcast Welcome Messages"
        ).replace("&", "§");
        
        List<String> lore = plugin.getConfig().getStringList("welcome_config.broadcast_welcome_toggle.description");
        if (lore.isEmpty()) {
            lore = new ArrayList<>();
            lore.add("&7When enabled, welcome messages are");
            lore.add("&7sent to all players. When disabled,");
            lore.add("&7only the joining player sees them.");
            lore.add("&7");
        }
        
        String status = getConfigString(
            enabled ? "welcome_config.broadcast_welcome_toggle.status_enabled" : "welcome_config.broadcast_welcome_toggle.status_disabled",
            enabled ? "&a&lStatus: Enabled" : "&c&lStatus: Disabled"
        );
        lore.add(status);
        lore = colorizeList(lore);

        menu.setItem(slot, MenuUtilities.createItem(material, name, lore), event -> {
            boolean current = plugin.getConfig().getBoolean("features.broadcast_welcome", false);
            plugin.getConfig().set("features.broadcast_welcome", !current);
                plugin.saveConfig();
            
            String message = getConfigString(
                !current ? "messages.broadcast_enabled" : "messages.broadcast_disabled",
                !current ? "&a&l[!]&r&7 Welcome message broadcasting is now &aenabled&7." : "&c&l[!]&r&7 Welcome message broadcasting is now &cdisabled&7."
            );
            player.sendMessage(plugin.getMessageUtilities().colorize(message));
            
            playClickSound(player, false);
                refreshMenu(player);
            });
    }

    /**
     * Sets up the welcome fireworks toggle button in the menu.
     * 
     * <p>Creates a toggle button for enabling/disabling firework displays on join.</p>
     * 
     * @param menu The menu holder to add the button to. Must not be null.
     * @param player The player viewing the menu. Must not be null.
     */
    private void setupFireworksToggle(@NotNull MenuUtilities.MenuHolder menu, @NotNull Player player) {
        int slot = plugin.getConfig().getInt("welcome_config.fireworks_toggle.slot", 31);
        boolean enabled = plugin.getConfig().getBoolean("features.welcome_fireworks", true);
        
        Material material = getMaterial(
            enabled ? "welcome_config.fireworks_toggle.material_enabled" : "welcome_config.fireworks_toggle.material_disabled",
            enabled ? "FIREWORK_ROCKET" : "GUNPOWDER"
        );
        
        String name = getConfigString(
            enabled ? "welcome_config.fireworks_toggle.name_enabled" : "welcome_config.fireworks_toggle.name_disabled",
            enabled ? "&c&l🎆 &r&c&lWelcome Fireworks" : "&7&l🎆 &r&7&lWelcome Fireworks"
        ).replace("&", "§");
        
        List<String> lore = plugin.getConfig().getStringList("welcome_config.fireworks_toggle.description");
        if (lore.isEmpty()) {
            lore = new ArrayList<>();
            lore.add("&7Launch colorful fireworks when");
            lore.add("&7players join the server.");
            lore.add("&7");
        }
        
        String status = getConfigString(
            enabled ? "welcome_config.fireworks_toggle.status_enabled" : "welcome_config.fireworks_toggle.status_disabled",
            enabled ? "&a&lStatus: Enabled" : "&c&lStatus: Disabled"
        );
        lore.add(status);
        lore = colorizeList(lore);

        menu.setItem(slot, MenuUtilities.createItem(material, name, lore), event -> {
            boolean current = plugin.getConfig().getBoolean("features.welcome_fireworks", true);
            plugin.getConfig().set("features.welcome_fireworks", !current);
            plugin.saveConfig();
            
            String message = getConfigString(
                !current ? "messages.fireworks_enabled" : "messages.fireworks_disabled",
                !current ? "&a&l[!]&r&7 Welcome fireworks are now &aenabled&7." : "&c&l[!]&r&7 Welcome fireworks are now &cdisabled&7."
            );
            player.sendMessage(plugin.getMessageUtilities().colorize(message));
            
            playClickSound(player, false);
            refreshMenu(player);
        });
    }

    /**
     * Sets up the welcome kit toggle button in the menu.
     * 
     * <p>Creates a toggle button for enabling/disabling welcome kits for new players.</p>
     * 
     * @param menu The menu holder to add the button to. Must not be null.
     * @param player The player viewing the menu. Must not be null.
     */
    private void setupKitToggle(@NotNull MenuUtilities.MenuHolder menu, @NotNull Player player) {
        int slot = plugin.getConfig().getInt("welcome_config.kit_toggle.slot", 32);
        boolean enabled = plugin.getConfig().getBoolean("features.welcome_kit", true);
        
        Material material = getMaterial(
            enabled ? "welcome_config.kit_toggle.material_enabled" : "welcome_config.kit_toggle.material_disabled",
            enabled ? "CHEST" : "BARRIER"
        );
        
        String name = getConfigString(
            enabled ? "welcome_config.kit_toggle.name_enabled" : "welcome_config.kit_toggle.name_disabled",
            enabled ? "&e&l📦 &r&e&lWelcome Kit" : "&7&l📦 &r&7&lWelcome Kit"
        ).replace("&", "§");
        
        List<String> lore = plugin.getConfig().getStringList("welcome_config.kit_toggle.description");
        if (lore.isEmpty()) {
            lore = new ArrayList<>();
            lore.add("&7Give new players a starter kit");
            lore.add("&7with useful items when they join.");
            lore.add("&7");
        }
        
        String status = getConfigString(
            enabled ? "welcome_config.kit_toggle.status_enabled" : "welcome_config.kit_toggle.status_disabled",
            enabled ? "&a&lStatus: Enabled" : "&c&lStatus: Disabled"
        );
        lore.add(status);
        lore = colorizeList(lore);

        menu.setItem(slot, MenuUtilities.createItem(material, name, lore), event -> {
            boolean current = plugin.getConfig().getBoolean("features.welcome_kit", true);
            plugin.getConfig().set("features.welcome_kit", !current);
            plugin.saveConfig();
            
            String message = getConfigString(
                !current ? "messages.kit_enabled" : "messages.kit_disabled",
                !current ? "&a&l[!]&r&7 Welcome kit is now &aenabled&7." : "&c&l[!]&r&7 Welcome kit is now &cdisabled&7."
            );
            player.sendMessage(plugin.getMessageUtilities().colorize(message));
            
            playClickSound(player, false);
            refreshMenu(player);
        });
    }

    /**
     * Sets up the welcome boss bar toggle button in the menu.
     * 
     * <p>Creates a toggle button for enabling/disabling boss bar welcome messages.</p>
     * 
     * @param menu The menu holder to add the button to. Must not be null.
     * @param player The player viewing the menu. Must not be null.
     */
    private void setupBossBarToggle(@NotNull MenuUtilities.MenuHolder menu, @NotNull Player player) {
        int slot = plugin.getConfig().getInt("welcome_config.boss_bar_toggle.slot", 33);
        boolean enabled = plugin.getConfig().getBoolean("features.welcome_boss_bar", true);
        
        Material material = getMaterial(
            enabled ? "welcome_config.boss_bar_toggle.material_enabled" : "welcome_config.boss_bar_toggle.material_disabled",
            enabled ? "DRAGON_HEAD" : "SKELETON_SKULL"
        );
        
        String name = getConfigString(
            enabled ? "welcome_config.boss_bar_toggle.name_enabled" : "welcome_config.boss_bar_toggle.name_disabled",
            enabled ? "&5&l🐉 &r&5&lWelcome Boss Bar" : "&7&l🐉 &r&7&lWelcome Boss Bar"
        ).replace("&", "§");
        
        List<String> lore = plugin.getConfig().getStringList("welcome_config.boss_bar_toggle.description");
        if (lore.isEmpty()) {
            lore = new ArrayList<>();
            lore.add("&7Display a boss bar with welcome");
            lore.add("&7message at the top of the screen.");
            lore.add("&7");
        }
        
        String status = getConfigString(
            enabled ? "welcome_config.boss_bar_toggle.status_enabled" : "welcome_config.boss_bar_toggle.status_disabled",
            enabled ? "&a&lStatus: Enabled" : "&c&lStatus: Disabled"
        );
        lore.add(status);
        lore = colorizeList(lore);

        menu.setItem(slot, MenuUtilities.createItem(material, name, lore), event -> {
            boolean current = plugin.getConfig().getBoolean("features.welcome_boss_bar", true);
            plugin.getConfig().set("features.welcome_boss_bar", !current);
                plugin.saveConfig();
            
            String message = getConfigString(
                !current ? "messages.boss_bar_enabled" : "messages.boss_bar_disabled",
                !current ? "&a&l[!]&r&7 Welcome boss bar is now &aenabled&7." : "&c&l[!]&r&7 Welcome boss bar is now &cdisabled&7."
            );
            player.sendMessage(plugin.getMessageUtilities().colorize(message));
            
            playClickSound(player, false);
            refreshMenu(player);
        });
    }

    /**
     * Sets up the welcome action bar toggle button in the menu.
     * 
     * <p>Creates a toggle button for enabling/disabling action bar welcome messages.</p>
     * 
     * @param menu The menu holder to add the button to. Must not be null.
     * @param player The player viewing the menu. Must not be null.
     */
    private void setupActionBarToggle(@NotNull MenuUtilities.MenuHolder menu, @NotNull Player player) {
        int slot = plugin.getConfig().getInt("welcome_config.action_bar_toggle.slot", 40);
        boolean enabled = plugin.getConfig().getBoolean("features.welcome_action_bar", true);
        
        Material material = getMaterial(
            enabled ? "welcome_config.action_bar_toggle.material_enabled" : "welcome_config.action_bar_toggle.material_disabled",
            enabled ? "NAME_TAG" : "BARRIER"
        );
        
        String name = getConfigString(
            enabled ? "welcome_config.action_bar_toggle.name_enabled" : "welcome_config.action_bar_toggle.name_disabled",
            enabled ? "&b&l📋 &r&b&lWelcome Action Bar" : "&7&l📋 &r&7&lWelcome Action Bar"
        ).replace("&", "§");
        
        List<String> lore = plugin.getConfig().getStringList("welcome_config.action_bar_toggle.description");
        if (lore.isEmpty()) {
            lore = new ArrayList<>();
            lore.add("&7Display a welcome message in the");
            lore.add("&7action bar (above the hotbar) when");
            lore.add("&7players join the server.");
            lore.add("&7");
        }
        
        String status = getConfigString(
            enabled ? "welcome_config.action_bar_toggle.status_enabled" : "welcome_config.action_bar_toggle.status_disabled",
            enabled ? "&a&lStatus: Enabled" : "&c&lStatus: Disabled"
        );
        lore.add(status);
        lore = colorizeList(lore);

        menu.setItem(slot, MenuUtilities.createItem(material, name, lore), event -> {
            boolean current = plugin.getConfig().getBoolean("features.welcome_action_bar", true);
            plugin.getConfig().set("features.welcome_action_bar", !current);
            plugin.saveConfig();
            
            String message = getConfigString(
                !current ? "messages.action_bar_enabled" : "messages.action_bar_disabled",
                !current ? "&a&l[!]&r&7 Welcome action bar is now &aenabled&7." : "&c&l[!]&r&7 Welcome action bar is now &cdisabled&7."
            );
            player.sendMessage(plugin.getMessageUtilities().colorize(message));
            
            playClickSound(player, false);
            refreshMenu(player);
        });
    }

    /**
     * Sets up the live activity feed display item in the menu.
     * 
     * <p>Creates a display item showing recent player join/quit events. The feed
     * is clickable to refresh and update the displayed entries. If the feed is
     * disabled in configuration, this method returns without creating the item.</p>
     * 
     * <p>The feed displays up to {@code welcome_config.feed.display_count} entries,
     * formatted according to {@code feed_formatting.entry_format}.</p>
     * 
     * @param menu The menu holder to add the feed item to. Must not be null.
     * @param player The player viewing the menu. Must not be null.
     */
    private void setupFeedDisplay(@NotNull MenuUtilities.MenuHolder menu, @NotNull Player player) {
        // Check if feed is enabled
        if (!plugin.getConfig().getBoolean("welcome_config.feed.enabled", true)) {
            return;
        }
        
        // Read feed configuration
        int slot = plugin.getConfig().getInt("welcome_config.feed_slot", 13); // Row 1, center
        Material feedMaterial = getMaterial("welcome_config.feed.material", "BOOK");
        String feedName = getConfigString("welcome_config.feed.name", "&e&lLive Feed").replace("&", "§");
        
        // Build feed lore with recent entries
        List<String> feedLore = buildFeedLore();
        
        // Create feed item with click handler to refresh
        menu.setItem(slot, MenuUtilities.createItem(feedMaterial, feedName, feedLore), event -> {
            // Refresh menu in place to update feed entries
            refreshMenu(player);
            // Play refresh sound (different pitch/volume than regular clicks)
            playClickSound(player, true);
        });
    }

    /**
     * Refreshes the configuration menu in place without closing it.
     * 
     * <p>This method updates all menu items to reflect the current configuration
     * state. It's called after toggling any feature to ensure the menu displays
     * the correct enabled/disabled states and updated feed entries.</p>
     * 
     * <p>The menu is updated in place using MenuUtilities.updateMenu(), which
     * clears and repopulates the menu without closing it, providing a smooth
     * user experience.</p>
     * 
     * <p>This method performs the same setup as openWelcomeConfig() but uses
     * updateMenu() instead of openMenu() to preserve the open state.</p>
     * 
     * @param player The player whose menu should be refreshed. Must not be null
     *               and must have the menu open.
     */
    private void refreshMenu(@NotNull Player player) {
        // Read menu title and convert color codes
        String title = getConfigString("welcome_config.title", "Welcome Config");
        title = title.replace("&", "§");
        
        // Read and validate menu size
        int menuSize = plugin.getConfig().getInt("welcome_config.menu_size", 54);
        if (menuSize < 9 || menuSize > 54 || menuSize % 9 != 0) {
            plugin.getLogger().warning("Invalid welcome_config.menu_size: " + menuSize + 
                ". Must be a multiple of 9 between 9-54. Using default: 54");
            menuSize = 54;
        }
        plugin.debug("Refreshing menu with size: " + menuSize);

        // Refresh plugin configuration
        plugin.reload();

        // Update menu in place (doesn't close it)
        MenuUtilities.updateMenu(player, title, menuSize, menu -> {
            // Re-setup all menu items to reflect current state
            setupWelcomeToggle(menu, player);
            setupQuitToggle(menu, player);
            setupFirstJoinToggle(menu, player);
            setupTitlesToggle(menu, player);
            setupSoundsToggle(menu, player);
            setupEffectsToggle(menu, player);
            setupBroadcastWelcomeToggle(menu, player);
            setupFireworksToggle(menu, player);
            setupKitToggle(menu, player);
            setupBossBarToggle(menu, player);
            setupActionBarToggle(menu, player);
            setupFeedDisplay(menu, player);
            setupRefreshButton(menu, player);
            setupPlaceholderPanes(menu);
        });
    }

    /**
     * Builds the lore for the live activity feed display item.
     * 
     * <p>Creates a formatted list of recent player join/quit events to display
     * in the feed item's lore. The feed shows:
     * <ul>
     *   <li>A header line</li>
     *   <li>Recent feed entries (up to display_count)</li>
     *   <li>An empty message if no entries exist</li>
     *   <li>A refresh hint at the bottom</li>
     * </ul>
     * </p>
     * 
     * <p>Each entry is formatted according to {@code feed_formatting.entry_format}
     * with placeholders replaced:
     * <ul>
     *   <li>%time% - Formatted timestamp</li>
     *   <li>%player% - Player name</li>
     *   <li>%action% - Action text (colored based on join/quit)</li>
     * </ul>
     * </p>
     * 
     * <p>Join actions are colored with {@code welcome_config.feed.join_color}
     * (default: green), while quit actions use {@code welcome_config.feed.quit_color}
     * (default: red).</p>
     * 
     * @return A list of lore strings representing the feed entries, with color
     *         codes converted to § format. Never returns null.
     */
    @NotNull
    private List<String> buildFeedLore() {
        List<String> lore = new ArrayList<>();
        
        String header = getConfigString("welcome_config.feed.header", "&7Recent server activity:");
        lore.add(header);
        lore.add("&7");
        
        int displayCount = plugin.getConfig().getInt("welcome_config.feed.display_count", 5);
        if (displayCount < 1) {
            plugin.getLogger().warning("welcome_config.feed.display_count must be at least 1. Using default: 5");
            displayCount = 5;
        }
        
        List<WelcomeManager.FeedEntry> entries = plugin.getWelcomeManager().getRecentFeedEntries(displayCount);
        
        if (entries.isEmpty()) {
            String emptyMessage = getConfigString("welcome_config.feed.empty_message", "&8No recent activity");
            lore.add(emptyMessage);
        } else {
            String joinAction = getConfigString("welcome_config.feed.join_action", "joined!");
            String joinColor = getConfigString("welcome_config.feed.join_color", "&a");
            String quitColor = getConfigString("welcome_config.feed.quit_color", "&c");
            String entryFormat = getConfigString("feed_formatting.entry_format", "&8[%time%] &7%player% &7%action%");
            
            DateTimeFormatter formatter = plugin.getWelcomeManager().getTimeFormatter();
            
            // Format each feed entry according to the configured format
            for (WelcomeManager.FeedEntry entry : entries) {
                // Determine color based on action type (join = green, quit = red)
                String actionColor = entry.getAction().equals(joinAction) ? joinColor : quitColor;
                // Format timestamp using the configured formatter
                String formattedTime = entry.getFormattedTime(formatter);
                
                // Replace placeholders in entry format string
                String formattedLine = entryFormat
                    .replace("%time%", formattedTime) // Replace time placeholder
                    .replace("%player%", entry.getPlayerName()) // Replace player name placeholder
                    .replace("%action%", actionColor + entry.getAction()); // Replace action with colored text
                
                lore.add(formattedLine);
            }
        }
        
        lore.add("&7");
        String refreshHint = getConfigString("welcome_config.feed.refresh_hint", "&eClick to refresh!");
        lore.add(refreshHint);
        
        return colorizeList(lore);
    }

    /**
     * Sets up the refresh button in the menu.
     * 
     * <p>Creates a button that reloads the plugin configuration and all dependent
     * components without requiring a server restart. This is useful for applying
     * configuration changes made outside the GUI.</p>
     * 
     * <p>When clicked, the button:
     * <ol>
     *   <li>Reloads the main config.yml file</li>
     *   <li>Reloads the WelcomeManager (updates time formatter, etc.)</li>
     *   <li>Reloads the DataUtilities (reloads player data)</li>
     *   <li>Displays a success message</li>
     *   <li>Refreshes the menu to show updated states</li>
     * </ol>
     * </p>
     * 
     * @param menu The menu holder to add the button to. Must not be null.
     * @param player The player viewing the menu. Must not be null.
     */
    private void setupRefreshButton(@NotNull MenuUtilities.MenuHolder menu, @NotNull Player player) {
        // Read refresh button configuration
        int slot = plugin.getConfig().getInt("welcome_config.refresh.slot", 53); // Bottom right
        Material refreshMaterial = getMaterial("welcome_config.refresh.material", "BLUE_STAINED_GLASS");
        String refreshName = getConfigString("welcome_config.refresh.name", "&b&lRefresh Plugin").replace("&", "§");
        
        // Read description from config or use default
        List<String> refreshLore = plugin.getConfig().getStringList("welcome_config.refresh.description");
        if (refreshLore.isEmpty()) {
            refreshLore = new ArrayList<>();
            refreshLore.add("&7Reloads the plugin configuration");
            refreshLore.add("&7and refreshes features without restarting");
            refreshLore.add("&7");
            refreshLore.add("&b&lClick to refresh!");
        }
        refreshLore = colorizeList(refreshLore);

        // Create refresh button with click handler
        menu.setItem(slot, MenuUtilities.createItem(refreshMaterial, refreshName, refreshLore), event -> {
            // Reload all plugin components
            plugin.reloadConfig(); // Reload main config.yml
            plugin.getWelcomeManager().reload(); // Reload WelcomeManager (time formatter, etc.)
            plugin.getDataUtilities().reload(); // Reload DataUtilities (player data)
            
            // Notify player of successful reload
            String message = getConfigString("messages.config_reloaded",
                "&a&l[!]&r&7 Plugin configuration &brefreshed &7successfully!");
            player.sendMessage(plugin.getMessageUtilities().colorize(message));
            
            // Play click sound and refresh menu
            playClickSound(player, false);
            refreshMenu(player);
        });
    }

    /**
     * Plays a click sound to provide audio feedback for menu interactions.
     * 
     * <p>Plays different sounds with different volume/pitch for regular clicks
     * vs feed refresh clicks. The sound is read from configuration with validation
     * and fallback to a default if invalid.</p>
     * 
     * <p>Sound configuration:
     * <ul>
     *   <li>Regular click: Uses {@code sounds.click} with {@code sounds.click_volume}
     *       and {@code sounds.click_pitch}</li>
     *   <li>Feed refresh: Uses {@code sounds.click} with {@code sounds.feed_refresh_volume}
     *       and {@code sounds.feed_refresh_pitch}</li>
     * </ul>
     * </p>
     * 
     * @param player The player to play the sound for. Must not be null.
     * @param isFeedRefresh Whether this is a feed refresh click (affects volume/pitch).
     */
    private void playClickSound(@NotNull Player player, boolean isFeedRefresh) {
        String soundName = getConfigString("sounds.click", "ENTITY_EXPERIENCE_ORB_PICKUP");
        Sound sound = getSound(soundName);
        if (sound == null) {
            plugin.getLogger().warning("Invalid sound: " + soundName + ". Using default: ENTITY_EXPERIENCE_ORB_PICKUP");
            sound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
        }
        
        double volume = isFeedRefresh 
            ? plugin.getConfig().getDouble("sounds.feed_refresh_volume", 0.5)
            : plugin.getConfig().getDouble("sounds.click_volume", 1.0);
        double pitch = isFeedRefresh
            ? plugin.getConfig().getDouble("sounds.feed_refresh_pitch", 1.5)
            : plugin.getConfig().getDouble("sounds.click_pitch", 1.0);
        
        player.playSound(player.getLocation(), sound, (float) volume, (float) pitch);
    }

    /**
     * Retrieves a Material from configuration with validation and fallback.
     * 
     * <p>Reads a material name from the configuration file, validates it against
     * Bukkit's Material enum, and returns the Material. If the material name is
     * invalid or missing, logs a warning and returns the default material. If the
     * default is also invalid, returns BOOK as a final fallback.</p>
     * 
     * <p>Material names are case-insensitive (converted to uppercase before lookup).</p>
     * 
     * @param configPath The configuration path to read the material name from. Must not be null.
     * @param defaultValue The default material name to use if config value is missing/invalid.
     *                    Must not be null and must be a valid Material enum value.
     * @return The Material object. Never returns null (falls back to BOOK if all else fails).
     */
    @NotNull
    private Material getMaterial(@NotNull String configPath, @NotNull String defaultValue) {
        String materialName = plugin.getConfig().getString(configPath, defaultValue);
        if (materialName.isEmpty()) {
            plugin.getLogger().warning("Missing or empty config value: " + configPath + ". Using default: " + defaultValue);
            materialName = defaultValue;
        }
        
        try {
            return Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid material: " + materialName + " at " + configPath + ". Using default: " + defaultValue);
            try {
                return Material.valueOf(defaultValue.toUpperCase());
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().severe("Default material is also invalid: " + defaultValue + ". Using BOOK as fallback.");
                return Material.BOOK;
            }
        }
    }

    /**
     * Converts a sound name string to a Bukkit Sound enum value.
     * 
     * <p>Attempts to parse the sound name as a Sound enum value (case-insensitive).
     * Returns null if the sound name is invalid, allowing the caller to handle
     * the error appropriately.</p>
     * 
     * <p>Sound names must match Bukkit's Sound enum values exactly (case-insensitive).
     * Common examples: ENTITY_PLAYER_LEVELUP, ENTITY_EXPERIENCE_ORB_PICKUP, BLOCK_NOTE_BLOCK_PLING</p>
     * 
     * @param soundName The sound name to convert. Must not be null.
     * @return The Sound enum value, or null if the sound name is invalid.
     */
    @Nullable
    private Sound getSound(@NotNull String soundName) {
        try {
            return Sound.valueOf(soundName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Retrieves a string value from configuration with validation and fallback.
     * 
     * <p>Reads a string value from the configuration file. If the value is null
     * or empty, logs a warning and returns the default value. This ensures that
     * the plugin always has valid values to work with, even if the configuration
     * is incomplete or corrupted.</p>
     * 
     * <p>This method is used throughout the codebase to safely read configuration
     * values with guaranteed non-null, non-empty results.</p>
     * 
     * @param path The configuration path to read from. Must not be null.
     * @param defaultValue The default value to return if config value is missing/invalid.
     *                    Must not be null.
     * @return The configuration value or default value. Never returns null or empty string.
     */
    @NotNull
    private String getConfigString(@NotNull String path, @NotNull String defaultValue) {
        String value = plugin.getConfig().getString(path);
        if (value == null || value.isEmpty()) {
            plugin.getLogger().warning("Missing or empty config value: " + path + ". Using default.");
            return defaultValue;
        }
        return value;
    }

    /**
     * Fills empty menu slots with placeholder panes for a clean appearance.
     * 
     * <p>This method identifies all used slots (where menu items are placed) and
     * fills all remaining empty slots with gray stained glass panes. This creates
     * a clean, organized appearance for the menu.</p>
     * 
     * <p>The placeholder material is configurable via
     * {@code welcome_config.placeholder_pane.material} (default: GRAY_STAINED_GLASS_PANE).</p>
     * 
     * <p>Used slots are determined by reading slot values from configuration for
     * all menu items (toggles, feed, refresh button). This ensures that placeholder
     * panes don't overwrite actual menu items.</p>
     * 
     * @param menu The menu holder to fill placeholders in. Must not be null.
     */
    private void setupPlaceholderPanes(@NotNull MenuUtilities.MenuHolder menu) {
        Material paneMaterial = getMaterial("welcome_config.placeholder_pane.material", "GRAY_STAINED_GLASS_PANE");
        ItemStack placeholder = MenuUtilities.createItem(paneMaterial, " ", new ArrayList<>());

        int menuSize = menu.getInventory().getSize();
        
        // Collect all used slots to avoid overwriting menu items
        // Using HashSet for O(1) lookup performance
        Set<Integer> usedSlots = new HashSet<>();
        // Add all toggle button slots
        usedSlots.add(plugin.getConfig().getInt("welcome_config.welcome_toggle.slot", 20));
        usedSlots.add(plugin.getConfig().getInt("welcome_config.quit_toggle.slot", 21));
        usedSlots.add(plugin.getConfig().getInt("welcome_config.first_join_toggle.slot", 22));
        usedSlots.add(plugin.getConfig().getInt("welcome_config.titles_toggle.slot", 23));
        usedSlots.add(plugin.getConfig().getInt("welcome_config.sounds_toggle.slot", 24));
        usedSlots.add(plugin.getConfig().getInt("welcome_config.effects_toggle.slot", 29));
        usedSlots.add(plugin.getConfig().getInt("welcome_config.broadcast_welcome_toggle.slot", 30));
        usedSlots.add(plugin.getConfig().getInt("welcome_config.fireworks_toggle.slot", 31));
        usedSlots.add(plugin.getConfig().getInt("welcome_config.kit_toggle.slot", 32));
        usedSlots.add(plugin.getConfig().getInt("welcome_config.boss_bar_toggle.slot", 33));
        usedSlots.add(plugin.getConfig().getInt("welcome_config.action_bar_toggle.slot", 40));
        // Add feed and refresh button slots
        usedSlots.add(plugin.getConfig().getInt("welcome_config.feed_slot", 13));
        usedSlots.add(plugin.getConfig().getInt("welcome_config.refresh.slot", 53));

        // Fill all empty slots with placeholder panes for clean appearance
        // Only fill slots that are not used and currently empty
        for (int i = 0; i < menuSize; i++) {
            if (!usedSlots.contains(i) && menu.getInventory().getItem(i) == null) {
                menu.setItem(i, placeholder, null); // No click handler for placeholders
            }
        }
    }

    /**
     * Converts color codes in a list of strings from '&' format to '§' format.
     * 
     * <p>This utility method is used to prepare lore and display names for menu
     * items. Minecraft's inventory system requires '§' color codes, but configuration
     * files typically use '&' for readability.</p>
     * 
     * <p>This method creates a new list to avoid modifying the original, ensuring
     * that the original list remains unchanged.</p>
     * 
     * @param list The list of strings to colorize. Must not be null (but may be empty).
     * @return A new list with all color codes converted. Never returns null.
     */
    @NotNull
    private List<String> colorizeList(@NotNull List<String> list) {
        List<String> colored = new ArrayList<>();
        for (String line : list) {
            // Convert & color codes to § for menu display (Minecraft format)
            colored.add(line.replace("&", "§"));
        }
        return colored;
    }
}
