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
 * This utility class provides a comprehensive system for creating and managing
 * custom inventory-based GUI menus. It handles menu creation, item placement,
 * click event handling, menu updates, and cleanup. The class uses a HashMap
 * to track open menus per player, ensuring thread-safe operations and preventing
 * memory leaks. It provides methods to create menus, update them in place,
 * handle clicks, and create item stacks with custom names and lore.
 */

package com.sanamo.LogixWelcome.Utilities;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Utility class for creating and managing custom inventory-based GUI menus.
 * 
 * <p>This class provides a complete menu system that:
 * <ul>
 *   <li>Creates custom inventory GUIs with configurable titles and sizes</li>
 *   <li>Tracks open menus per player to prevent conflicts</li>
 *   <li>Handles click events and executes registered actions</li>
 *   <li>Prevents item removal from menus</li>
 *   <li>Supports in-place menu updates without closing</li>
 *   <li>Provides utility methods for creating menu items</li>
 * </ul>
 * </p>
 * 
 * <p>Menu System Architecture:
 * <ul>
 *   <li>Each menu is represented by a MenuHolder instance</li>
 *   <li>MenuHolders store the inventory and click action mappings</li>
 *   <li>Open menus are tracked in a static HashMap by player UUID</li>
 *   <li>Menus are automatically cleaned up when closed</li>
 * </ul>
 * </p>
 * 
 * <p>Thread Safety:
 * <ul>
 *   <li>All menu operations are performed on the main server thread</li>
 *   <li>The openMenus HashMap is accessed from event handlers (main thread)</li>
 *   <li>No explicit synchronization is needed as Bukkit events are single-threaded</li>
 * </ul>
 * </p>
 * 
 * <p>Performance:
 * <ul>
 *   <li>HashMap provides O(1) lookup time for menu retrieval</li>
 *   <li>Menu updates are done in-place to avoid closing/reopening</li>
 *   <li>Items are created efficiently with minimal object allocation</li>
 * </ul>
 * </p>
 * 
 * @author Karter Sanamo
 * @version 1.0.0
 * @since 1.0.0
 */
public class MenuUtilities {

    // ============================================
    // Static Fields
    // ============================================
    
    /**
     * Map tracking open menus for each player.
     * 
     * <p>Key: Player UUID
     * Value: MenuHolder instance containing the menu inventory and actions</p>
     * 
     * <p>This map is used to:
     * <ul>
     *   <li>Retrieve menu holders for click event handling</li>
     *   <li>Check if a player has an open menu</li>
     *   <li>Update menus in place without closing</li>
     *   <li>Clean up menus when closed</li>
     * </ul>
     * </p>
     * 
     * <p>Thread Safety: This map is only accessed from the main server thread
     * (via event handlers), so no explicit synchronization is required.</p>
     */
    private static final Map<UUID, MenuHolder> openMenus = new HashMap<>();

    // ============================================
    // MenuHolder Inner Class
    // ============================================
    
    /**
     * Internal class representing a menu instance.
     * 
     * <p>A MenuHolder encapsulates:
     * <ul>
     *   <li>The inventory object representing the menu</li>
     *   <li>A map of slot numbers to click action handlers</li>
     * </ul>
     * </p>
     * 
     * <p>This class provides methods to:
     * <ul>
     *   <li>Set items in menu slots with associated click actions</li>
     *   <li>Clear individual items or the entire menu</li>
     *   <li>Handle click events and execute registered actions</li>
     *   <li>Retrieve the underlying inventory</li>
     * </ul>
     * </p>
     * 
     * @author Karter Sanamo
     * @version 1.0.0
     * @since 1.0.0
     */
    public static class MenuHolder {
        
        /**
         * The inventory object representing this menu.
         * Created by Bukkit.createInventory() and displayed to players.
         */
        private final Inventory inventory;
        
        /**
         * Map of slot numbers to click action handlers.
         * 
         * <p>Key: Slot number (0-based index in the inventory)
         * Value: Consumer that handles the click event for that slot</p>
         * 
         * <p>When a player clicks a slot that has an action registered,
         * the corresponding Consumer is called with the InventoryClickEvent.</p>
         */
        private final Map<Integer, Consumer<InventoryClickEvent>> actions = new HashMap<>();

        /**
         * Constructs a new MenuHolder for the given inventory.
         * 
         * @param inventory The inventory object to wrap. Must not be null.
         */
        public MenuHolder(@NotNull Inventory inventory) {
            this.inventory = inventory;
        }

        /**
         * Sets an item in a specific slot and registers a click action.
         * 
         * <p>This method:
         * <ol>
         *   <li>Places the item in the specified slot</li>
         *   <li>Registers the click action (if provided) or removes any existing action</li>
         * </ol>
         * </p>
         * 
         * <p>If action is null, any existing action for that slot is removed.
         * This allows items to be placed without click handlers (e.g., decorative items).</p>
         * 
         * @param slot The slot number (0-based index) where the item should be placed.
         *             Must be within the inventory size bounds.
         * @param item The ItemStack to place in the slot. Must not be null.
         * @param action The click action handler, or null to remove any existing action.
         *               The Consumer receives the InventoryClickEvent when the slot is clicked.
         */
        public void setItem(int slot, @NotNull ItemStack item, @Nullable Consumer<InventoryClickEvent> action) {
            inventory.setItem(slot, item);
            if (action != null) {
                actions.put(slot, action);
            } else {
                actions.remove(slot);
            }
        }

        /**
         * Clears all items and actions from the menu.
         * 
         * <p>This method:
         * <ol>
         *   <li>Removes all items from the inventory</li>
         *   <li>Clears all registered click actions</li>
         * </ol>
         * </p>
         * 
         * <p>This is useful when updating a menu in place, as it provides
         * a clean slate before repopulating with new items.</p>
         */
        public void clearAll() {
            inventory.clear();
            actions.clear();
        }

        /**
         * Handles a click event and executes the registered action if one exists.
         * 
         * <p>This method is called by MenuUtilities.handleClick() when a player
         * clicks in a menu. It looks up the clicked slot in the actions map and
         * executes the corresponding Consumer if found.</p>
         * 
         * <p>If no action is registered for the clicked slot, this method does nothing.</p>
         * 
         * @param event The InventoryClickEvent that triggered this handler. Must not be null.
         */
        public void handleClick(@NotNull InventoryClickEvent event) {
            int slot = event.getRawSlot();
            Consumer<InventoryClickEvent> action = actions.get(slot);
            if (action != null) {
                action.accept(event);
            }
        }

        /**
         * Returns the inventory object for this menu.
         * 
         * @return The Inventory instance. Never null.
         */
        @NotNull
        public Inventory getInventory() {
            return inventory;
        }
    }

    // ============================================
    // Menu Management Methods
    // ============================================
    
    /**
     * Opens a new menu for a player.
     * 
     * <p>This method:
     * <ol>
     *   <li>Removes any existing menu for the player (if present)</li>
     *   <li>Creates a new inventory with the specified title and size</li>
     *   <li>Creates a MenuHolder and passes it to the menuSetup Consumer</li>
     *   <li>Registers the menu in the openMenus map</li>
     *   <li>Opens the inventory for the player</li>
     * </ol>
     * </p>
     * 
     * <p>The menuSetup Consumer is responsible for populating the menu with
     * items and registering click actions. This allows for flexible menu
     * creation with a clean API.</p>
     * 
     * <p>If the player already has an open menu, it is removed before opening
     * the new one to prevent conflicts.</p>
     * 
     * <p>Example usage:
     * <pre>{@code
     * MenuUtilities.openMenu(player, "My Menu", 27, menu -> {
     *     menu.setItem(10, itemStack, event -> {
     *         player.sendMessage("Clicked!");
     *     });
     * });
     * }</pre>
     * </p>
     * 
     * @param player The player to open the menu for. Must not be null.
     * @param title The title of the menu (displayed at the top). Supports color codes (§).
     *              Must not be null.
     * @param size The size of the menu (must be a multiple of 9, between 9 and 54).
     *             Common sizes: 9 (1 row), 18 (2 rows), 27 (3 rows), 36 (4 rows),
     *             45 (5 rows), 54 (6 rows).
     * @param menuSetup A Consumer that receives the MenuHolder and populates the menu.
     *                  Must not be null.
     */
    public static void openMenu(@NotNull Player player, @NotNull String title, int size,
                                @NotNull Consumer<MenuHolder> menuSetup) {
        // Remove any existing menu for this player to prevent conflicts
        openMenus.remove(player.getUniqueId());
        
        // Create new inventory
        Inventory inv = Bukkit.createInventory(null, size, title);
        
        // Create menu holder and populate menu
        MenuHolder holder = new MenuHolder(inv);
        menuSetup.accept(holder);

        // Register menu and open for player
        openMenus.put(player.getUniqueId(), holder);
        player.openInventory(inv);
    }

    /**
     * Updates an existing menu in place or opens a new one if needed.
     * 
     * <p>This method attempts to update the menu without closing it, providing
     * a smooth user experience. It:
     * <ol>
     *   <li>Checks if the player has an open menu</li>
     *   <li>Verifies the open inventory matches the menu's inventory</li>
     *   <li>If both conditions are true, clears and repopulates the menu in place</li>
     *   <li>Otherwise, opens a new menu</li>
     * </ol>
     * </p>
     * 
     * <p>In-place updates are useful for:
     * <ul>
     *   <li>Refreshing menu content (e.g., updating a live feed)</li>
     *   <li>Updating item states (e.g., toggling enabled/disabled indicators)</li>
     *   <li>Maintaining menu state without interrupting the user</li>
     * </ul>
     * </p>
     * 
     * <p>This method is used by the configuration GUI to refresh content
     * when toggles are clicked, keeping the menu open for a better UX.</p>
     * 
     * @param player The player whose menu should be updated. Must not be null.
     * @param title The title of the menu (used if opening a new menu). Must not be null.
     * @param size The size of the menu (used if opening a new menu).
     * @param menuSetup A Consumer that receives the MenuHolder and populates the menu.
     *                  Must not be null.
     */
    public static void updateMenu(@NotNull Player player, @NotNull String title, int size,
                                  @NotNull Consumer<MenuHolder> menuSetup) {
        MenuHolder holder = openMenus.get(player.getUniqueId());
        
        // Check if menu is currently open and matches
        if (holder != null && player.getOpenInventory().getTopInventory().equals(holder.getInventory())) {
            // Menu is open - update in place without closing
            holder.clearAll();
            menuSetup.accept(holder);
        } else {
            // Menu not open or different inventory - open new one
            openMenu(player, title, size, menuSetup);
        }
    }

    // ============================================
    // Event Handling Methods
    // ============================================
    
    /**
     * Handles inventory click events for custom menus.
     * 
     * <p>This method is called by MenuListener when a player clicks in any inventory.
     * It:
     * <ol>
     *   <li>Checks if the player has an open custom menu</li>
     *   <li>Cancels the click event to prevent item removal</li>
     *   <li>Executes the registered action if the click was in the menu inventory</li>
     * </ol>
     * </p>
     * 
     * <p>All clicks are cancelled when a custom menu is open, regardless of
     * whether the click is in the menu inventory or the player's inventory.
     * This ensures players cannot take items from menus or manipulate them
     * in unintended ways.</p>
     * 
     * <p>Click actions are only executed if:
     * <ul>
     *   <li>The player has an open custom menu</li>
     *   <li>The click was in the menu inventory (not player inventory)</li>
     *   <li>An action is registered for the clicked slot</li>
     * </ul>
     * </p>
     * 
     * @param event The InventoryClickEvent to handle. Must not be null.
     */
    public static void handleClick(@NotNull InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        
        MenuHolder holder = openMenus.get(player.getUniqueId());
        if (holder != null) {
            // Cancel ALL clicks when menu is open to prevent item removal
            // This includes clicks in player inventory, menu inventory, and any other interactions
            event.setCancelled(true);
            
            // Only handle click actions if clicking in the menu inventory
            if (event.getClickedInventory() != null && 
                event.getClickedInventory().equals(holder.getInventory())) {
                holder.handleClick(event);
            }
        }
    }

    /**
     * Handles inventory close events and cleans up menu state.
     * 
     * <p>This method is called by MenuListener when a player closes an inventory.
     * It removes the player's menu from the openMenus map, allowing the MenuHolder
     * to be garbage collected and preventing memory leaks.</p>
     * 
     * <p>This cleanup is essential for proper memory management, as menu holders
     * are stored in memory and must be removed when no longer needed.</p>
     * 
     * @param event The InventoryCloseEvent to handle. Must not be null.
     */
    public static void handleClose(@NotNull InventoryCloseEvent event) {
        openMenus.remove(event.getPlayer().getUniqueId());
    }

    /**
     * Checks if a player currently has an open custom menu.
     * 
     * <p>This method performs an O(1) lookup in the openMenus HashMap to
     * determine if the player has an open menu.</p>
     * 
     * <p>This is useful for:
     * <ul>
     *   <li>Preventing certain actions when a menu is open</li>
     *   <li>Checking menu state before operations</li>
     *   <li>Conditional logic based on menu presence</li>
     * </ul>
     * </p>
     * 
     * @param player The player to check. Must not be null.
     * @return true if the player has an open custom menu, false otherwise
     */
    public static boolean hasOpenMenu(@NotNull Player player) {
        return openMenus.containsKey(player.getUniqueId());
    }

    // ============================================
    // Item Creation Utilities
    // ============================================
    
    /**
     * Creates an ItemStack with a custom display name and lore.
     * 
     * <p>This is a convenience method for creating menu items quickly.
     * It creates an ItemStack, sets the display name and lore, and returns
     * the configured item.</p>
     * 
     * <p>Color codes in the name and lore should use the '§' format (not '&').
     * Use MessageUtilities.colorize() to convert '&' codes to '§' codes.</p>
     * 
     * <p>If lore is null, no lore is set on the item. If the ItemMeta cannot
     * be retrieved (should never happen in normal operation), the item is
     * returned without modifications.</p>
     * 
     * <p>Example usage:
     * <pre>{@code
     * ItemStack item = MenuUtilities.createItem(
     *     Material.DIAMOND,
     *     "§a§lClick Me!",
     *     Arrays.asList("§7This is a description", "§7Line 2")
     * );
     * }</pre>
     * </p>
     * 
     * @param material The material for the item. Must not be null.
     * @param name The display name for the item. Supports color codes (§).
     *             Must not be null.
     * @param lore The lore (description) for the item. Each string in the list
     *             represents one line. Supports color codes (§). May be null.
     * @return A new ItemStack with the specified material, name, and lore.
     *         Never returns null.
     */
    @NotNull
    public static ItemStack createItem(@NotNull Material material, @NotNull String name,
                                       @Nullable List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            // Set display name (use § for color codes)
            meta.setDisplayName(name);

            // Set lore if provided
            if (lore != null) {
                meta.setLore(lore);
            }

            item.setItemMeta(meta);
        }
        
        return item;
    }
}
