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
import tf.lotte.tinlok.net.ConnectionInfo

/**
 * Defines a server-side socket that accepts new connections.
 */
public interface AcceptingSeverSocket<I : ConnectionInfo, T : ClientSocket<I>> : ServerSocket {
    /**
     * Accepts a new connection, returning a synchronous client socket for the incoming connection.
     *
     * This method is unsafe as it can leak file descriptors.
     */
    @Unsafe
    public fun unsafeAccept(): T
}
