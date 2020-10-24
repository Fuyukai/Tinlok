/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.tls

import tf.lotte.cc.Unsafe
import tf.lotte.tinlok.net.socket.PlatformSockets
import tf.lotte.tinlok.net.tcp.TcpSocketAddress

/**
 * Implementation of the TLS client socket using libtls.
 */
internal expect class LibtlsClientSocket : TlsClientSocket

/**
 * Opens a new connected TLS synchronous socket.
 */
@Unsafe
public expect fun PlatformSockets.newTlsSynchronousSocket(
    address: TcpSocketAddress, config: TlsConfig, timeout: Int
): TlsClientSocket


