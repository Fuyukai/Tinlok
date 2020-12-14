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
import tf.lotte.tinlok.io.Buffer
import tf.lotte.tinlok.io.OSException
import tf.lotte.tinlok.io.async.Selectable
import tf.lotte.tinlok.net.*
import tf.lotte.tinlok.net.tcp.TcpConnectionInfo
import tf.lotte.tinlok.net.udp.UdpConnectionInfo
import tf.lotte.tinlok.system.BlockingResult
import tf.lotte.tinlok.system.SOCKET
import tf.lotte.tinlok.system.Syscall
import tf.lotte.tinlok.util.AtomicBoolean
import tf.lotte.tinlok.util.Closeable
import tf.lotte.tinlok.util.ClosedException

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
        public actual fun tcp(
            family: AddressFamily,
        ): Socket<TcpConnectionInfo> {
            val type = SocketType.SOCK_STREAM
            val proto = IPProtocol.IPPROTO_TCP
            val sock = Syscall.socket(family, type, proto)

            return WindowsSocket(family, type, proto, sock, ::TcpConnectionInfo)
        }

        /**
         * Creates a new unconnected UDP socket.
         */
        @Unsafe
        public actual fun udp(
            family: AddressFamily,
        ): Socket<UdpConnectionInfo> {
            val type = SocketType.SOCK_DGRAM
            val proto = IPProtocol.IPPROTO_UDP
            val sock = Syscall.socket(family, type, proto)

            return WindowsSocket(family, type, proto, sock, ::UdpConnectionInfo)
        }

    }

    /** The underlying handle for this socket. */
    public val handle: SOCKET

    /** The address family this socket was created with. */
    public actual val family: AddressFamily

    /** The socket type this socket was created with. */
    public actual val type: SocketType

    /** The protocol this socket was created with. */
    public actual val protocol: IPProtocol

    /** If the FD for this socket is still open. */
    public actual val isOpen: AtomicBoolean

    /** If this file descriptor is non-blocking. */
    public actual var nonBlocking: Boolean

    /**
     * Sets the [option] on this BSD socket to [value].
     */
    @Throws(ClosedException::class, OSException::class)
    public actual fun <T> setOption(option: BsdSocketOption<T>, value: T)

    /**
     * Gets the [option] on this BSD socket.
     */
    @Throws(ClosedException::class, OSException::class)
    public actual fun <T> getOption(option: BsdSocketOption<T>): T

    /**
     * Connects a socket to a remote endpoint. For blocking sockets, the [timeout] parameter
     * controls how long to wait for the connection to complete. For non-blocking sockets, the
     * parameter has no effect.
     *
     * If the socket is non-blocking, this returns if the socket was connected immediately or if it
     * requires a poll() operation to notify when connected.
     */
    @Throws(ClosedException::class, OSException::class)
    public actual fun connect(addr: I, timeout: Int): Boolean

    /**
     * Binds this socket to the specified [addr].
     */
    @Throws(ClosedException::class, OSException::class)
    public actual fun bind(addr: I)

    /**
     * Marks this socket for listening with the specified [backlog].
     */
    @Throws(ClosedException::class, OSException::class)
    public actual fun listen(backlog: Int)

    /**
     * Accepts a new client connection, returning the newly connected [Socket]. Returns null if this
     * is a non-blocking socket and no connections are available.
     */
    @Throws(ClosedException::class, OSException::class)
    @Unsafe
    public actual fun accept(): Socket<I>?

    /**
     * Receives up to [size] bytes from this socket into [buf], starting at [offset], using the
     * specified [flags].
     */
    @Throws(ClosedException::class, OSException::class)
    public actual fun recv(buf: ByteArray, size: Int, offset: Int, flags: Int): BlockingResult

    /**
     * Receives up to [size] bytes from this socket into the buffer [buf], using the specified
     * [flags].
     */
    public actual fun recv(buf: Buffer, size: Int, flags: Int): BlockingResult

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
    @Throws(ClosedException::class, OSException::class)
    public actual fun recvfrom(buf: ByteArray, size: Int, offset: Int, flags: Int): RecvFrom<I>?

    /**
     * Sends up to [size] bytes from [buf] into this socket, starting at [offset], using the
     * specified [flags].
     */
    @Throws(ClosedException::class, OSException::class)
    public actual fun send(buf: ByteArray, size: Int, offset: Int, flags: Int): BlockingResult

    /**
     * Sends up to [size] bytes from [buf] into this socket, using the specified [flags].
     */
    public actual fun send(buf: Buffer, size: Int, flags: Int): BlockingResult

    /**
     * Attempts to send *all* [size] bytes from [buf] into this socket, starting at [offset], using
     * the specified [flags], returning the actual number of bytes written. This will attempt retry
     * logic.
     */
    public actual fun sendall(buf: ByteArray, size: Int, offset: Int, flags: Int): BlockingResult

    /**
     * Sends up to [size] bytes from [buf] into this socket. This will attempt retry logic.
     */
    public actual fun sendall(buf: Buffer, size: Int, flags: Int): BlockingResult

    /**
     * Sends up to [size] bytes from [buf] into this socket, starting at [offset], using the
     * specified [flags], to the specified [addr].
     *
     * .. warning::
     *
     *     The ``addr`` parameter is ignored on connection-oriented sockets.
     */
    @Throws(ClosedException::class, OSException::class)
    public actual fun sendto(
        buf: ByteArray, size: Int, offset: Int, flags: Int, addr: I
    ): BlockingResult

    /**
     * Shuts down this socket either at one end or both ends.
     */
    @Throws(ClosedException::class, OSException::class)
    public actual fun shutdown(how: ShutdownOption)
}
