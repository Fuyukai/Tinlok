/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net

import tf.lotte.cc.ClosingScope
import tf.lotte.cc.Unsafe
import tf.lotte.cc.exc.OSException
import tf.lotte.cc.net.ConnectionInfo
import tf.lotte.cc.net.tcp.TcpConnectionInfo
import tf.lotte.cc.net.tcp.TcpSocketAddress
import tf.lotte.cc.use
import tf.lotte.tinlok.Sys
import tf.lotte.tinlok.net.socket.AcceptingSeverSocket
import tf.lotte.tinlok.net.socket.ClientSocket
import tf.lotte.tinlok.net.socket.StandardSocketOption
import tf.lotte.tinlok.net.tcp.TcpClientSocket
import tf.lotte.tinlok.net.tcp.TcpServerSocket
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
    address: TcpSocketAddress, timeout: Int = 30_000, block: (TcpClientSocket) -> R,
): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    val sock = unsafeOpen(address, timeout)
    // TODO: TCP_NODELAY and TCP_NOTSENT_LOWAT if needed

    return sock.use(block)
}

/**
 * Opens a new TCP connection to the specified [address], using the default socket options, and
 * and timing out after [timeout] milliseconds. If [timeout] is negative, no timeout will be used
 * and a standard blocking connect will be issued.
 */
@OptIn(Unsafe::class)
public fun TcpClientSocket.Companion.connect(
    scope: ClosingScope, address: TcpSocketAddress, timeout: Int = 30_000,
): TcpClientSocket {
    val sock = unsafeOpen(address, timeout)
    scope.add(sock)
    return sock
}

/**
 * Creates a new unbound TCP server socket.
 */
@OptIn(Unsafe::class, ExperimentalContracts::class)
public inline fun <R> TcpServerSocket.Companion.open(
    address: TcpConnectionInfo,
    block: (TcpServerSocket) -> R,
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
    address: TcpConnectionInfo, backlog: Int = 128, block: (TcpServerSocket) -> R,
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
 * the specified lambda [block]. The connection will be automatically closed when the block
 * returns.
 */
@OptIn(Unsafe::class, ExperimentalContracts::class)
public inline fun <R, I : CI, T : ClientSocket<I>> AcceptingSeverSocket<I, T>.accept(
    block: (T) -> R,
): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return unsafeAccept().use(block)
}

/**
 * Accepts a new connection, returning a new synchronous client socket, and adding it to the
 * specified [scope] for automatic closing.
 */
@OptIn(Unsafe::class)
public fun <I : CI, T : ClientSocket<I>> AcceptingSeverSocket<I, T>.accept(scope: ClosingScope): T {
    val sock = unsafeAccept()
    scope.add(sock)
    return sock
}
