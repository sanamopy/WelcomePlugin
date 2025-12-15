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
 * This utility class manages persistent player data storage and retrieval.
 * It handles first-join detection by tracking which players have joined the
 * server before. Data is stored in a YAML file (data.yml) in the plugin's
 * data folder. This class uses an in-memory cache (HashSet) for fast lookups
 * and only writes to disk when necessary to optimize performance.
 */

package com.sanamo.LogixWelcome.Utilities;

import com.sanamo.LogixWelcome.LogixWelcome;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Utility class for managing persistent player data.
 * 
 * <p>This class provides functionality for tracking which players have joined
 * the server before, enabling first-join detection. It uses a combination of
 * in-memory caching (HashSet) and persistent storage (YAML file) to provide
 * fast lookups while ensuring data persistence across server restarts.</p>
 * 
 * <p>Data Storage:
 * <ul>
 *   <li>Data is stored in {@code data.yml} in the plugin's data folder</li>
 *   <li>Uses YAML format for human-readable storage</li>
 *   <li>In-memory HashSet cache for O(1) lookup performance</li>
 *   <li>Data is saved to disk when players are marked as joined</li>
 * </ul>
 * </p>
 * 
 * <p>Performance Optimizations:
 * <ul>
 *   <li>In-memory HashSet provides O(1) lookup time</li>
 *   <li>Data is only saved to disk when new players are added</li>
 *   <li>Lazy loading - data is loaded once on initialization</li>
 * </ul>
 * </p>
 * 
 * <p>Thread Safety:
 * <ul>
 *   <li>HashSet operations are not thread-safe</li>
 *   <li>This class is typically accessed from the main server thread</li>
 *   <li>If accessed from multiple threads, external synchronization is required</li>
 * </ul>
 * </p>
 * 
 * @author Karter Sanamo
 * @version 1.0.0
 * @since 1.0.0
 */
public class DataUtilities {

    // ============================================
    // Fields
    // ============================================
    
    /**
     * Reference to the main plugin instance.
     * Used for accessing plugin data folder and logging.
     */
    private final LogixWelcome plugin;
    
    /**
     * The data file where player information is stored.
     * Located at: plugins/LogixWelcome/data.yml
     */
    private File dataFile;
    
    /**
     * The YAML configuration object for reading/writing data.
     * Loaded from dataFile.
     */
    private FileConfiguration dataConfig;
    
    /**
     * In-memory cache of player UUIDs who have joined before.
     * Used for fast O(1) lookup performance.
     * 
     * <p>A player is in this set if they have joined the server at least once.
     * If a player's UUID is not in this set, they are considered a first-time joiner.</p>
     */
    private final Set<UUID> firstJoinPlayers = new HashSet<>();

    // ============================================
    // Constructor
    // ============================================
    
    /**
     * Constructs a new DataUtilities instance and loads existing data.
     * 
     * <p>This constructor:
     * <ol>
     *   <li>Initializes the data file path</li>
     *   <li>Creates the data file if it doesn't exist</li>
     *   <li>Loads existing player data into memory</li>
     * </ol>
     * </p>
     * 
     * <p>If the data file cannot be created or loaded, an error is logged
     * but the plugin continues to operate (with an empty data set).</p>
     * 
     * @param plugin The main plugin instance. Must not be null.
     */
    public DataUtilities(@NotNull LogixWelcome plugin) {
        this.plugin = plugin;
        loadData();
    }

    // ============================================
    // Data Loading
    // ============================================
    
    /**
     * Loads player data from the data file into memory.
     * 
     * <p>This method:
     * <ol>
     *   <li>Ensures the data file exists (creates it if necessary)</li>
     *   <li>Loads the YAML configuration from the file</li>
     *   <li>Reads the list of player UUIDs who have joined</li>
     *   <li>Populates the in-memory HashSet cache</li>
     * </ol>
     * </p>
     * 
     * <p>If the data file doesn't exist, it is created. If there are errors
     * reading the file or parsing UUIDs, warnings are logged but the method
     * continues (skipping invalid entries).</p>
     * 
     * <p>This method is called:
     * <ul>
     *   <li>During plugin initialization (from constructor)</li>
     *   <li>When reload() is called (configuration reload)</li>
     * </ul>
     * </p>
     */
    private void loadData() {
        // Initialize data file path
        dataFile = new File(plugin.getDataFolder(), "data.yml");

        // Create data file if it doesn't exist
        if (!dataFile.exists()) {
            try {
                File parent = dataFile.getParentFile();
                if (parent != null && !parent.exists()) {
                    boolean dirsCreated = parent.mkdirs();
                    if (!dirsCreated) {
                        plugin.getLogger().warning("Failed to create plugin data directories.");
                    }
                }

                boolean fileCreated = dataFile.createNewFile();
                if (!fileCreated) {
                    plugin.getLogger().warning("data.yml already exists but was expected to be created.");
                }

            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create data.yml", e);
                dataConfig = YamlConfiguration.loadConfiguration(dataFile);
                return;
            }
        }


        // Load YAML configuration from file
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        
        // Load first-join players from configuration
        // Clear existing cache first to avoid duplicates on reload
        firstJoinPlayers.clear();
        
        if (dataConfig.contains("first_joins")) {
            for (String uuidString : dataConfig.getStringList("first_joins")) {
                try {
                    UUID playerUUID = UUID.fromString(uuidString);
                    firstJoinPlayers.add(playerUUID);
                } catch (IllegalArgumentException e) {
                    // Log warning for invalid UUID but continue processing others
                    plugin.getLogger().warning("Invalid UUID in data.yml: " + uuidString);
                }
            }
        }
        
        plugin.debug("Loaded " + firstJoinPlayers.size() + " player(s) from data.yml");
    }

    // ============================================
    // Data Saving
    // ============================================
    
    /**
     * Saves the current player data to the data file.
     * 
     * <p>This method:
     * <ol>
     *   <li>Converts the in-memory HashSet to a list of UUID strings</li>
     *   <li>Writes the list to the YAML configuration</li>
     *   <li>Saves the configuration to the data file</li>
     * </ol>
     * </p>
     * 
     * <p>If the save operation fails, an error is logged but the method
     * returns normally (data remains in memory).</p>
     * 
     * <p>This method is called:
     * <ul>
     *   <li>When a new player is marked as joined (markAsJoined)</li>
     *   <li>During plugin shutdown (from LogixWelcome.onDisable)</li>
     * </ul>
     * </p>
     * 
     * <p>Performance: This method performs a file I/O operation, so it should
     * not be called excessively. The current implementation only saves when
     * new data is added, which is optimal.</p>
     */
    public void saveData() {
        try {
            // Convert HashSet to list of UUID strings for YAML storage
            dataConfig.set("first_joins", firstJoinPlayers.stream()
                .map(UUID::toString)
                .toList());
            
            // Save to file
            dataConfig.save(dataFile);
            
            plugin.debug("Saved " + firstJoinPlayers.size() + " player(s) to data.yml");
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save data.yml: " + e.getMessage(), e);
        }
    }

    // ============================================
    // Player Data Methods
    // ============================================
    
    /**
     * Checks if a player is joining for the first time.
     * 
     * <p>This method performs an O(1) lookup in the in-memory HashSet to
     * determine if the player has joined before.</p>
     * 
     * <p>A player is considered a first-time joiner if their UUID is not
     * present in the firstJoinPlayers set.</p>
     * 
     * @param playerUUID The UUID of the player to check. Must not be null.
     * @return true if this is the player's first join, false if they have
     *         joined before
     */
    public boolean isFirstJoin(@NotNull UUID playerUUID) {
        // O(1) HashSet lookup - very fast
        return !firstJoinPlayers.contains(playerUUID);
    }

    /**
     * Marks a player as having joined the server.
     * 
     * <p>This method:
     * <ol>
     *   <li>Adds the player's UUID to the in-memory HashSet</li>
     *   <li>Saves the data to disk if this is a new player</li>
     * </ol>
     * </p>
     * 
     * <p>The save operation only occurs if the player was not already in the
     * set (i.e., if this is truly a new player). This optimization prevents
     * unnecessary disk writes.</p>
     * 
     * <p>This method should be called when a player joins for the first time
     * to ensure they are not treated as a first-time joiner on subsequent joins.</p>
     * 
     * @param playerUUID The UUID of the player to mark as joined. Must not be null.
     */
    public void markAsJoined(@NotNull UUID playerUUID) {
        // Add to HashSet (returns true if this is a new entry)
        if (firstJoinPlayers.add(playerUUID)) {
            // Only save if this is a new player (optimization)
            saveData();
            plugin.debug("Marked player " + playerUUID + " as joined (first time)");
        }
    }

    // ============================================
    // Reload
    // ============================================
    
    /**
     * Reloads player data from the data file.
     * 
     * <p>This method clears the in-memory cache and reloads all data from
     * the data.yml file. This is useful when the data file has been modified
     * externally or when the plugin configuration is reloaded.</p>
     * 
     * <p>This method is called when the plugin's reload() method is invoked
     * (e.g., when the refresh button is clicked in the configuration GUI).</p>
     */
    public void reload() {
        loadData();
    }
}
