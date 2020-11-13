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
import tf.lotte.tinlok.io.remaining
import tf.lotte.tinlok.system.BlockingResult
import tf.lotte.tinlok.system.Syscall

/**
 * Retries a send operation for [buffer] until it is fully written, or the socket requires polling.
 */
@OptIn(Unsafe::class)
internal actual fun Socket<*>.retrySend(buffer: Buffer): BlockingResult {
    var size = buffer.remaining.toInt()
    var total = 0
    var hitAgain = false

    while (true) {
        // offset zero is ALWAYS relative to the buffer cursor
        // so we always pass zero
        val result = buffer.address(0) {
            Syscall.write(fd, it, size)
        }

        if (result == BlockingResult.WOULD_BLOCK) {
            hitAgain = true
            break
        }

        // written up to the capacity, no more retries
        if (result.count >= buffer.capacity - 1) {
            break
        }

        size -= result.count.toInt()
        total += result.count.toInt()
        buffer.cursor += result.count.toLong()
    }

    return if (hitAgain && total == 0) {
        BlockingResult.WOULD_BLOCK
    } else {
        BlockingResult(total.toLong())
    }
}
