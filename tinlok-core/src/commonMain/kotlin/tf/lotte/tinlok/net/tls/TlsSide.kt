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
 * An enumeration of TLS connection sides. This is used when creating a [TlsObject].
 */
public enum class TlsSide {
    /** This is a client socket, connecting to a TLS server. */
    CLIENT,
    /** This is a server socket, accepting new client connections. */
    SERVER,
    ;
}
