/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.socket

import tf.lotte.tinlok.io.HalfCloseableStream
import tf.lotte.tinlok.net.ConnectionInfo

/**
 * A client socket that performs I/O synchronously.
 *
 * @param ADDR: The type of address this client socket uses.
 */
public interface ClientSocket<I : ConnectionInfo>
    : HalfCloseableStream, Socket {
    /** The remote address this socket is connected to. */
    public val remoteAddress: I
}
