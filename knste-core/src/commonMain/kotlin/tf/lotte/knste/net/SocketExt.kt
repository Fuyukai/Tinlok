/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.knste.net

import tf.lotte.knste.Sys
import tf.lotte.knste.io.use
import tf.lotte.knste.net.socket.ClientSocket
import tf.lotte.knste.net.socket.ServerSocket
import tf.lotte.knste.net.socket.SocketAddress
import tf.lotte.knste.net.socket.StandardSocketOption
import tf.lotte.knste.net.tcp.TcpClientSocket
import tf.lotte.knste.net.tcp.TcpConnectionInfo
import tf.lotte.knste.net.tcp.TcpServerSocket
import tf.lotte.knste.net.tcp.TcpSocketAddress
import tf.lotte.knste.util.Unsafe

private typealias CI = ConnectionInfo
private typealias ADD<I> = SocketAddress<I>
private typealias SCS<I, T> = ClientSocket<I, T>

// == TCP socket helper functions == //

/**
 * Opens a new TCP connection to the specified address, using the default socket options, passing
 * the created socket to the specified lambda.
 */
@OptIn(Unsafe::class)
public inline fun <R> TcpClientSocket.Companion.connect(
    address: TcpSocketAddress, block: (TcpClientSocket) -> R
): R {
    val sock = unsafeOpen(address)
    // TODO: TCP_NODELAY and TCP_NOTSENT_LOWAT if needed

    return sock.use(block)
}

/**
 * Creates a new unbound TCP server socket.
 */
@OptIn(Unsafe::class)
public inline fun <R> TcpServerSocket.Companion.open(
    address: TcpConnectionInfo,
    block: (TcpServerSocket) -> R
): R {
    val sock = unsafeOpen(address)
    return sock.use(block)
}

/**
 * Opens a new TCP server socket, binds it to the specified [address] with the specified
 * [backlog], using the default socket options, passing the created socket to the specified
 * lambda.
 */
@OptIn(Unsafe::class)
public inline fun <R> TcpServerSocket.Companion.bind(
    address: TcpConnectionInfo, backlog: Int = 128, block: (TcpServerSocket) -> R
): R {
    return TcpServerSocket.open(address) {
        // Always set REUSEADDR on non-Windows
        if (!Sys.osInfo.isWindows) {
            it.setSocketOption(StandardSocketOption.SO_REUSEADDR, true)
        }

        it.bind(backlog)
        block(it)
    }
}

// == Server socket helpers == //
/**
 * Accepts a new connection, passing a synchronous client socket for the incoming connection to
 * the specified lambda [block]. The connection will be automatically closed when the bloc
 */
@OptIn(Unsafe::class)
public inline fun <R, I: CI, ADDR : ADD<I>, T : SCS<I, ADDR>> ServerSocket<*, *, T>.accept(
    block: (T) -> R
): R {
    return unsafeAccept().use(block)
}
