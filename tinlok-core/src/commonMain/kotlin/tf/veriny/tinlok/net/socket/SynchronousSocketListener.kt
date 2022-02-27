/*
 * Copyright (C) 2020-2022 Lura Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.net.socket

import tf.veriny.tinlok.Unsafe
import tf.veriny.tinlok.io.Listener
import tf.veriny.tinlok.net.ConnectionInfo
import tf.veriny.tinlok.net.SocketType
import tf.veriny.tinlok.util.Closeable

/**
 * A [Listener] that wraps a synchronous streaming socket.
 */
public class SynchronousSocketListener<I : ConnectionInfo>(
    public val socket: Socket<I>,
) : Listener<Socket<I>>, Closeable by socket {
    init {
        require(!socket.nonBlocking) { "Socket must be blocking" }
        require(socket.isOpen.value) { "Socket must be open" }
        require(socket.type == SocketType.SOCK_STREAM) {
            "Can only listen on stream-based sockets"
        }
    }

    @Unsafe
    override fun unsafeAccept(): Socket<I> {
        return socket.accept()!!
    }
}
