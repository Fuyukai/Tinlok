/*
 * Copyright (C) 2020-2022 Lura Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.concurrent

import kotlinx.cinterop.Arena
import kotlinx.cinterop.alloc
import kotlinx.cinterop.ptr
import platform.posix.*
import tf.veriny.tinlok.util.*

/**
 * A condition variable that can be used for a worker thread to suspend execution until signalled
 * by another thread. This is usually used to signal when some state has changed, and as such a
 * condition is associated with a lock which should be locked before this condition is used.
 *
 * A condition cannot be used when its parent lock is closed.
 */
public actual class Condition
internal constructor(private val parent: ReentrantLock) : Closeable, AtomicSafeCloseable() {
    private val isOpen = AtomicBoolean(true)
    private val arena = Arena()
    private val condition = arena.alloc<pthread_cond_t>()

    init {
        pthread_cond_init(condition.ptr, null)
    }

    /**
     * Waits for this condition to be signalled by another thread.
     */
    public actual fun wait() {
        if (!parent.isOpen) throw ClosedException("Parent lock has been closed")

        parent.withLocked {
            pthread_cond_wait(condition.ptr, parent.mutex.ptr)
        }
    }

    /**
     * Wakes up a single thread waiting on this condition. The order that threads will be woken up
     * in is unspecified.
     */
    public actual fun wakeupOne() {
        if (!parent.isOpen) throw ClosedException("Parent lock has been closed")

        parent.withLocked {
            pthread_cond_signal(condition.ptr)
        }
    }

    /**
     * Wakes up all threads waiting on this condition. The order that threads will be woken up
     * in is unspecified.
     */
    public actual fun wakeupAll() {
        if (!parent.isOpen) throw ClosedException("Parent lock has been closed")

        parent.withLocked {
            pthread_cond_broadcast(condition.ptr)
        }
    }

    override fun closeImpl() {
        pthread_cond_destroy(condition.ptr)
        arena.clear()
    }
}
