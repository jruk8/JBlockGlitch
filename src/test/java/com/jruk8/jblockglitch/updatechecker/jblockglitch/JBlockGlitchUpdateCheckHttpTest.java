package com.jruk8.jblockglitch.updatechecker.jblockglitch;

import com.jruk8.jblockglitch.updatechecker.ports.GitHubRelease;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JBlockGlitchUpdateCheckHttpTest {

    private static final String RELEASE_JSON = """
            {"tag_name": "v3.1.0",
             "html_url": "https://github.com/jruk8/JBlockGlitch/releases/tag/v3.1.0",
             "prerelease": false}""";

    @Test
    void parseReleaseReadsFields() {
        GitHubRelease release = JBlockGlitchUpdateCheckHttp.parseRelease(RELEASE_JSON).orElseThrow();

        assertEquals("v3.1.0", release.tagName());
        assertEquals("https://github.com/jruk8/JBlockGlitch/releases/tag/v3.1.0", release.htmlUrl());
        assertFalse(release.prerelease());
    }

    @Test
    void parseReleaseReadsPrereleaseFlag() {
        String json = RELEASE_JSON.replace("\"prerelease\": false", "\"prerelease\":true");

        assertTrue(JBlockGlitchUpdateCheckHttp.parseRelease(json).orElseThrow().prerelease());
    }

    @Test
    void parseReleaseEmptyWhenFieldsMissing() {
        assertTrue(JBlockGlitchUpdateCheckHttp.parseRelease("{}").isEmpty());
        assertTrue(JBlockGlitchUpdateCheckHttp.parseRelease("not json").isEmpty());
    }
}
