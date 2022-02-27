/*
 * Copyright (C) 2020-2022 Lura Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.util

import tf.veriny.tinlok.Unsafe
import tf.veriny.tinlok.system.FD
import tf.veriny.tinlok.system.Syscall

/**
 * Automatically implements good [Closeable] behaviour.
 */
public abstract class EasyFdCloseable : AtomicSafeCloseable() {
    /** The file descriptor associated with this resource. */
    public abstract val fd: FD

    @OptIn(Unsafe::class)
    override fun closeImpl() {
        Syscall.close(fd)
    }
}
