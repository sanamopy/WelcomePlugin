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
 * This manager class orchestrates all welcome-related features when players join
 * the server. It handles welcome messages, titles, sounds, particle effects,
 * fireworks, kits, commands, boss bars, action bars, and welcome books. It also
 * manages the live activity feed that displays recent player join/quit events in
 * the configuration GUI. All features are configurable and can be toggled on/off
 * individually. The class uses efficient data structures and minimizes config lookups
 * for optimal performance.
 */

package com.sanamo.LogixWelcome.Managers;

import com.sanamo.LogixWelcome.LogixWelcome;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manager class for all welcome-related features and the live activity feed.
 * 
 * <p>This class is the central coordinator for all welcome features that activate
 * when players join the server. It provides methods to:
 * <ul>
 *   <li>Handle player join events and execute all enabled welcome features</li>
 *   <li>Send welcome and quit messages (with broadcast support)</li>
 *   <li>Display welcome titles and subtitles</li>
 *   <li>Play welcome sounds (different for first-join vs regular join)</li>
 *   <li>Show particle effects</li>
 *   <li>Launch fireworks displays</li>
 *   <li>Give welcome kits to new players</li>
 *   <li>Execute custom commands on join</li>
 *   <li>Display boss bar messages</li>
 *   <li>Send action bar messages</li>
 *   <li>Give welcome books to new players</li>
 *   <li>Manage the live activity feed (join/quit events)</li>
 * </ul>
 * </p>
 * 
 * <p>All features respect configuration settings and can be individually enabled/disabled.
 * Many features support "first-join only" mode, where they only activate for players
 * joining for the first time.</p>
 * 
 * <p>Performance Optimizations:
 * <ul>
 *   <li>Uses synchronized list for thread-safe feed entry management</li>
 *   <li>Feed entries are limited to a configurable maximum to prevent memory issues</li>
 *   <li>Config lookups are performed only when needed (not cached to allow hot-reload)</li>
 *   <li>Delayed execution via BukkitRunnable for resource-intensive features</li>
 * </p>
 * 
 * <p>Thread Safety:
 * <ul>
 *   <li>Feed entries list is synchronized for thread-safe access</li>
 *   <li>All operations are performed on the main server thread (via BukkitRunnable)</li>
 * </ul>
 * </p>
 * 
 * @author Karter Sanamo
 * @version 1.0.0
 * @since 1.0.0
 */
public class WelcomeManager {

    // ============================================
    // Fields
    // ============================================
    
    /**
     * Reference to the main plugin instance.
     * Used for accessing configuration, utilities, and server API.
     */
    private final LogixWelcome plugin;
    
    /**
     * Thread-safe list of feed entries for the live activity feed.
     * Entries are added at index 0 (most recent first) and limited to a maximum count.
     */
    private final List<FeedEntry> feedEntries;
    
    /**
     * DateTimeFormatter for formatting timestamps in feed entries.
     * Loaded from configuration and can be reloaded via reload() method.
     */
    private DateTimeFormatter timeFormatter;

    // ============================================
    // Constructor
    // ============================================
    
    /**
     * Constructs a new WelcomeManager instance.
     * 
     * <p>Initializes the feed entries list as a synchronized ArrayList for
     * thread-safe access, and loads the time formatter from configuration.</p>
     * 
     * @param plugin The main plugin instance. Must not be null.
     */
    public WelcomeManager(@NotNull LogixWelcome plugin) {
        this.plugin = plugin;
        // Use synchronized list for thread-safe feed entry management
        this.feedEntries = Collections.synchronizedList(new ArrayList<>());
        loadTimeFormatter();
    }

    // ============================================
    // Initialization
    // ============================================
    
    /**
     * Loads the time formatter from configuration.
     * 
     * <p>Reads the time format pattern from {@code welcome_config.feed.time_format}
     * and creates a DateTimeFormatter. If the format is invalid, logs a warning
     * and uses the default format "HH:mm:ss".</p>
     * 
     * <p>This method is called during initialization and when reload() is invoked.</p>
     */
    private void loadTimeFormatter() {
        String timeFormat = plugin.getConfig().getString("welcome_config.feed.time_format", "HH:mm:ss");
        try {
            this.timeFormatter = DateTimeFormatter.ofPattern(timeFormat);
        } catch (Exception e) {
            plugin.getLogger().warning("Invalid time format in config: " + timeFormat + ". Using default HH:mm:ss");
            this.timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        }
    }

    // ============================================
    // Main Join Handler
    // ============================================
    
    /**
     * Handles a player join event and executes all enabled welcome features.
     * 
     * <p>This is the main entry point for welcome features. It:
     * <ol>
     *   <li>Checks if this is the player's first join</li>
     *   <li>Marks the player as joined if it's their first time</li>
     *   <li>Executes all enabled welcome features based on configuration</li>
     * </ol>
     * </p>
     * 
     * <p>Features are executed in the following order:
     * <ol>
     *   <li>Welcome messages</li>
     *   <li>First-join messages (if applicable)</li>
     *   <li>Welcome titles</li>
     *   <li>Welcome sounds</li>
     *   <li>Welcome effects (particles)</li>
     *   <li>Welcome fireworks</li>
     *   <li>Welcome kit</li>
     *   <li>Welcome commands</li>
     *   <li>Welcome boss bar</li>
     *   <li>Welcome action bar</li>
     *   <li>Welcome book</li>
     * </ol>
     * </p>
     * 
     * <p>Each feature checks its own configuration toggle before executing.
     * Some features also check the {@code isFirstJoin} flag to determine
     * if they should activate (e.g., first-join only features).</p>
     * 
     * @param player The player who joined. Must not be null and must be online.
     */
    public void handlePlayerJoin(@NotNull Player player) {
        // Check if this is the player's first join
        boolean isFirstJoin = plugin.getDataUtilities().isFirstJoin(player.getUniqueId());
        
        // Mark as joined if first time (prevents treating subsequent joins as first-join)
        if (isFirstJoin) {
            plugin.getDataUtilities().markAsJoined(player.getUniqueId());
        }

        // Execute all enabled welcome features
        // Each feature checks its own configuration toggle
        
        if (plugin.getConfig().getBoolean("features.welcome_messages", true)) {
            sendWelcomeMessage(player);
        }

        if (isFirstJoin && plugin.getConfig().getBoolean("features.first_join_messages", true)) {
            sendFirstJoinMessage(player);
        }

        if (plugin.getConfig().getBoolean("features.welcome_titles", true)) {
            showWelcomeTitle(player);
        }

        if (plugin.getConfig().getBoolean("features.welcome_sounds", true)) {
            playWelcomeSound(player, isFirstJoin);
        }

        if (plugin.getConfig().getBoolean("features.welcome_effects", true)) {
            showWelcomeEffects(player, isFirstJoin);
        }

        if (plugin.getConfig().getBoolean("features.welcome_fireworks", true)) {
            launchWelcomeFireworks(player, isFirstJoin);
        }

        if (plugin.getConfig().getBoolean("features.welcome_kit", true)) {
            giveWelcomeKit(player, isFirstJoin);
        }

        if (plugin.getConfig().getBoolean("features.welcome_commands", true)) {
            executeWelcomeCommands(player, isFirstJoin);
        }

        if (plugin.getConfig().getBoolean("features.welcome_boss_bar", true)) {
            showWelcomeBossBar(player, isFirstJoin);
        }

        if (plugin.getConfig().getBoolean("features.welcome_action_bar", true)) {
            showWelcomeActionBar(player, isFirstJoin);
        }

        if (plugin.getConfig().getBoolean("features.welcome_book", true)) {
            giveWelcomeBook(player, isFirstJoin);
        }
    }

    // ============================================
    // Message Methods
    // ============================================
    
    /**
     * Sends a welcome message to the player or broadcasts it to all players.
     * 
     * <p>Reads the welcome message from {@code messages.welcome} in configuration,
     * replaces placeholders, colorizes it, and sends it either to the player
     * individually or broadcasts it to all players based on the
     * {@code features.broadcast_welcome} setting.</p>
     * 
     * <p>Placeholders supported:
     * <ul>
     *   <li>%player_name% - The player's display name</li>
     *   <li>%server_name% - The server's name</li>
     * </ul>
     * </p>
     * 
     * @param player The player who joined. Must not be null.
     */
    private void sendWelcomeMessage(@NotNull Player player) {
        // Read message from config with default fallback
        String message = plugin.getConfig().getString("messages.welcome", "&a+ &7%player_name%");
        if (message.isEmpty()) {
            message = "&a+ &7%player_name%";
        }

        // Replace placeholders (%player_name%, %server_name%)
        message = replacePlaceholders(message, player);

        // Check if message should be broadcast to all players
        boolean broadcast = plugin.getConfig().getBoolean("features.broadcast_welcome", false);
        String colorized = plugin.getMessageUtilities().colorize(message);

        // Send message to player or broadcast to all players
        if (broadcast) {
            plugin.getServer().broadcastMessage(colorized);
        } else {
            player.sendMessage(colorized);
        }
    }

    /**
     * Sends a special first-join message to the player or broadcasts it.
     * 
     * <p>This method is only called for players joining for the first time.
     * It reads the message from {@code messages.first_join} in configuration,
     * replaces placeholders, colorizes it, and sends it based on the broadcast setting.</p>
     * 
     * <p>Placeholders supported:
     * <ul>
     *   <li>%player_name% - The player's display name</li>
     *   <li>%server_name% - The server's name</li>
     * </ul>
     * </p>
     * 
     * @param player The player who joined for the first time. Must not be null.
     */
    private void sendFirstJoinMessage(@NotNull Player player) {
        // Read first-join message from config with default fallback
        String message = plugin.getConfig().getString("messages.first_join", 
            "&e&lWelcome &a%player_name% &e&lto the server! &7This is your first time joining!");
        if (message.isEmpty()) {
            message = "&e&lWelcome &a%player_name% &e&lto the server! &7This is your first time joining!";
        }

        // Replace placeholders and colorize
        message = replacePlaceholders(message, player);
        String colorized = plugin.getMessageUtilities().colorize(message);

        // Check broadcast setting and send message accordingly
        boolean broadcast = plugin.getConfig().getBoolean("features.broadcast_welcome", false);
        if (broadcast) {
            plugin.getServer().broadcastMessage(colorized);
        } else {
            player.sendMessage(colorized);
        }
    }

    /**
     * Sends a quit message when a player leaves the server.
     * 
     * <p>Reads the quit message from {@code messages.quit} in configuration,
     * replaces placeholders, colorizes it, and either broadcasts it to all
     * players or logs it to console based on the {@code features.broadcast_quit}
     * setting.</p>
     * 
     * <p>Note: The player is already offline when this method is called, so
     * messages cannot be sent directly to them. If broadcast is disabled,
     * the message is logged to console with color codes stripped.</p>
     * 
     * <p>Placeholders supported:
     * <ul>
     *   <li>%player_name% - The player's display name</li>
     *   <li>%server_name% - The server's name</li>
     * </ul>
     * </p>
     * 
     * @param player The player who left. Must not be null (but will be offline).
     */
    public void sendQuitMessage(@NotNull Player player) {
        // Read quit message from config with default fallback
        String message = plugin.getConfig().getString("messages.quit", "&c- &f%player_name%");
        if (message.isEmpty()) {
            message = "&c- &f%player_name%";
        }

        // Replace placeholders and colorize
        message = replacePlaceholders(message, player);
        String colorized = plugin.getMessageUtilities().colorize(message);

        // Check broadcast setting
        boolean broadcast = plugin.getConfig().getBoolean("features.broadcast_quit", false);
        if (broadcast) {
            // Broadcast to all online players
            plugin.getServer().broadcastMessage(colorized);
        } else {
            // Log to console with color codes stripped (player is offline, can't send to them)
            plugin.getLogger().info(colorized.replaceAll("§[0-9a-fk-or]", ""));
        }
    }

    // ============================================
    // Title Methods
    // ============================================
    
    /**
     * Displays a welcome title and subtitle to the player.
     * 
     * <p>Reads the title and subtitle from {@code welcome_titles.title} and
     * {@code welcome_titles.subtitle} in configuration, replaces placeholders,
     * colorizes them, and displays them with configurable fade in/out and
     * stay duration.</p>
     * 
     * <p>Timing values (in ticks, where 20 ticks = 1 second):
     * <ul>
     *   <li>fade_in - How long the title takes to fade in (default: 10 ticks)</li>
     *   <li>stay - How long the title stays fully visible (default: 70 ticks)</li>
     *   <li>fade_out - How long the title takes to fade out (default: 20 ticks)</li>
     * </ul>
     * </p>
     * 
     * <p>Placeholders supported:
     * <ul>
     *   <li>%player_name% - The player's display name</li>
     *   <li>%server_name% - The server's name</li>
     * </ul>
     * </p>
     * 
     * @param player The player to show the title to. Must not be null.
     */
    private void showWelcomeTitle(@NotNull Player player) {
        // Read title and subtitle from config with defaults
        String title = plugin.getConfig().getString("welcome_titles.title", "&a&lWelcome!");
        String subtitle = plugin.getConfig().getString("welcome_titles.subtitle", "&7Enjoy your stay, %player_name%!");

        // Replace placeholders in title and subtitle
        title = replacePlaceholders(title, player);
        subtitle = replacePlaceholders(subtitle, player);

        // Read timing values (in ticks: 20 ticks = 1 second)
        int fadeIn = plugin.getConfig().getInt("welcome_titles.fade_in", 10);
        int stay = plugin.getConfig().getInt("welcome_titles.stay", 70);
        int fadeOut = plugin.getConfig().getInt("welcome_titles.fade_out", 20);

        // Display title and subtitle to player
        player.sendTitle(
            plugin.getMessageUtilities().colorize(title),
            plugin.getMessageUtilities().colorize(subtitle),
            fadeIn, stay, fadeOut
        );
    }

    // ============================================
    // Sound Methods
    // ============================================
    
    /**
     * Plays a welcome sound effect to the player.
     * 
     * <p>Plays different sounds for first-join vs regular join:
     * <ul>
     *   <li>First-join: Uses {@code welcome_sounds.first_join_sound} (default: ENTITY_FIREWORK_ROCKET_LAUNCH)</li>
     *   <li>Regular join: Uses {@code welcome_sounds.sound} (default: ENTITY_PLAYER_LEVELUP)</li>
     * </ul>
     * </p>
     * 
     * <p>Volume and pitch are also configurable separately for first-join and regular join.
     * If the sound name is invalid, a warning is logged and a default sound is used.</p>
     * 
     * <p>Sound names must match Bukkit's Sound enum values (case-insensitive).
     * Common examples: ENTITY_PLAYER_LEVELUP, ENTITY_FIREWORK_ROCKET_LAUNCH, BLOCK_NOTE_BLOCK_PLING</p>
     * 
     * @param player The player to play the sound for. Must not be null.
     * @param isFirstJoin Whether this is the player's first join (determines which sound to play).
     */
    private void playWelcomeSound(@NotNull Player player, boolean isFirstJoin) {
        String soundName;
        double volume;
        double pitch;

        // Read sound configuration based on join type
        if (isFirstJoin) {
            soundName = plugin.getConfig().getString("welcome_sounds.first_join_sound", "ENTITY_FIREWORK_ROCKET_LAUNCH");
            volume = plugin.getConfig().getDouble("welcome_sounds.first_join_volume", 1.0);
            pitch = plugin.getConfig().getDouble("welcome_sounds.first_join_pitch", 1.0);
        } else {
            soundName = plugin.getConfig().getString("welcome_sounds.sound", "ENTITY_PLAYER_LEVELUP");
            volume = plugin.getConfig().getDouble("welcome_sounds.volume", 1.0);
            pitch = plugin.getConfig().getDouble("welcome_sounds.pitch", 1.0);
        }

        // Validate sound name
        if (soundName.isEmpty()) {
            soundName = isFirstJoin ? "ENTITY_FIREWORK_ROCKET_LAUNCH" : "ENTITY_PLAYER_LEVELUP";
        }

        // Try to play the configured sound
        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            player.playSound(player.getLocation(), sound, (float) volume, (float) pitch);
        } catch (IllegalArgumentException e) {
            // Invalid sound name - log warning and use default
            plugin.getLogger().warning("Invalid sound: " + soundName + ". Using default.");
            try {
                Sound defaultSound = isFirstJoin ? Sound.ENTITY_FIREWORK_ROCKET_LAUNCH : Sound.ENTITY_PLAYER_LEVELUP;
                player.playSound(player.getLocation(), defaultSound, (float) volume, (float) pitch);
            } catch (Exception ex) {
                // Fallback failed - log error
                plugin.getLogger().warning("Could not play sound: " + ex.getMessage());
            }
        }
    }

    // ============================================
    // Effect Methods
    // ============================================
    
    /**
     * Shows particle effects around the player.
     * 
     * <p>Displays different particle effects for first-join vs regular join:
     * <ul>
     *   <li>First-join: Uses {@code welcome_effects.first_join_particle} (default: FIREWORK) with higher count (50)</li>
     *   <li>Regular join: Uses {@code welcome_effects.particle} (default: VILLAGER_HAPPY) with lower count (20)</li>
     * </ul>
     * </p>
     * 
     * <p>Particle effects are spawned at the player's location (offset by 1 block upward)
     * with configurable offset, count, and speed. If the particle name is invalid,
     * a warning is logged and a default particle is used.</p>
     * 
     * <p>Particle names must match Bukkit's Particle enum values (case-insensitive).
     * Common examples: FIREWORK, VILLAGER_HAPPY, HAPPY_VILLAGER, HEART, ENCHANT</p>
     * 
     * @param player The player to show effects around. Must not be null.
     * @param isFirstJoin Whether this is the player's first join (determines which particle to use).
     */
    private void showWelcomeEffects(@NotNull Player player, boolean isFirstJoin) {
        String particleName;
        int count;
        double offsetX;
        double offsetY;
        double offsetZ;
        double speed;

        // Read particle configuration based on join type
        if (isFirstJoin) {
            particleName = plugin.getConfig().getString("welcome_effects.first_join_particle", "FIREWORK");
            count = plugin.getConfig().getInt("welcome_effects.first_join_count", 50);
        } else {
            particleName = plugin.getConfig().getString("welcome_effects.particle", "VILLAGER_HAPPY");
            count = plugin.getConfig().getInt("welcome_effects.count", 20);
        }

        // Read offset and speed values (same for both join types)
        offsetX = plugin.getConfig().getDouble("welcome_effects.offset_x", 0.5);
        offsetY = plugin.getConfig().getDouble("welcome_effects.offset_y", 1.0);
        offsetZ = plugin.getConfig().getDouble("welcome_effects.offset_z", 0.5);
        speed = plugin.getConfig().getDouble("welcome_effects.speed", 0.1);

        // Validate particle name
        if (particleName.isEmpty()) {
            particleName = isFirstJoin ? "FIREWORK" : "VILLAGER_HAPPY";
        }

        // Try to spawn the configured particle
        try {
            Particle particle = Particle.valueOf(particleName.toUpperCase());
            // Spawn particles at player location (offset 1 block upward)
            player.getWorld().spawnParticle(
                particle,
                player.getLocation().add(0, 1, 0),
                count,
                offsetX, offsetY, offsetZ,
                speed
            );
        } catch (IllegalArgumentException e) {
            // Invalid particle name - log warning and use default
            plugin.getLogger().warning("Invalid particle: " + particleName + ". Using default.");
            try {
                Particle defaultParticle = isFirstJoin ? Particle.FIREWORK : Particle.HAPPY_VILLAGER;
                player.getWorld().spawnParticle(
                    defaultParticle,
                    player.getLocation().add(0, 1, 0),
                    count,
                    offsetX, offsetY, offsetZ,
                    speed
                );
            } catch (Exception ex) {
                // Fallback failed - log error
                plugin.getLogger().warning("Could not spawn particle: " + ex.getMessage());
            }
        }
    }

    // ============================================
    // Firework Methods
    // ============================================
    
    /**
     * Launches colorful fireworks around the player.
     * 
     * <p>Launches multiple fireworks (configurable count) with random positioning
     * around the player. Each firework has configurable power, colors, type,
     * flicker, and trail effects.</p>
     * 
     * <p>This feature can be set to first-join only via
     * {@code welcome_fireworks.first_join_only}. If enabled and the player is
     * not a first-join, this method returns immediately.</p>
     * 
     * <p>Fireworks are launched after a configurable delay (in ticks) to allow
     * the player to fully load. The fireworks are spawned at random positions
     * within 3 blocks of the player's location.</p>
     * 
     * <p>Firework colors are read from {@code welcome_fireworks.colors} as a
     * list of color names. If no valid colors are found, default colors (RED,
     * GREEN, BLUE) are used.</p>
     * 
     * @param player The player to launch fireworks for. Must not be null.
     * @param isFirstJoin Whether this is the player's first join.
     */
    private void launchWelcomeFireworks(@NotNull Player player, boolean isFirstJoin) {
        // Check if fireworks are first-join only
        boolean firstJoinOnly = plugin.getConfig().getBoolean("welcome_fireworks.first_join_only", true);
        if (firstJoinOnly && !isFirstJoin) {
            return;
        }

        // Read firework configuration
        int delay = plugin.getConfig().getInt("welcome_fireworks.delay", 20); // Delay in ticks
        int count = plugin.getConfig().getInt("welcome_fireworks.count", 3); // Number of fireworks
        int power = plugin.getConfig().getInt("welcome_fireworks.power", 2); // Firework power (flight duration)

        // Schedule firework launch after delay
        new BukkitRunnable() {
            @Override
            public void run() {
                // Check if player is still online
                if (!player.isOnline()) {
                    return;
                }

                Location loc = player.getLocation();
                
                // Launch multiple fireworks at random positions
                for (int i = 0; i < count; i++) {
                    // Spawn firework at random position within 3 blocks of player
                    Firework firework = player.getWorld().spawn(loc.clone().add(
                        (Math.random() - 0.5) * 3, // Random X offset (-1.5 to 1.5)
                        0, // Same Y level as player
                        (Math.random() - 0.5) * 3  // Random Z offset (-1.5 to 1.5)
                    ), Firework.class);
                    
                    FireworkMeta meta = firework.getFireworkMeta();
                    meta.setPower(power);

                    // Read and parse firework colors from config
                    List<Color> colors = new ArrayList<>();
                    List<String> colorStrings = plugin.getConfig().getStringList("welcome_fireworks.colors");
                    for (String colorStr : colorStrings) {
                        try {
                            Color color = getFireworkColor(colorStr);
                            colors.add(color);
                        } catch (Exception e) {
                            plugin.getLogger().warning("Invalid firework color: " + colorStr);
                        }
                    }
                    
                    // Use default colors if none were configured or all were invalid
                    if (colors.isEmpty()) {
                        colors.add(Color.RED);
                        colors.add(Color.GREEN);
                        colors.add(Color.BLUE);
                    }
                    
                    // Build firework effect with configured properties
                    meta.addEffect(FireworkEffect.builder()
                        .with(FireworkEffect.Type.valueOf(plugin.getConfig().getString("welcome_fireworks.type", "BALL_LARGE").toUpperCase()))
                        .withColor(colors)
                        .flicker(plugin.getConfig().getBoolean("welcome_fireworks.flicker", true))
                        .trail(plugin.getConfig().getBoolean("welcome_fireworks.trail", true))
                        .build());

                    firework.setFireworkMeta(meta);
                }
            }
        }.runTaskLater(plugin, delay);
    }

    /**
     * Converts a color name string to a Bukkit Color object.
     * 
     * <p>First attempts to find the color as a field in the Color class using
     * reflection. If that fails, falls back to a switch statement with common
     * color mappings.</p>
     * 
     * <p>Supported color names (case-insensitive):
     * <ul>
     *   <li>RED, GREEN, BLUE, YELLOW, PURPLE, WHITE, ORANGE, PINK</li>
     *   <li>Any valid Color class field name</li>
     * </ul>
     * </p>
     * 
     * <p>If the color cannot be determined, returns WHITE as a default.</p>
     * 
     * @param colorName The name of the color. Must not be null.
     * @return The Color object, or WHITE if the color name is invalid.
     */
    @NotNull
    private Color getFireworkColor(@NotNull String colorName) {
        try {
            // Try to get color as a field in Color class using reflection
            return (Color) Color.class.getField(colorName.toUpperCase()).get(null);
        } catch (Exception e) {
            // Fallback to common color mappings
            return switch (colorName.toUpperCase()) {
                case "RED" -> Color.RED;
                case "GREEN" -> Color.LIME; // LIME is the bright green in Minecraft
                case "BLUE" -> Color.BLUE;
                case "YELLOW" -> Color.YELLOW;
                case "PURPLE" -> Color.PURPLE;
                case "WHITE" -> Color.WHITE;
                case "ORANGE" -> Color.ORANGE;
                case "PINK" -> Color.FUCHSIA; // FUCHSIA is the pink color in Minecraft
                default -> Color.WHITE; // Default to white if unknown
            };
        }
    }

    // ============================================
    // Kit Methods
    // ============================================
    
    /**
     * Gives a welcome kit to the player.
     * 
     * <p>Gives items to the player based on the {@code welcome_kit.items}
     * configuration. Items are specified in the format "MATERIAL:AMOUNT" or
     * just "MATERIAL" (amount defaults to 1).</p>
     * 
     * <p>This feature can be set to first-join only via
     * {@code welcome_kit.first_join_only}. If enabled and the player is not
     * a first-join, this method returns immediately.</p>
     * 
     * <p>Items are given after a configurable delay (in ticks) to allow the
     * player's inventory to be ready. If the player's inventory is full,
     * items will drop on the ground (standard Minecraft behavior).</p>
     * 
     * <p>Example item strings:
     * <ul>
     *   <li>"BREAD:5" - 5 bread</li>
     *   <li>"WOODEN_SWORD:1" - 1 wooden sword</li>
     *   <li>"DIAMOND" - 1 diamond (amount defaults to 1)</li>
     * </ul>
     * </p>
     * 
     * @param player The player to give the kit to. Must not be null.
     * @param isFirstJoin Whether this is the player's first join.
     */
    private void giveWelcomeKit(@NotNull Player player, boolean isFirstJoin) {
        // Check if kit is first-join only
        boolean firstJoinOnly = plugin.getConfig().getBoolean("welcome_kit.first_join_only", true);
        if (firstJoinOnly && !isFirstJoin) {
            return;
        }

        // Read delay and item list from config
        int delay = plugin.getConfig().getInt("welcome_kit.delay", 40); // Delay in ticks
        List<String> itemStrings = plugin.getConfig().getStringList("welcome_kit.items");

        // Schedule kit delivery after delay
        new BukkitRunnable() {
            @Override
            public void run() {
                // Check if player is still online
                if (!player.isOnline()) {
                    return;
                }

                // Parse and give each item
                for (String itemString : itemStrings) {
                    try {
                        // Parse item string format: "MATERIAL:AMOUNT" or "MATERIAL"
                        String[] parts = itemString.split(":");
                        Material material = Material.valueOf(parts[0].toUpperCase());
                        int amount = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;

                        // Create and give item
                        ItemStack item = new ItemStack(material, amount);
                        player.getInventory().addItem(item);
                    } catch (Exception e) {
                        // Log warning for invalid items but continue with others
                        plugin.getLogger().warning("Invalid kit item: " + itemString + " - " + e.getMessage());
                    }
                }
                
                // Notify player
                player.sendMessage(plugin.getMessageUtilities().colorize("&a&l[!] &r&7You received a welcome kit!"));
            }
        }.runTaskLater(plugin, delay);
    }

    // ============================================
    // Command Methods
    // ============================================
    
    /**
     * Executes custom commands when a player joins.
     * 
     * <p>Executes commands from {@code welcome_commands.commands} for regular
     * joins, or {@code welcome_commands.first_join_commands} for first-join
     * players. If first-join commands are empty but regular commands exist,
     * regular commands are used for first-join players.</p>
     * 
     * <p>This feature can be set to first-join only via
     * {@code welcome_commands.first_join_only}. If enabled and the player is
     * not a first-join, this method returns immediately.</p>
     * 
     * <p>Commands are executed as the console sender, so they have full
     * permissions. Placeholders in commands are replaced before execution.</p>
     * 
     * <p>Commands are executed after a configurable delay (in ticks) to ensure
     * the player is fully loaded.</p>
     * 
     * <p>Placeholders supported in commands:
     * <ul>
     *   <li>%player_name% - The player's display name</li>
     *   <li>%server_name% - The server's name</li>
     * </ul>
     * </p>
     * 
     * <p>Example commands:
     * <ul>
     *   <li>"say Welcome %player_name% to the server!"</li>
     *   <li>"give %player_name% diamond 1"</li>
     *   <li>"tell %player_name% Hello!"</li>
     * </ul>
     * </p>
     * 
     * @param player The player who joined. Must not be null.
     * @param isFirstJoin Whether this is the player's first join.
     */
    private void executeWelcomeCommands(@NotNull Player player, boolean isFirstJoin) {
        // Check if commands are first-join only
        boolean firstJoinOnly = plugin.getConfig().getBoolean("welcome_commands.first_join_only", false);
        if (firstJoinOnly && !isFirstJoin) {
            return;
        }

        // Read delay from config
        int delay = plugin.getConfig().getInt("welcome_commands.delay", 10); // Delay in ticks
        List<String> commands;

        // Determine which command list to use
        if (isFirstJoin) {
            // Try first-join commands first
            commands = plugin.getConfig().getStringList("welcome_commands.first_join_commands");
            List<String> regularCommands = plugin.getConfig().getStringList("welcome_commands.commands");
            // Fallback to regular commands if first-join commands are empty
            if (commands.isEmpty() && !regularCommands.isEmpty()) {
                commands = regularCommands;
            }
        } else {
            // Use regular commands for non-first-join players
            commands = plugin.getConfig().getStringList("welcome_commands.commands");
        }

        // Return early if no commands to execute
        if (commands.isEmpty()) {
            return;
        }

        // Store commands in final variable for use in anonymous class
        List<String> finalCommands = commands;
        
        // Schedule command execution after delay
        new BukkitRunnable() {
            @Override
            public void run() {
                // Check if player is still online
                if (!player.isOnline()) {
                    return;
                }

                // Execute each command
                for (String command : finalCommands) {
                    // Replace placeholders in command
                    command = replacePlaceholders(command, player);
                    // Execute as console sender (has full permissions)
                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
                }
            }
        }.runTaskLater(plugin, delay);
    }

    // ============================================
    // Boss Bar Methods
    // ============================================
    
    /**
     * Displays a welcome message in a boss bar.
     * 
     * <p>Creates a boss bar with a configurable message, color, style, and
     * duration. The boss bar appears at the top of the player's screen and
     * automatically removes itself after the duration expires.</p>
     * 
     * <p>This feature can be set to first-join only via
     * {@code welcome_boss_bar.first_join_only}. If enabled and the player is
     * not a first-join, this method returns immediately.</p>
     * 
     * <p>Boss bar properties:
     * <ul>
     *   <li>Message - Text displayed in the boss bar (supports placeholders)</li>
     *   <li>Color - Bar color (GREEN, RED, BLUE, etc.)</li>
     *   <li>Style - Bar style (SOLID, SEGMENTED_6, SEGMENTED_10, etc.)</li>
     *   <li>Progress - Fill level (0.0 to 1.0)</li>
     *   <li>Duration - How long the bar is displayed (in ticks)</li>
     * </ul>
     * </p>
     * 
     * <p>Placeholders supported:
     * <ul>
     *   <li>%player_name% - The player's display name</li>
     *   <li>%server_name% - The server's name</li>
     * </ul>
     * </p>
     * 
     * @param player The player to show the boss bar to. Must not be null.
     * @param isFirstJoin Whether this is the player's first join.
     */
    private void showWelcomeBossBar(@NotNull Player player, boolean isFirstJoin) {
        // Check if boss bar is first-join only
        boolean firstJoinOnly = plugin.getConfig().getBoolean("welcome_boss_bar.first_join_only", false);
        if (firstJoinOnly && !isFirstJoin) {
            return;
        }

        // Read message from config
        String message = plugin.getConfig().getString("welcome_boss_bar.message", 
            "&a&lWelcome &7%player_name% &a&lto &6&l%server_name%&r&7!");
        if (message.isEmpty()) {
            message = "&a&lWelcome &7%player_name% &a&lto &6&l%server_name%&r&7!";
        }

        // Replace placeholders and colorize
        message = replacePlaceholders(message, player);
        String colorized = plugin.getMessageUtilities().colorize(message);

        // Parse bar color with fallback
        BarColor barColor;
        try {
            barColor = BarColor.valueOf(plugin.getConfig().getString("welcome_boss_bar.color", "GREEN").toUpperCase());
        } catch (Exception e) {
            barColor = BarColor.GREEN; // Default to green
        }

        // Parse bar style with fallback
        BarStyle barStyle;
        try {
            barStyle = BarStyle.valueOf(plugin.getConfig().getString("welcome_boss_bar.style", "SOLID").toUpperCase());
        } catch (Exception e) {
            barStyle = BarStyle.SOLID; // Default to solid
        }

        // Read other boss bar properties
        double progress = plugin.getConfig().getDouble("welcome_boss_bar.progress", 1.0);
        int duration = plugin.getConfig().getInt("welcome_boss_bar.duration", 100); // Duration in ticks

        // Create and configure boss bar
        BossBar bossBar = Bukkit.createBossBar(colorized, barColor, barStyle);
        bossBar.setProgress(progress);
        bossBar.addPlayer(player);

        // Schedule boss bar removal after duration
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                ticks++;
                // Remove boss bar after duration or if player goes offline
                if (ticks >= duration || !player.isOnline()) {
                    bossBar.removePlayer(player);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L); // Run every tick (1L = 1 tick interval)
    }

    // ============================================
    // Action Bar Methods
    // ============================================
    
    /**
     * Displays a welcome message in the action bar (above the hotbar).
     * 
     * <p>Displays a message in the action bar, which appears just above the
     * player's hotbar. The message is displayed repeatedly for a configurable
     * duration.</p>
     * 
     * <p>This feature can be set to first-join only via
     * {@code welcome_action_bar.first_join_only}. If enabled and the player is
     * not a first-join, this method returns immediately.</p>
     * 
     * <p>The action bar uses Spigot's API for display. If the API is not
     * available, the message is sent as a regular chat message once as a
     * fallback.</p>
     * 
     * <p>Placeholders supported:
     * <ul>
     *   <li>%player_name% - The player's display name</li>
     *   <li>%server_name% - The server's name</li>
     * </ul>
     * </p>
     * 
     * @param player The player to show the action bar to. Must not be null.
     * @param isFirstJoin Whether this is the player's first join.
     */
    private void showWelcomeActionBar(@NotNull Player player, boolean isFirstJoin) {
        // Check if action bar is first-join only
        boolean firstJoinOnly = plugin.getConfig().getBoolean("welcome_action_bar.first_join_only", false);
        if (firstJoinOnly && !isFirstJoin) {
            return;
        }

        // Read message from config
        String message = plugin.getConfig().getString("welcome_action_bar.message", 
            "&e&lWelcome! &7Type &a/help &7for commands");
        if (message.isEmpty()) {
            message = "&e&lWelcome! &7Type &a/help &7for commands";
        }

        // Replace placeholders and colorize
        message = replacePlaceholders(message, player);
        String colorized = plugin.getMessageUtilities().colorize(message);

        // Read timing configuration
        int delay = plugin.getConfig().getInt("welcome_action_bar.delay", 30); // Delay before first display (ticks)
        int duration = plugin.getConfig().getInt("welcome_action_bar.duration", 60); // How long to display (ticks)

        // Schedule action bar display
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                // Stop if player goes offline or duration expires
                if (!player.isOnline() || ticks >= duration) {
                    cancel();
                    return;
                }
                
                try {
                    // Use Spigot API for action bar (available in Spigot/Paper)
                    // noinspection deprecation
                    player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                        net.md_5.bungee.api.chat.TextComponent.fromLegacyText(colorized));
                } catch (Exception e) {
                    // Fallback to regular message if Spigot API not available
                    // Only send once to avoid spam
                    if (ticks == 0) {
                        player.sendMessage(colorized);
                    }
                    cancel();
                }
                ticks++;
            }
        }.runTaskTimer(plugin, delay, 1L); // Start after delay, then run every tick
    }

    // ============================================
    // Book Methods
    // ============================================
    
    /**
     * Gives a welcome book to the player.
     * 
     * <p>Creates a written book with configurable title, author, and pages.
     * The book is given directly to the player's inventory.</p>
     * 
     * <p>This feature can be set to first-join only via
     * {@code welcome_book.first_join_only}. If enabled and the player is
     * not a first-join, this method returns immediately.</p>
     * 
     * <p>Book properties:
     * <ul>
     *   <li>Title - Book title (supports placeholders and color codes)</li>
     *   <li>Author - Book author (supports color codes)</li>
     *   <li>Pages - List of page content (supports placeholders and color codes)</li>
     * </ul>
     * </p>
     * 
     * <p>Placeholders supported in title and pages:
     * <ul>
     *   <li>%player_name% - The player's display name</li>
     *   <li>%server_name% - The server's name</li>
     * </ul>
     * </p>
     * 
     * <p>If no pages are configured, a default welcome page is created.</p>
     * 
     * @param player The player to give the book to. Must not be null.
     * @param isFirstJoin Whether this is the player's first join.
     */
    private void giveWelcomeBook(@NotNull Player player, boolean isFirstJoin) {
        // Check if book is first-join only
        boolean firstJoinOnly = plugin.getConfig().getBoolean("welcome_book.first_join_only", true);
        if (firstJoinOnly && !isFirstJoin) {
            return;
        }

        // Read book configuration
        String title = plugin.getConfig().getString("welcome_book.title", "&6&lWelcome to %server_name%!");
        String author = plugin.getConfig().getString("welcome_book.author", "Server Staff");
        List<String> pages = plugin.getConfig().getStringList("welcome_book.pages");

        // Create default page if none configured
        if (pages.isEmpty()) {
            pages = new ArrayList<>();
            pages.add("&lWelcome!\n\n&r&7Thank you for joining!");
        }

        // Replace placeholders in title and colorize
        title = replacePlaceholders(title, player);
        title = plugin.getMessageUtilities().colorize(title);

        // Create book item
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) book.getItemMeta();
        
        if (bookMeta != null) {
            // Set book title and author
            bookMeta.setTitle(title);
            bookMeta.setAuthor(plugin.getMessageUtilities().colorize(author));

            // Process and colorize each page
            List<String> colorizedPages = new ArrayList<>();
            for (String page : pages) {
                // Replace placeholders in page content
                page = replacePlaceholders(page, player);
                // Colorize page content
                colorizedPages.add(plugin.getMessageUtilities().colorize(page));
            }
            bookMeta.setPages(colorizedPages);
            book.setItemMeta(bookMeta);
        }

        // Give book to player
        player.getInventory().addItem(book);
        player.sendMessage(plugin.getMessageUtilities().colorize("&a&l[!] &r&7You received a welcome book! Check your inventory."));
    }

    // ============================================
    // Utility Methods
    // ============================================
    
    /**
     * Replaces placeholders in a message string.
     * 
     * <p>Replaces the following placeholders:
     * <ul>
     *   <li>%player_name% - Replaced with the player's display name</li>
     *   <li>%server_name% - Replaced with the server's name</li>
     * </ul>
     * </p>
     * 
     * <p>This method performs simple string replacement. Placeholders are
     * case-sensitive and must match exactly (including the % symbols).</p>
     * 
     * @param message The message string containing placeholders. Must not be null.
     * @param player The player whose information should be used for replacement. Must not be null.
     * @return The message with placeholders replaced. Never returns null.
     */
    @NotNull
    private String replacePlaceholders(@NotNull String message, @NotNull Player player) {
        // Replace player name placeholder
        message = message.replace("%player_name%", player.getName());
        // Replace server name placeholder
        message = message.replace("%server_name%", plugin.getServer().getName());
        return message;
    }

    // ============================================
    // Feed Methods
    // ============================================
    
    /**
     * Adds a new entry to the live activity feed.
     * 
     * <p>Adds an entry to the feed showing a player action (join/quit).
     * The entry is added at the beginning of the list (most recent first).
     * The feed is automatically trimmed to the maximum entry count to prevent
     * memory issues.</p>
     * 
     * <p>If the feed is disabled in configuration, this method returns
     * immediately without adding an entry.</p>
     * 
     * <p>This method is thread-safe and can be called from any thread.</p>
     * 
     * @param action The action text (e.g., "joined!", "quit!"). Must not be null.
     * @param playerName The name of the player who performed the action. Must not be null.
     */
    public void addFeedEntry(@NotNull String action, @NotNull String playerName) {
        // Check if feed is enabled
        if (!plugin.getConfig().getBoolean("welcome_config.feed.enabled", true)) {
            return;
        }

        // Read and validate max entries
        int maxEntries = plugin.getConfig().getInt("welcome_config.feed.max_entries", 10);
        if (maxEntries < 1) {
            plugin.getLogger().warning("welcome_config.feed.max_entries must be at least 1. Using default: 10");
            maxEntries = 10;
        }

        // Thread-safe addition to feed
        synchronized (feedEntries) {
            // Add new entry at the beginning (most recent first)
            feedEntries.addFirst(new FeedEntry(action, playerName, LocalDateTime.now()));
            
            // Trim feed to maximum size (remove the oldest entries)
            while (feedEntries.size() > maxEntries) {
                feedEntries.removeLast();
            }
        }
    }

    /**
     * Retrieves the most recent feed entries.
     * 
     * <p>Returns a list of the most recent feed entries, up to the specified count.
     * The entries are returned in order from most recent to oldest.</p>
     * 
     * <p>This method is thread-safe and returns a copy of the entries to prevent
     * external modification of the internal list.</p>
     * 
     * @param count The maximum number of entries to return. Must be positive.
     * @return A list of FeedEntry objects, ordered from most recent to oldest.
     *         The list size will be the minimum of count and the actual number of entries.
     *         Never returns null, but may return an empty list.
     */
    @NotNull
    public List<FeedEntry> getRecentFeedEntries(int count) {
        synchronized (feedEntries) {
            // Calculate how many entries to return (don't exceed available entries)
            int size = Math.min(count, feedEntries.size());
            // Return a copy of the sublist to prevent external modification
            return new ArrayList<>(feedEntries.subList(0, size));
        }
    }

    /**
     * Returns the time formatter used for formatting feed entry timestamps.
     * 
     * <p>The formatter is loaded from configuration and can be reloaded via
     * the reload() method.</p>
     * 
     * @return The DateTimeFormatter instance. Never returns null.
     */
    @NotNull
    public DateTimeFormatter getTimeFormatter() {
        return timeFormatter;
    }

    /**
     * Reloads the manager's configuration.
     * 
     * <p>Currently reloads the time formatter from configuration. This method
     * is called when the plugin's configuration is reloaded (e.g., via the
     * refresh button in the GUI).</p>
     */
    public void reload() {
        loadTimeFormatter();
    }

    // ============================================
    // FeedEntry Inner Class
    // ============================================
    
    /**
     * Represents a single entry in the live activity feed.
     * 
     * <p>Each entry contains:
     * <ul>
     *   <li>Action - The action text (e.g., "joined!", "quit!")</li>
     *   <li>Player name - The name of the player who performed the action</li>
     *   <li>Timestamp - When the action occurred</li>
     * </ul>
     * </p>
     * 
     * <p>This class is immutable - all fields are final and set in the constructor.
     * This ensures thread safety and prevents accidental modification.</p>
     * 
     * @author Karter Sanamo
     * @version 1.0.0
     * @since 1.0.0
     */
    public static class FeedEntry {
        
        /**
         * The action text (e.g., "joined!", "quit!").
         */
        private final String action;
        
        /**
         * The name of the player who performed the action.
         */
        private final String playerName;
        
        /**
         * The timestamp when the action occurred.
         */
        private final LocalDateTime timestamp;

        /**
         * Constructs a new FeedEntry.
         * 
         * @param action The action text. Must not be null.
         * @param playerName The player's name. Must not be null.
         * @param timestamp When the action occurred. Must not be null.
         */
        public FeedEntry(@NotNull String action, @NotNull String playerName, @NotNull LocalDateTime timestamp) {
            this.action = action;
            this.playerName = playerName;
            this.timestamp = timestamp;
        }

        /**
         * Returns the action text.
         * 
         * @return The action text. Never returns null.
         */
        @NotNull
        public String getAction() {
            return action;
        }

        /**
         * Returns the player's name.
         * 
         * @return The player's name. Never returns null.
         */
        @NotNull
        public String getPlayerName() {
            return playerName;
        }

        /**
         * Formats the timestamp using the provided formatter.
         * 
         * @param formatter The DateTimeFormatter to use for formatting. Must not be null.
         * @return The formatted timestamp string. Never returns null.
         */
        @NotNull
        public String getFormattedTime(@NotNull DateTimeFormatter formatter) {
            return timestamp.format(formatter);
        }

    }
}
