/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.concurrent

import tf.lotte.tinlok.util.Closeable

/**
 * A condition variable that can be used for a worker thread to suspend execution until signalled
 * by another thread. This is usually used to signal when some state has changed, and as such a
 * condition is associated with a lock which should be locked before this condition is used.
 *
 * A condition cannot be used when its parent lock is closed.
 */
public expect class Condition : Closeable {
    /**
     * Waits for this condition to be signalled by another thread.
     */
    public fun wait()

    /**
     * Wakes up a single thread waiting on this condition. The order that threads will be woken up
     * in is unspecified.
     */
    public fun wakeupOne()

    /**
     * Wakes up all threads waiting on this condition. The order that threads will be woken up
     * in is unspecified.
     */
    public fun wakeupAll()
}
