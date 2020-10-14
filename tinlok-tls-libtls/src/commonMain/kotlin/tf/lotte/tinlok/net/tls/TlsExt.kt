/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.tls

import tf.lotte.tinlok.net.socket.PlatformSockets
import tf.lotte.tinlok.net.tcp.TcpSocketAddress
import tf.lotte.tinlok.util.ClosingScope
import tf.lotte.tinlok.util.Unsafe
import tf.lotte.tinlok.util.use


/**
 * Creates a new connected [TlsClientSocket] using the specified [address] and [timeout].
 *
 * This method is unsafe as it can leak file descriptors and memory related to TLS
 * structures.
 */
@Unsafe
public fun TlsClientSocket.Companion.unsafeOpen(
    address: TcpSocketAddress, config: TlsConfig, timeout: Int
): TlsClientSocket {
    return PlatformSockets.newTlsSynchronousSocket(address, config, timeout)
}


/**
 * Opens a new TLS connection to the specified [address], using the specified TLS [config], using
 * the default socket options, passing the created socket to the specified lambda, and timing out
 * after [timeout] milliseconds. If [timeout] is negative, no timeout will be used and a standard
 * blocking connect will be issued.
 */
@OptIn(Unsafe::class)
public inline fun <R> TlsClientSocket.Companion.connect(
    address: TcpSocketAddress, config: TlsConfig = TlsConfig.DEFAULT,
    timeout: Int = 30_000, block: (TlsClientSocket) -> R
): R {
    val socket = unsafeOpen(address, config, timeout)
    return socket.use(block)
}

/**
 * Opens a new TLS connection to the specified [address], using the specified TLS [config], using
 * the default socket options, adding the created socket to the specified [scope], and timing out
 * after [timeout] milliseconds. If [timeout] is negative, no timeout will be used and a standard
 * blocking connect will be issued.
 */
@OptIn(Unsafe::class)
public fun TlsClientSocket.Companion.connect(
    scope: ClosingScope, address: TcpSocketAddress, config: TlsConfig = TlsConfig.DEFAULT,
    timeout: Int = 30_000,
): TlsClientSocket {
    val socket = unsafeOpen(address, config, timeout)
    scope.add(socket)
    return socket
}
