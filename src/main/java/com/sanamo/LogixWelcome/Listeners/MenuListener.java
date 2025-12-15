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
 * This listener handles inventory-related events for the plugin's GUI menus.
 * It prevents players from taking items out of custom menus, handles menu
 * interactions, and cleans up menu state when menus are closed. It handles
 * InventoryClickEvent, InventoryDragEvent, and InventoryCloseEvent events.
 */

package com.sanamo.LogixWelcome.Listeners;

import com.sanamo.LogixWelcome.Utilities.MenuUtilities;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Event listener for inventory-related events in custom GUI menus.
 * 
 * <p>This listener handles three types of inventory events:
 * <ul>
 *   <li><b>InventoryClickEvent</b> - Handles clicks in custom menus, prevents
 *       item removal, and executes menu item actions</li>
 *   <li><b>InventoryDragEvent</b> - Prevents dragging items when a custom menu
 *       is open to maintain menu integrity</li>
 *   <li><b>InventoryCloseEvent</b> - Cleans up menu state when a menu is closed</li>
 * </ul>
 * </p>
 * 
 * <p>This listener works in conjunction with MenuUtilities to provide a secure
 * and user-friendly menu system. All clicks in custom menus are cancelled to
 * prevent players from taking items, and only registered menu item actions are
 * executed.</p>
 * 
 * <p>Event Priority: All handlers use the default priority (NORMAL) to ensure
 * compatibility with other plugins that may also handle inventory events.</p>
 * 
 * @author Karter Sanamo
 * @version 1.0.0
 * @since 1.0.0
 */
public class MenuListener implements Listener {

    // ============================================
    // Event Handlers
    // ============================================
    
    /**
     * Handles inventory click events for custom GUI menus.
     * 
     * <p>This method is called whenever a player clicks in any inventory.
     * It delegates to MenuUtilities.handleClick() which:
     * <ul>
     *   <li>Checks if the player has an open custom menu</li>
     *   <li>Cancels the click event to prevent item removal</li>
     *   <li>Executes the registered action for the clicked menu item (if any)</li>
     * </ul>
     * </p>
     * 
     * <p>All clicks are cancelled when a custom menu is open, regardless of
     * whether the click is in the menu inventory or the player's inventory.
     * This ensures that players cannot take items from the menu or manipulate
     * the menu in unintended ways.</p>
     * 
     * @param event The InventoryClickEvent fired when a player clicks in an inventory.
     *              Must not be null.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        MenuUtilities.handleClick(event);
    }

    /**
     * Handles inventory drag events to prevent item dragging in custom menus.
     * 
     * <p>This method is called whenever a player drags items in an inventory.
     * It checks if the player has an open custom menu, and if so, cancels the
     * drag event to prevent players from dragging items out of the menu.</p>
     * 
     * <p>Dragging is prevented for all players who have an open custom menu,
     * regardless of which inventory is being dragged in. This ensures menu
     * integrity and prevents unintended item manipulation.</p>
     * 
     * @param event The InventoryDragEvent fired when a player drags items in an inventory.
     *              Must not be null.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryDrag(@NotNull InventoryDragEvent event) {
        // Only process if the event involves a player
        if (event.getWhoClicked() instanceof Player player) {
            // Check if player has an open custom menu
            if (MenuUtilities.hasOpenMenu(player)) {
                // Cancel drag to prevent item removal from menu
                event.setCancelled(true);
            }
        }
    }

    /**
     * Handles inventory close events to clean up menu state.
     * 
     * <p>This method is called whenever a player closes an inventory. It delegates
     * to MenuUtilities.handleClose() which removes the player's menu from the
     * internal tracking system, allowing the menu to be garbage collected and
     * preventing memory leaks.</p>
     * 
     * <p>This cleanup is essential for proper memory management, as menu holders
     * are stored in memory and must be removed when no longer needed.</p>
     * 
     * @param event The InventoryCloseEvent fired when a player closes an inventory.
     *              Must not be null.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryClose(@NotNull InventoryCloseEvent event) {
        MenuUtilities.handleClose(event);
    }
}
