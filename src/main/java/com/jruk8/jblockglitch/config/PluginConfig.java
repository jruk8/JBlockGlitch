package com.jruk8.jblockglitch.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Header;
import eu.okaeri.configs.annotation.Comment;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@SuppressWarnings("FieldMayBeFinal")
@Header({
        "################################",
        "#        JBlockGlitch        #",
        "################################",
        "",
        "# Prevents block placement glitches in protected regions",
        "# and vanilla ghost item generation.",
        ""
})
public class PluginConfig extends OkaeriConfig {

    @Comment("GitHub update checker settings.")
    private UpdateChecker updateChecker = new UpdateChecker();

    @Comment({
            "Extra labels for the /jblockglitch root command (shortcuts like /jbg).",
            "Applies on restart."
    })
    private List<String> aliases = List.of("jbg");

    @Comment({
            "Default language for players without a language pin: auto follows",
            "each player's client locale, any other key forces that language.",
            "Keys: https://jruk8.github.io/JBlockGlitch/configuration/#languages"
    })
    private String defaultLanguage = "auto";

    @Comment("Protected block placement validation (WorldGuard, GriefPrevention and others).")
    private ProtectedBlock protectedBlock = new ProtectedBlock();

    @Comment("Ghost item and ghost block validation.")
    private GhostItem ghostItem = new GhostItem();

    @Comment({
            "Color palette used by every message via {plain}, {brand}, {hl} and",
            "{error}. Values are MiniMessage color tags."
    })
    private Colors colors = new Colors();

    @Comment({
            "Anonymous usage statistics via bStats (open-source, no personal data or IP addresses).",
            "Requires a server restart to take effect."
    })
    private Metrics metrics = new Metrics();

    @Getter
    @Setter
    @SuppressWarnings("FieldMayBeFinal")
    public static class ProtectedBlock extends OkaeriConfig {

        @Comment("Resend the real block state and stop movement through denied placements.")
        private boolean enabled = true;

        @Comment({
                "How denied protected block placements are handled.",
                "'medium' rubberbands the player to their position on the last tick.",
                "'strict' teleports the player to the ground using advanced detection."
        })
        private String mode = "strict";
    }

    @Getter
    @Setter
    @SuppressWarnings("FieldMayBeFinal")
    public static class GhostItem extends OkaeriConfig {

        @Comment("Detect ghost item attempts and force a client inventory resync.")
        private boolean enabled = true;

        @Comment({
                "How ghost item generation is detected.",
                "'medium' listens for well-known event patterns in the same tick (recommended).",
                "'hard' listens for all inventory manipulation events (prevents MC-277905).",
                "'brute-force' forces the client to resync the inventory on every tick (laggy)."
        })
        private String mode = "medium";

        @Comment({
                "How often the blocks around each player resync, in ticks.",
                "Only players who moved since the previous resync are processed.",
                "-1 disables the resync entirely."
        })
        private int resyncTickInterval = 5;
    }

    @Getter
    @Setter
    @SuppressWarnings("FieldMayBeFinal")
    public static class Colors extends OkaeriConfig {

        @Comment("MiniMessage color tag for normal message bodies, used via {plain}.")
        private String plainColor = "<#e685b5>";

        @Comment("MiniMessage color tag for brand accents, used via {brand}.")
        private String brandColor = "<#e68595>";

        @Comment("MiniMessage color tag for highlighted words, used via {hl}.")
        private String highlightColor = "<#85b5e6>";

        @Comment("MiniMessage color tag for errors, used via {error}.")
        private String errorColor = "<#e68585>";
    }

    @Getter
    @Setter
    @SuppressWarnings("FieldMayBeFinal")
    public static class Metrics extends OkaeriConfig {

        @Comment("Send anonymous statistics to bStats.")
        private boolean enabled = true;
    }

    @Getter
    @Setter
    @SuppressWarnings("FieldMayBeFinal")
    public static class UpdateChecker extends OkaeriConfig {

        @Comment({
                "Checks GitHub releases for new JBlockGlitch versions on enable and on reload.",
                "The check runs asynchronously; findings go to the console and to",
                "players holding jblockglitch.reload (on join and via /jblockglitch version)."
        })
        private boolean enabled = true;

        @Comment({
                "Which release severities notify. A release only notifies when",
                "its severity over the running version is enabled here."
        })
        private Releases releases = new Releases();

        @Getter
        @Setter
        @SuppressWarnings("FieldMayBeFinal")
        public static class Releases extends OkaeriConfig {

            @Comment("Notify about major releases.")
            private boolean major = true;

            @Comment("Notify about minor releases.")
            private boolean minor = true;

            @Comment("Notify about hotfix releases.")
            private boolean hotfix = true;
        }
    }
}
