/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.socket

import tf.lotte.tinlok.net.ConnectionInfo
import tf.lotte.tinlok.util.Unsafe

/**
 * A server socket that synchronously produces new [ClientSocket] instances when
 * accepting.
 *
 *
 */
public interface ServerSocket<
    I : ConnectionInfo,
    ADDR : SocketAddress<I>,
    T : ClientSocket<I, ADDR>
    > : Socket<I, ADDR> {
    /**
     * Binds this socket to the address.
     */
    public fun bind(backlog: Int = 128)  // old linux support for 128

    /**
     * Accepts a new connection, returning a synchronous client socket for the incoming connection.
     *
     * This method is unsafe as it can leak file descriptors.
     */
    @Unsafe
    public fun unsafeAccept(): T
}
