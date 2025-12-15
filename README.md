# 🎉 LogixWelcome

<div>

![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)
![Minecraft](https://img.shields.io/badge/minecraft-1.21-green.svg)
![Java](https://img.shields.io/badge/java-21-orange.svg)
![License](https://img.shields.io/badge/license-Proprietary-red.svg)

**A comprehensive and feature-rich welcome plugin for Minecraft servers**

*Make your players feel special from the moment they join!*

[Features](#-features) • [Installation](#-installation) • [Configuration](#-configuration) • [Commands](#-commands) • [Permissions](#-permissions) • [Support](#-support)

</div>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Installation](#-installation)
- [Configuration](#-configuration)
- [Commands](#-commands)
- [Permissions](#-permissions)
- [Placeholders](#-placeholders)
- [API](#-api)
- [Troubleshooting](#-troubleshooting)
- [Support](#-support)
- [Credits](#-credits)

---

## 🎯 Overview

LogixWelcome is a powerful, highly configurable welcome system for Minecraft servers running Spigot/Paper 1.21+. It provides a beautiful, feature-rich experience for both new and returning players, with an intuitive GUI configuration menu that makes customization effortless.

### Key Highlights

- ✨ **Beautiful GUI Configuration** - No more editing config files! Configure everything through an in-game menu
- 🎨 **Highly Customizable** - Every message, sound, effect, and feature can be customized
- 🚀 **Performance Optimized** - Lightweight and efficient, designed for servers of all sizes
- 📊 **Live Activity Feed** - Real-time feed of player joins/quits in the configuration GUI
- 🎁 **First-Join Detection** - Special features for first-time players
- 🔧 **Production Ready** - Fully documented, tested, and optimized codebase

---

## ✨ Features

### Core Features

#### 📝 **Welcome Messages**
- Customizable welcome messages for all players
- Special first-join messages
- Support for broadcasting to all players or individual messages
- Full placeholder support

#### 👋 **Quit Messages**
- Customizable goodbye messages
- Broadcast or individual message options
- Configurable delays

#### 🎬 **Welcome Titles & Subtitles**
- Beautiful title and subtitle displays
- Configurable fade in/out and stay duration
- Placeholder support for dynamic content

#### 🔊 **Sound Effects**
- Different sounds for first-join vs regular join
- Configurable volume and pitch
- Support for all Minecraft sound types

#### ✨ **Particle Effects**
- Spectacular particle displays on join
- Different effects for first-join players
- Configurable particle count, offset, and speed

#### 🎆 **Fireworks Display**
- Colorful fireworks launches
- Configurable colors, types, and effects
- First-join only option
- Multiple fireworks with random positioning

#### 📦 **Welcome Kits**
- Give starter items to new players
- Configurable items and amounts
- First-join only option
- Delayed delivery to prevent inventory issues

#### ⚡ **Command Execution**
- Execute custom commands on player join
- Separate commands for first-join players
- Placeholder support in commands
- Configurable delays

#### 🐉 **Boss Bar Messages**
- Display welcome messages in boss bar
- Configurable color, style, and duration
- Smooth fade in/out animations
- First-join only option

#### 📋 **Action Bar Messages**
- Messages displayed above the hotbar
- Configurable duration and delay
- First-join only option
- Automatic fallback for compatibility

#### 📖 **Welcome Books**
- Custom written books for new players
- Configurable title, author, and pages
- Placeholder support
- First-join only option

### Advanced Features

#### 📊 **Live Activity Feed**
- Real-time feed of player joins and quits
- Displayed in the configuration GUI
- Configurable display count and formatting
- Click to refresh functionality

#### 🎛️ **GUI Configuration Menu**
- Beautiful, organized configuration interface
- Toggle all features on/off with a single click
- Real-time configuration updates
- No server restart required
- Intuitive layout with placeholder panes

#### 🔄 **Hot Reload**
- Reload configuration without server restart
- Instant feature updates
- Safe data persistence

---

## 📥 Installation

### Requirements

- **Minecraft Server**: Spigot 1.21+ or Paper 1.21+
- **Java**: Java 21 or higher
- **Permissions**: Server operator or `welcome.config` permission

### Steps

1. **Download** the latest `LogixWelcome.jar` from the releases page
2. **Place** the JAR file in your server's `plugins` folder
3. **Start** or **restart** your server
4. **Configure** the plugin via `/welcomeconfig` or edit `plugins/LogixWelcome/config.yml`
5. **Enjoy** your new welcome system!

### First Run

On first run, the plugin will:
- Create `config.yml` with default settings
- Create `data.yml` for player data storage
- Register the `/welcomeconfig` command
- Enable all default features

---

## ⚙️ Configuration

### Configuration File

The main configuration file is located at:
```
plugins/LogixWelcome/config.yml
```

### GUI Configuration

The easiest way to configure the plugin is through the in-game GUI:

1. Run `/welcomeconfig` (requires `welcome.config` permission)
2. Click on any feature toggle to enable/disable it
3. Click the refresh button to reload configuration
4. All changes are saved automatically!

### Configuration Sections

#### Messages
```yaml
messages:
  welcome: "&a+ &r&7%player_name% joined the server"
  quit: "&c- &r&7%player_name% left the server"
  first_join: "&7&l✨ &r&7Welcome &a&l%player_name% &r&7to the server! This is your first time joining! &e&l✨"
```

#### Features
```yaml
features:
  welcome_messages: true
  quit_messages: true
  first_join_messages: true
  welcome_titles: true
  welcome_sounds: true
  welcome_effects: true
  welcome_fireworks: true
  welcome_kit: true
  welcome_commands: true
  welcome_boss_bar: true
  welcome_action_bar: true
  welcome_book: true
  broadcast_welcome: false
  broadcast_quit: false
  join_delay: 0
  quit_delay: 0
```

#### Welcome Titles
```yaml
welcome_titles:
  title: "&6&l══╗ &e&lWelcome! &6&l╚══"
  subtitle: "&7Enjoy your stay, %player_name%!"
  fade_in: 10
  stay: 70
  fade_out: 20
```

#### Welcome Sounds
```yaml
welcome_sounds:
  sound: "ENTITY_PLAYER_LEVELUP"
  volume: 1.0
  pitch: 1.0
  first_join_sound: "ENTITY_FIREWORK_ROCKET_LAUNCH"
  first_join_volume: 1.0
  first_join_pitch: 1.0
```

#### Welcome Effects
```yaml
welcome_effects:
  particle: "VILLAGER_HAPPY"
  count: 20
  offset_x: 0.5
  offset_y: 1.0
  offset_z: 0.5
  speed: 0.1
  first_join_particle: "FIREWORK"
  first_join_count: 50
```

#### Welcome Fireworks
```yaml
welcome_fireworks:
  enabled: true
  first_join_only: true
  delay: 20
  count: 3
  power: 2
  type: "BALL_LARGE"
  flicker: true
  trail: true
  colors:
    - "RED"
    - "GREEN"
    - "BLUE"
```

#### Welcome Kit
```yaml
welcome_kit:
  enabled: true
  first_join_only: true
  delay: 40
  items:
    - "BREAD:5"
    - "WOODEN_SWORD:1"
    - "LEATHER_HELMET:1"
```

#### Welcome Commands
```yaml
welcome_commands:
  enabled: false
  first_join_only: false
  delay: 10
  commands: []
  first_join_commands: []
```

#### Welcome Boss Bar
```yaml
welcome_boss_bar:
  enabled: true
  first_join_only: false
  message: "&a&lWelcome &7%player_name% &a&lto &6&l%server_name%&r&7!"
  color: "GREEN"
  style: "SOLID"
  progress: 1.0
  duration: 100
  fade_in: 20
  fade_out: 20
```

#### Welcome Action Bar
```yaml
welcome_action_bar:
  enabled: true
  first_join_only: false
  message: "&e&lWelcome! &7Type &a/help &7for commands"
  delay: 30
  duration: 60
```

#### Welcome Book
```yaml
welcome_book:
  enabled: true
  first_join_only: true
  title: "&6&lWelcome to %server_name%!"
  author: "Server Staff"
  pages:
    - "&lWelcome!\n\n&r&7Thank you for joining\n&a%server_name%&7!\n\n&7We hope you enjoy\nyour stay here."
    - "&lServer Rules\n\n&r&71. Be respectful\n&72. No griefing\n&73. Have fun!"
```

### GUI Configuration

The GUI configuration section controls the appearance and layout of the configuration menu:

```yaml
welcome_config:
  title: "&8▬▬▬▬▬▬▬▬▬ &b&lWelcome &7Config &8▬▬▬▬▬▬▬▬▬"
  menu_size: 54
  
  feed:
    enabled: true
    max_entries: 10
    display_count: 5
    material: "BOOK"
    name: "&e&l📖 &r&e&lLive Activity Feed"
    # ... more feed settings
```

---

## 💬 Commands

### `/welcomeconfig`

Opens the beautiful GUI configuration menu.

**Permission**: `welcome.config` (default: OP)

**Usage**:
```
/welcomeconfig
```

**Description**: Opens an interactive GUI where you can toggle all plugin features on/off, view the live activity feed, and reload the configuration.

---

## 🔐 Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `welcome.config` | Allows access to the `/welcomeconfig` command and GUI | `op` |

### Permission Examples

**Give all players access to the config GUI:**
```yaml
permissions:
  welcome.config:
    default: true
```

**Give only admins access:**
```yaml
permissions:
  welcome.config:
    default: false
```

Then use a permission plugin to grant `welcome.config` to specific groups.

---

## 📝 Placeholders

The following placeholders can be used in messages, titles, commands, and book pages:

| Placeholder | Description | Example |
|-------------|-------------|---------|
| `%player_name%` | The player's display name | `Steve` |
| `%server_name%` | The server's name | `MyServer` |

### Usage Examples

```yaml
messages:
  welcome: "&a+ &r&7%player_name% joined the server"
  first_join: "&7&l✨ &r&7Welcome &a&l%player_name% &r&7to &6&l%server_name%&r&7!"
```

---

## 🔌 API

LogixWelcome provides a simple API for developers to integrate with other plugins.

### Getting the Plugin Instance

```java
LogixWelcome plugin = (LogixWelcome) Bukkit.getPluginManager().getPlugin("LogixWelcome");
if (plugin == null) {
    // Plugin not loaded
    return;
}
```

### Accessing Managers

```java
WelcomeManager welcomeManager = plugin.getWelcomeManager();
MessageUtilities messageUtilities = plugin.getMessageUtilities();
DataUtilities dataUtilities = plugin.getDataUtilities();
```

### Checking First Join

```java
UUID playerUUID = player.getUniqueId();
boolean isFirstJoin = dataUtilities.isFirstJoin(playerUUID);
```

### Adding Feed Entries

```java
welcomeManager.addFeedEntry("custom_action", player.getName());
```

### Getting Recent Feed Entries

```java
List<WelcomeManager.FeedEntry> entries = welcomeManager.getRecentFeedEntries(5);
for (WelcomeManager.FeedEntry entry : entries) {
    String action = entry.getAction();
    String playerName = entry.getPlayerName();
    String formattedTime = entry.getFormattedTime(welcomeManager.getTimeFormatter());
}
```

---

## 🐛 Troubleshooting

### Plugin Not Loading

**Problem**: Plugin doesn't appear in `/plugins` list

**Solutions**:
- Ensure you're using Spigot 1.21+ or Paper 1.21+
- Check that Java 21+ is installed
- Verify the JAR file is in the `plugins` folder
- Check server logs for error messages

### GUI Not Opening

**Problem**: `/welcomeconfig` command doesn't work

**Solutions**:
- Check that you have the `welcome.config` permission
- Verify the command is registered (check server logs)
- Try restarting the server

### Features Not Working

**Problem**: Welcome messages/effects/etc. not appearing

**Solutions**:
- Check that the feature is enabled in `config.yml` or via GUI
- Verify the feature toggle in the GUI is green (enabled)
- Check server logs for error messages
- Ensure the player has the necessary permissions

### Configuration Not Saving

**Problem**: Changes in GUI don't persist after server restart

**Solutions**:
- Click the refresh button in the GUI after making changes
- Check file permissions on `config.yml`
- Verify the plugin has write access to the plugins folder

### Performance Issues

**Problem**: Server lag when players join

**Solutions**:
- Reduce particle counts in `welcome_effects`
- Disable fireworks if not needed
- Increase delays for resource-intensive features
- Reduce the number of welcome commands

---

## 💬 Support

### Getting Help

- **GitHub Issues**: Report bugs or request features
- **Documentation**: Check this README and inline code documentation
- **Server Logs**: Always check server logs first for error messages

### Reporting Bugs

When reporting bugs, please include:
- Minecraft version
- Server type (Spigot/Paper) and version
- Plugin version
- Steps to reproduce
- Error messages from server logs
- Relevant configuration sections

### Feature Requests

Feature requests are welcome! Please provide:
- Clear description of the feature
- Use case and benefits
- Any implementation ideas (optional)

---

## 👏 Credits

**LogixWelcome** is developed and maintained by **Karter Sanamo**.

### Special Thanks

- The Spigot/Paper development team for the excellent API
- The Minecraft community for feedback and suggestions
- All server owners who use and support this plugin

---

## 📄 License

**All code is property of Karter Sanamo**

This software is proprietary and confidential. All rights reserved.

Unauthorized copying, modification, distribution, or use of this software, via any medium, is strictly prohibited. This software is owned by Karter Sanamo. Any use of this software without explicit permission from the owner is a violation of law.

---

## 🎉 Thank You!

Thank you for using LogixWelcome! We hope it enhances your server's player experience. If you enjoy the plugin, please consider leaving a review or star!

**Made with ❤️ by Karter Sanamo**

---

<div>

**LogixWelcome v1.0.0** • **Minecraft 1.21+** • **Java 21+**

*Making every player feel welcome, one join at a time.*

</div>

