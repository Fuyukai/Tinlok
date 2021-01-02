/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.concurrent

import tf.veriny.tinlok.util.Closeable

/**
 * A base interface for any sort of synchronous lock. This lock is closeable, as it can hold on to
 * an external operating system-based lock.
 */
public interface SynchronousLock : Closeable {
    /**
     * Obtains this lock, runs [block], releases the lock (if possible), then returns the result of
     * [block].
     */
    public fun <R> withLocked(block: () -> R): R
}
