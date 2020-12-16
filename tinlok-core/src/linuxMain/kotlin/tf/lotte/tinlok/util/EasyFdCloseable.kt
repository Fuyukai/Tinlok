/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.util

import tf.lotte.tinlok.Unsafe
import tf.lotte.tinlok.io.async.Selectable
import tf.lotte.tinlok.system.Syscall

/**
 * Automatically implements good [Closeable] behaviour.
 */
public abstract class EasyFdCloseable : AtomicSafeCloseable(), Selectable {
    @OptIn(Unsafe::class)
    override fun closeImpl() {
        Syscall.close(fd)
    }
}