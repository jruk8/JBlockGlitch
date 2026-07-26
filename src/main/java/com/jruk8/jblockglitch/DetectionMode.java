package com.jruk8.jblockglitch;

import java.util.Locale;

enum DetectionMode {
    MEDIUM,
    STRICT;

    static DetectionMode parse(String value) {
        if (value == null) {
            return STRICT;
        }
        try {
            return valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return STRICT;
        }
    }
}
