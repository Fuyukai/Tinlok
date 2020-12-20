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
 * A simple re-entrant lock that can be used to synchronise resources concurrently. This lock is
 * light-weight (using a pthreads futex or a Windows critical section), and re-entrant (so that
 * the same thread can obtain it multiple times without deadlocking).
 *
 * Locks are intended to be allocated globally once and then shared between workers. However, since
 * they hold on to an external resource, they can be closed (at which point the underlying lock
 * becomes invalid), but this is not normally needed. Avoid using locks in instance variables, for
 * example.
 *
 * To obtain the lock, use the [withLocked] function.
 */
public expect class ReentrantLock() : Closeable {
    /**
     * Obtains this lock, runs [block], releases the lock (if possible), then returns the result of
     * [block].
     */
    public fun <R> withLocked(block: () -> R): R
}
