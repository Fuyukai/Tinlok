/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.concurrent

import kotlinx.cinterop.Arena
import kotlinx.cinterop.alloc
import kotlinx.cinterop.ptr
import platform.windows.*
import tf.lotte.tinlok.util.AtomicSafeCloseable
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
public actual class ReentrantLock : Closeable, AtomicSafeCloseable() {
    private val arena = Arena()
    private val section = arena.alloc<CRITICAL_SECTION>()

    init {
        InitializeCriticalSection(section.ptr)
    }

    /**
     * Obtains this lock, runs [block], releases the lock (if possible), then returns the result of
     * [block].
     */
    public actual fun <R> withLocked(block: () -> R): R {
        EnterCriticalSection(section.ptr)

        try {
            return block()
        } finally {
            LeaveCriticalSection(section.ptr)
        }
    }

    /**
     * Closes this lock and releases any underlying structure it may have. This is generally
     * tricky (as the futex may be destroyed whilst locked), and generally isn't needed if locks
     * are scoped to a companion object.
     */
    override fun closeImpl() {
        arena.clear()
        DeleteCriticalSection(section.ptr)
    }
}
