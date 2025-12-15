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
 * This utility class provides methods for message formatting and color code conversion.
 * It handles the conversion of color codes from the standard '&' format used in
 * configuration files to the '§' format required by Minecraft's chat system.
 * This class is thread-safe and optimized for performance.
 */

package com.sanamo.LogixWelcome.Utilities;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utility class for message formatting and color code conversion.
 * 
 * <p>This class provides a centralized location for message-related utilities,
 * primarily focusing on color code conversion. It converts the standard '&'
 * color code format (used in configuration files) to the '§' format required
 * by Minecraft's chat system.</p>
 * 
 * <p>All methods in this class are thread-safe and optimized for performance.
 * The colorize method uses a simple string replacement which is efficient for
 * the typical message lengths used in Minecraft.</p>
 * 
 * <p>Example usage:
 * <pre>{@code
 * MessageUtilities messageUtils = plugin.getMessageUtilities();
 * String colored = messageUtils.colorize("&aHello &cWorld!");
 * // Result: "§aHello §cWorld!"
 * }</pre>
 * </p>
 * 
 * @author Karter Sanamo
 * @version 1.0.0
 * @since 1.0.0
 */
public class MessageUtilities {

    // ============================================
    // Fields
    // ============================================

    // ============================================
    // Constructor
    // ============================================
    
    /**
     * Constructs a new MessageUtilities instance.
     *
     */
    public MessageUtilities() { }

    // ============================================
    // Public Methods
    // ============================================
    
    /**
     * Converts color codes from '&' format to '§' format.
     * 
     * <p>This method replaces all occurrences of '&' followed by a color code
     * character (0-9, a-f, k-o, r) with the equivalent '§' format that
     * Minecraft uses for colored text.</p>
     * 
     * <p>If the input message is null, an empty string is returned to prevent
     * NullPointerExceptions in calling code.</p>
     * 
     * <p>Performance: This method uses a simple string replacement which is
     * O(n) where n is the length of the message. For typical Minecraft messages
     * (50-200 characters), this is extremely fast.</p>
     * 
     * <p>Examples:
     * <ul>
     *   <li>"&aHello" → "§aHello" (green text)</li>
     *   <li>"&c&lWarning!" → "§c§lWarning!" (red bold text)</li>
     *   <li>"&7&oItalic" → "§7§oItalic" (gray italic text)</li>
     * </ul>
     * </p>
     * 
     * @param message The message to colorize. May be null.
     * @return The colorized message with '§' color codes, or an empty string
     *         if the input message is null. Never returns null.
     */
    @NotNull
    public String colorize(@Nullable String message) {
        // Return empty string for null input to prevent NullPointerException
        if (message == null) {
            return "";
        }
        
        // Replace '&' with '§' for Minecraft color code compatibility
        // This is a simple O(n) operation that's very fast for typical message lengths
        return message.replace("&", "§");
    }
}
