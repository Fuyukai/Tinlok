/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.tls

import tf.lotte.tinlok.net.tcp.TcpClientSocket

/**
 * A client socket that is encrypted using TLS.
 */
public interface TlsClientSocket : TcpClientSocket {
    public companion object;

    /** The configuration this socket was created with. */
    public val config: TlsConfig
}
