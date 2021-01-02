/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.concurrent

import kotlinx.cinterop.Arena
import kotlinx.cinterop.alloc
import platform.windows.*
import tf.veriny.tinlok.util.AtomicSafeCloseable
import tf.veriny.tinlok.util.Closeable

/**
 * A condition variable that can be used for a worker thread to suspend execution until signalled
 * by another thread. This is usually used to signal when some state has changed, and as such a
 * condition is associated with a lock which should be locked before this condition is used.
 *
 * A condition cannot be used when its parent lock is closed.
 */
public actual class Condition
internal constructor(private val parent: ReentrantLock) : Closeable, AtomicSafeCloseable() {
    private val arena = Arena()
    private val cond = arena.alloc<CONDITION_VARIABLE>()

    init {
        InitializeConditionVariable(cond.ptr)
    }

    /**
     * Waits for this condition to be signalled by another thread.
     */
    actual fun wait() {
        parent.withLocked {
            SleepConditionVariableCS(cond.ptr, parent.section.ptr, INFINITE)
        }
    }

    /**
     * Wakes up a single thread waiting on this condition. The order that threads will be woken up
     * in is unspecified.
     */
    actual fun wakeupOne() {
        parent.withLocked {
            WakeConditionVariable(cond.ptr)
        }
    }

    /**
     * Wakes up all threads waiting on this condition. The order that threads will be woken up
     * in is unspecified.
     */
    actual fun wakeupAll() {
        parent.withLocked {
            WakeAllConditionVariable(cond.ptr)
        }
    }

    /**
     * Closes this resource.
     */
    override fun closeImpl(): Unit = arena.clear()
}
