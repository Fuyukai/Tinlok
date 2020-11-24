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
 * The configuration for a TLS connection.
 */
public data class TlsConnectionConfig(
    /**
     * The side for this connection i.e. if this will be a client-sided or server-sided connection.
     */
    public val side: TlsSide,
    /**
     * The set of valid TLS protocols to use for the connection. By default, this is TLS 1.2 and
     * TLS 1.3.
     */
    public val versions: Set<TlsVersion> = setOf(TlsVersion.TLS_V12, TlsVersion.TLS_V13),
    /**
     * The set of ALPN protocols to use. See RFC 8447 for more information.
     */
    public val alpnProtocols: MutableSet<String> = mutableSetOf()
)
