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
import kotlinx.cinterop.ptr
import platform.windows.*
import tf.veriny.tinlok.Unsafe
import tf.veriny.tinlok.util.AtomicSafeCloseable
import tf.veriny.tinlok.util.ClosingScope

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
public actual class ReentrantLock
public actual constructor(scope: ClosingScope) : SynchronousLock, AtomicSafeCloseable() {
    private val arena = Arena()
    internal val section = arena.alloc<CRITICAL_SECTION>()

    init {
        InitializeCriticalSection(section.ptr)
    }

    /**
     * Obtains this lock, runs [block], releases the lock (if possible), then returns the result of
     * [block].
     */
    public override fun <R> withLocked(block: () -> R): R {
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

    /**
     * Creates a new [Condition] associated with this lock.
     */
    @OptIn(Unsafe::class)
    public actual fun condition(scope: ClosingScope): Condition {
        val cond = Condition(this)
        scope.add(cond)
        return cond
    }
}
