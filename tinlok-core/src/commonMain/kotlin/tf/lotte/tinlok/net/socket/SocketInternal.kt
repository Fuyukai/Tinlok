/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.socket

import tf.lotte.tinlok.io.Buffer
import tf.lotte.tinlok.system.BlockingResult

/**
 * Retries a send operation for [buffer] until it is fully written, or the socket requires polling.
 */
internal expect fun Socket<*>.retrySend(buffer: Buffer): BlockingResult
