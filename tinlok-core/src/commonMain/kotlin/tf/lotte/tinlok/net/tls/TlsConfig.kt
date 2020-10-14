/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.tls

/**
 * Configuration for any TLS connection.
 */
public data class TlsConfig(
    /** If TLS 1.2 should be used. If False, only TLS 1.3 will be used. */
    public val useTlsv12: Boolean = true,
    /** The list of ALPN protocols to use (e.g. h2). */
    public val alpnProtocols: List<String> = listOf()
) {
    public companion object {
        /** A configuration with sensible defaults. */
        public val DEFAULT: TlsConfig = TlsConfig()
    }
}
