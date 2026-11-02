package com.jruk8.jblockglitch.message;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LanguagesTest {

    @Test
    @DisplayName("should map client locales onto codes")
    void shouldNormalizeLocales() {
        assertEquals("en", Languages.normalize("en_us"));
        assertEquals("fi", Languages.normalize("fi_fi"));
        assertEquals("de", Languages.normalize("DE_de"));
        assertEquals("zh_tw", Languages.normalize("zh_tw"));
        assertEquals("zh_tw", Languages.normalize("zh-Hant-HK"));
        assertEquals("zh_cn", Languages.normalize("zh_cn"));
        assertEquals("zh_cn", Languages.normalize("zh"));
        assertEquals("pt_br", Languages.normalize("pt_br"));
        assertEquals("pt_br", Languages.normalize("pt_ao"));
        assertEquals("pt_pt", Languages.normalize("pt_pt"));
        assertEquals("pt_br", Languages.normalize("pt"));
        assertEquals("sv", Languages.normalize("sv_se"));
        assertEquals("en", Languages.normalize("se_no"));
        assertEquals("lol_us", Languages.normalize("lol_us"));
        assertEquals("en_pt", Languages.normalize("en_pt"));
        assertEquals("qya_aa", Languages.normalize("qya_aa"));
        assertEquals("he", Languages.normalize("iw_il"));
        assertEquals("id", Languages.normalize("in_id"));
        assertEquals("no", Languages.normalize("nb_no"));
        assertEquals("no", Languages.normalize("nn_no"));
    }

    @Test
    @DisplayName("should fall back to English on unknown locales")
    void shouldFallBackToEnglish() {
        assertEquals("en", Languages.normalize(null));
        assertEquals("en", Languages.normalize(""));
        assertEquals("en", Languages.normalize("  "));
        assertEquals("en", Languages.normalize("xx_yy"));
        assertEquals("en", Languages.normalize("sw_ke"));
    }

    @Test
    @DisplayName("should match codes, subtags and names case-insensitively")
    void shouldMatchInput() {
        assertEquals(Optional.of("fi"), Languages.matches("fi"));
        assertEquals(Optional.of("fi"), Languages.matches(" FI "));
        assertEquals(Optional.of("sv"), Languages.matches("sv"));
        assertEquals(Optional.of("sv"), Languages.matches("se"));
        assertEquals(Optional.of("pt_br"), Languages.matches("pt-br"));
        assertEquals(Optional.of("zh_tw"), Languages.matches("zh-tw"));
        assertEquals(Optional.of("pt_br"), Languages.matches("portuguese"));
        assertEquals(Optional.of("zh_cn"), Languages.matches("chinese"));
        assertEquals(Optional.of("lol_us"), Languages.matches("lolcat"));
        assertEquals(Optional.of("en_pt"), Languages.matches("pirate speak"));
        assertEquals(Optional.of("qya_aa"), Languages.matches("quENya"));
        assertEquals(Optional.of("no"), Languages.matches("nb"));
        assertEquals(Optional.of("fi"), Languages.matches("finnish"));
        assertEquals(Optional.of("fi"), Languages.matches("Suomi"));
        assertEquals(Optional.of("de"), Languages.matches("DEUTSCH"));
        assertEquals(Optional.of("zh_cn"), Languages.matches("中文"));
        assertEquals(Optional.of("zh_tw"), Languages.matches("繁體中文"));
        assertTrue(Languages.matches("klingon").isEmpty());
        assertTrue(Languages.matches(null).isEmpty());
        assertTrue(Languages.matches("  ").isEmpty());
    }

    @Test
    @DisplayName("should label every supported code")
    void shouldLabelCodes() {
        assertEquals("Suomi (FI)", Languages.label("fi"));
        assertEquals("English (EN)", Languages.label("en"));
        assertEquals("Svenska (SV)", Languages.label("sv"));
        assertEquals("Português (PT_BR)", Languages.label("pt_br"));
        for (String code : Languages.SUPPORTED) {
            assertFalse(Languages.nativeName(code).isBlank());
            assertTrue(Languages.isSupported(code));
        }
        assertFalse(Languages.isSupported("xx"));
    }

    @Test
    @DisplayName("should round-trip storage ids")
    void shouldRoundTripIds() {
        for (String code : Languages.SUPPORTED) {
            assertEquals(Optional.of(code), Languages.codeOf(Languages.idOf(code)));
        }
        assertTrue(Languages.codeOf(-1).isEmpty());
        assertTrue(Languages.codeOf(99).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> Languages.idOf("xx"));
    }
}
