/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net

import tf.lotte.tinlok.Sys
import tf.lotte.tinlok.exc.OSException
import tf.lotte.tinlok.net.socket.AcceptingSeverSocket
import tf.lotte.tinlok.net.socket.ClientSocket
import tf.lotte.tinlok.net.socket.StandardSocketOption
import tf.lotte.tinlok.net.tcp.TcpClientSocket
import tf.lotte.tinlok.net.tcp.TcpConnectionInfo
import tf.lotte.tinlok.net.tcp.TcpServerSocket
import tf.lotte.tinlok.net.tcp.TcpSocketAddress
import tf.lotte.tinlok.util.Unsafe
import tf.lotte.tinlok.util.use
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

private typealias CI = ConnectionInfo

// == TCP socket helper functions == //

/**
 * Opens a new TCP connection to the specified [address], using the default socket options, passing
 * the created socket to the specified lambda, and timing out after [timeout] milliseconds. If
 * [timeout] is negative, no timeout will be used and a standard blocking connect will be issued.
 */
@OptIn(Unsafe::class, ExperimentalContracts::class)
@Throws(OSException::class)
public inline fun <R> TcpClientSocket.Companion.connect(
    address: TcpSocketAddress, timeout: Int = 30_000, block: (TcpClientSocket) -> R
): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    val sock = unsafeOpen(address, timeout)
    // TODO: TCP_NODELAY and TCP_NOTSENT_LOWAT if needed

    return sock.use(block)
}

/**
 * Creates a new unbound TCP server socket.
 */
@OptIn(Unsafe::class, ExperimentalContracts::class)
public inline fun <R> TcpServerSocket.Companion.open(
    address: TcpConnectionInfo,
    block: (TcpServerSocket) -> R
): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    val sock = unsafeOpen(address)
    return sock.use(block)
}

/**
 * Opens a new TCP server socket, binds it to the specified [address] with the specified
 * [backlog], using the default socket options, passing the created socket to the specified
 * lambda.
 */
@OptIn(Unsafe::class, ExperimentalContracts::class)
public inline fun <R> TcpServerSocket.Companion.bind(
    address: TcpConnectionInfo, backlog: Int = 128, block: (TcpServerSocket) -> R
): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

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
@OptIn(Unsafe::class, ExperimentalContracts::class)
public inline fun <R, I : CI, T : ClientSocket<I>> AcceptingSeverSocket<I, T>.accept(
    block: (T) -> R
): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return unsafeAccept().use(block)
}
