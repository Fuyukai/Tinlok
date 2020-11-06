/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.socket

import tf.lotte.cc.io.HalfCloseableStream
import tf.lotte.cc.net.ConnectionInfo

/**
 * Defines a client socket that is also a [HalfCloseableStream]. This is used for SOCK_STREAM
 * based sockets.
 */
public interface StreamingClientSocket<I: ConnectionInfo?> : ClientSocket<I>, HalfCloseableStream
