package com.jruk8.jblockglitch.message;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import lombok.Getter;
import lombok.Setter;

/**
 * Messages shown by the plugin.
 * <p>
 * Every message starts with {prefix} and uses the config.yml color palette:
 * {plain} for body text, {brand} for headers and the brand name, {hl} for
 * numbers, player names and similar values, {error} for errors. Commands
 * insert runtime values through {bracket} placeholders: {version},
 * {authors}, {time}, {syntax}, {description}, {latest}, {current}, {url},
 * {modrinth-url}, {permission}, {key}, {value}, {keys}, {allowed},
 * {modrinth}, {github}, {discord} and {donate}. Angle brackets are reserved
 * for raw MiniMessage, and \n breaks the line.
 */
@Getter
@Setter
public class MessagesConfig extends OkaeriConfig {

    @Getter
    @Setter
    @SuppressWarnings("FieldMayBeFinal")
    public static class Core extends OkaeriConfig {

        @Comment("Sent when a reload succeeds (use {time})")
        private String reloadSuccess = "\\n{prefix}{plain}Configuration reloaded in {hl}{time}ms{plain}.";

        @Comment("Sent when a reload fails (use {time})")
        private String reloadFailure = "{prefix}{error}Reload failed in {time}ms. "
                + "Check the console for details.";

        @Comment("Sent when changed labels need a restart")
        private String reloadRestartRequired = "{prefix}{hl}Reload complete. "
                + "{plain}Command label changes need a server restart.";

        @Comment("Help header (use {version})")
        private String helpHeader = "\\n{prefix}{brand}JBlockGlitch {version} Help:";

        @Comment("One help entry (use {syntax} and {description})")
        private String helpEntryFormat = "{brand}» {hl}{syntax}{plain}: {description}";

        @Comment("Sent when a help query matches no commands")
        private String helpNoMatch = "{prefix}{plain}No commands match that query.";

        @Comment("Sent when no commands are visible to the sender")
        private String helpNoCommands = "{prefix}{plain}No commands are available for you.";

        @Comment("Version command output (use {version} and {authors})")
        private String versionSuccess = "\n{prefix}{brand}Version {hl}{version}"
                + "\n{brand}» {hl}Authors: {plain}{authors}"
                + "\n{brand}» {hl}Links: {modrinth}{plain} | {github}{plain} | {donate}";

        @Comment("Update notice (use {latest}, {current}, {url} and {modrinth-url})")
        private String updateAvailable = "\\n{prefix}{hl}JBlockGlitch {latest}{brand} is available"
                + " (running {hl}{current}{brand})."
                + "\\n{brand}» {hl}Update: {modrinth} {plain}| {github} {plain}| {discord}";

        @Comment("Sent when the sender lacks a permission (use {permission})")
        private String noPermission = "{prefix}{error}You do not have the permission "
                + "{hl}'{permission}'{error} to use this command.";

        @Comment("Modrinth link button (use {url})")
        private String linkModrinth = "<click:open_url:'{url}'>"
                + "<hover:show_text:'<#5ae663>Open on Modrinth'><#5ae663>Modrinth</hover></click>";

        @Comment("GitHub link button (use {url})")
        private String linkGithub = "<click:open_url:'{url}'>"
                + "<hover:show_text:'<#5a88e6>Open on GitHub'><#5a88e6>GitHub</hover></click>";

        @Comment("Discord link button (use {url})")
        private String linkDiscord = "<click:open_url:'{url}'>"
                + "<hover:show_text:'<#6258f2>Join the Discord'><#6258f2>Discord</hover></click>";

        @Comment("Donate link button (use {url})")
        private String linkDonate = "<click:open_url:'{url}'>"
                + "<hover:show_text:'<#ff5e5b>Support on Ko-fi'><#ff5e5b>Donate</hover></click>";

        @Comment("Separator between version authors")
        private String versionComma = "<gray>,</gray>";

        @Comment("'and' word between version authors")
        private String versionAnd = "<gray>and</gray>";
    }

    @Getter
    @Setter
    @SuppressWarnings("FieldMayBeFinal")
    public static class Config extends OkaeriConfig {

        @Comment("Header of the config listing")
        private String header = "\\n{prefix}{brand}Current configuration:";

        @Comment("One config entry (use {key} and {value})")
        private String entry = "{brand}» {hl}{key}{plain}: {value}";

        @Comment("Sent when a value is set (use {key} and {value})")
        private String updated = "{prefix}{plain}Set {hl}{key}{plain} to {hl}{value}{plain}.";

        @Comment("Sent for an unknown config key (use {key} and {keys})")
        private String unknownKey = "{prefix}{error}Unknown config key {hl}{key}{error}. "
                + "Valid keys: {hl}{keys}{error}.";

        @Comment("Sent for an invalid value (use {key}, {value} and {allowed})")
        private String invalidValue = "{prefix}{error}Invalid value {hl}{value}{error} "
                + "for {hl}{key}{error}. Allowed: {hl}{allowed}{error}.";
    }

    @Comment("Prefix prepended to every message, trailing space included")
    private String prefix = "<#a6a6a6>[<gradient:#DB6D77:#E08A8A><bold>J</bold>BlockGlitch</gradient>]</#a6a6a6> ";

    @Comment("Core plugin messages")
    private Core core = new Core();

    @Comment("Config command messages")
    private Config config = new Config();

    @Comment("Help menu command descriptions")
    private Help help = new Help();

    @Getter
    @Setter
    @SuppressWarnings("FieldMayBeFinal")
    public static class Help extends OkaeriConfig {

        @Comment("Help description of the help command")
        private String help = "Shows this help menu.";

        @Comment("Help description of the version command")
        private String version = "Shows the plugin version.";

        @Comment("Help description of the reload command")
        private String reload = "Reloads the plugin configuration.";

        @Comment("Help description of the config command")
        private String config = "Views or changes settings in-game.";
    }
}
