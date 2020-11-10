/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.socket

import tf.lotte.tinlok.Unsafe
import tf.lotte.tinlok.io.async.Selectable
import tf.lotte.tinlok.net.*
import tf.lotte.tinlok.net.tcp.TcpConnectionInfo
import tf.lotte.tinlok.net.udp.UdpConnectionInfo
import tf.lotte.tinlok.system.BlockingResult
import tf.lotte.tinlok.util.AtomicBoolean
import tf.lotte.tinlok.util.Closeable

/**
 * A BSD socket, an abstraction used for inter-process communication. Sockets can be either local or
 * over a network.
 */
public actual interface Socket<I: ConnectionInfo> : Selectable, Closeable {
    public actual companion object {
        /**
         * Creates a new unconnected TCP socket.
         */
        @Unsafe
        actual fun tcp(
            family: AddressFamily,
        ): Socket<TcpConnectionInfo> {
            TODO("Not yet implemented")
        }

        /**
         * Creates a new unconnected UDP socket.
         */
        @Unsafe
        actual fun udp(
            family: AddressFamily,
        ): Socket<UdpConnectionInfo> {
            TODO("Not yet implemented")
        }

    }

    /** The address family this socket was created with. */
    public actual val family: AddressFamily

    /** The socket type this socket was created with. */
    public actual val type: SocketType

    /** The protocol this socket was created with. */
    public actual val protocol: IPProtocol

    /** If this socket is still open. */
    actual val isOpen: AtomicBoolean

    /** If this socket is non-blocking. */
    actual var nonBlocking: Boolean

    /**
     * Sets the [option] on this BSD socket to [value].
     */
    public actual fun <T> setOption(option: BsdSocketOption<T>, value: T)

    /**
     * Gets the [option] on this BSD socket.
     */
    public actual fun <T> getOption(option: BsdSocketOption<T>): T

    /**
     * Connects a socket to a remote endpoint. For blocking sockets, the [timeout] parameter
     * controls how long to wait for the connection to complete. For non-blocking sockets, the
     * parameter has no effect.
     *
     * If the socket is non-blocking, this returns if the socket was connected immediately or if it
     * requires a poll() operation to notify when connected.
     */
    public actual fun connect(addr: I, timeout: Int): Boolean

    /**
     * Binds this socket to the specified [addr].
     */
    public actual fun bind(addr: I)

    /**
     * Marks this socket for listening with the specified [backlog].
     */
    public actual fun listen(backlog: Int)

    /**
     * Accepts a new client connection, returning the newly connected [Socket]. Returns null if this
     * is a non-blocking socket and no connections are available.
     */
    @Unsafe
    public actual fun accept(): Socket<I>?

    /**
     * Receives up to [size] bytes from this socket into [buf], starting at [offset], using the
     * specified [flags].
     */
    public actual fun recv(buf: ByteArray, size: Int, offset: Int, flags: Int): BlockingResult

    /**
     * Receives up to [size] bytes from this socket into [buf], starting at [offset], using the
     * specified [flags]. This returns a [RecvFrom] which wraps both the [BlockingResult] for the
     * bytes read, and the address read from. A null return is the same as a -1 BlockingResult.
     *
     * .. warning::
     *
     *     This will raise an error on connection-oriented sockets; it is designed to be used on
     *     connectionless protocols.
     */
    public actual fun recvfrom(buf: ByteArray, size: Int, offset: Int, flags: Int): RecvFrom<I>?

    /**
     * Sends up to [size] bytes from [buf] into this socket, starting at [offset], using the
     * specified [flags].
     */
    public actual fun send(buf: ByteArray, size: Int, offset: Int, flags: Int): BlockingResult

    /**
     * Sends up to [size] bytes from [buf] into this socket, starting at [offset], using the
     * specified [flags], to the specified [addr].
     *
     * .. warning::
     *
     *     The ``addr`` parameter is ignored on connection-oriented sockets.
     */
    public actual fun sendto(
        buf: ByteArray, size: Int, offset: Int, flags: Int, addr: I
    ): BlockingResult

    /**
     * Shuts down this socket either at one end or both ends.
     */
    public actual fun shutdown(how: ShutdownOption)

}
