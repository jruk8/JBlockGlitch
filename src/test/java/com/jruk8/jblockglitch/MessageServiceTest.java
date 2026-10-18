package com.jruk8.jblockglitch;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MessageServiceTest {

    @TempDir
    private Path dataFolder;

    private MessageService serviceWithMessages(String messagesYml, String textFormat) throws Exception {
        Files.write(dataFolder.resolve("messages.yml"), messagesYml.getBytes(StandardCharsets.UTF_8));

        YamlConfiguration config = new YamlConfiguration();
        config.set("text-format", textFormat);

        JavaPlugin plugin = mock(JavaPlugin.class);
        when(plugin.getDataFolder()).thenReturn(dataFolder.toFile());
        when(plugin.getConfig()).thenReturn(config);
        when(plugin.getLogger()).thenReturn(Logger.getLogger("MessageServiceTest"));

        MessageService service = new MessageService(plugin);
        service.load();
        return service;
    }

    private String sentPlainText(MessageService service, java.util.function.Consumer<CommandSender> send) {
        CommandSender sender = mock(CommandSender.class);
        send.accept(sender);
        ArgumentCaptor<Component> captor = ArgumentCaptor.forClass(Component.class);
        org.mockito.Mockito.verify(sender).sendMessage(captor.capture());
        return PlainTextComponentSerializer.plainText().serialize(captor.getValue());
    }

    @Test
    void prefixPlaceholderReplacedInMessages() throws Exception {
        MessageService service = serviceWithMessages("""
                prefix: "[Test] "
                no-permission: "{prefix}Nope"
                reload-success: "{prefix}Done"
                """, "minimessage");

        assertEquals("[Test] Nope", sentPlainText(service, service::sendNoPermission));
        assertEquals("[Test] Done", sentPlainText(service, service::sendReloadSuccess));
    }

    @Test
    void missingPrefixFallsBackToDefault() throws Exception {
        MessageService service = serviceWithMessages("""
                no-permission: "{prefix}Nope"
                """, "minimessage");

        assertEquals("[JBlockGlitch] Nope", sentPlainText(service, service::sendNoPermission));
    }

    @Test
    void legacyFormatRendersAmpersandCodes() throws Exception {
        MessageService service = serviceWithMessages("""
                prefix: "&7[JB] "
                no-permission: "{prefix}&cNope"
                """, "legacy");

        String text = sentPlainText(service, service::sendNoPermission);

        assertTrue(text.contains("[JB] "), text);
        assertTrue(text.contains("Nope"), text);
        assertTrue(!text.contains("&") && !text.contains("<"), text);
    }
}
