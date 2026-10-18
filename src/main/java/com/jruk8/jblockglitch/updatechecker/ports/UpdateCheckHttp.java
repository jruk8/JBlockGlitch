package com.jruk8.jblockglitch.updatechecker.ports;

import java.util.Optional;

/** Fetches releases from the host, blocking. Adapted per platform. */
public interface UpdateCheckHttp {
    /** Latest release, or empty when none is published. */
    Optional<GitHubRelease> fetchLatestRelease() throws Exception;
}
