package com.sanamo.welcomePlugin.Utilities;

import com.sanamo.welcomePlugin.WelcomePlugin;

public class MessageUtilities {

    private final WelcomePlugin plugin;

    public MessageUtilities(WelcomePlugin plugin) { this.plugin = plugin; }

    public String colorize(String message) {
        if (message == null) return "";
        return message.replace("&", "§");
    }
}
