/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.concurrent

import kotlinx.cinterop.*
import platform.posix.*
import tf.lotte.tinlok.system.Syscall
import tf.lotte.tinlok.util.AtomicBoolean
import tf.lotte.tinlok.util.AtomicSafeCloseable
import tf.lotte.tinlok.util.ClosingScope

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
    internal val mutex = arena.alloc<pthread_mutex_t>()

    internal val isOpen: AtomicBoolean get() = _isOpen

    init {
        scope.add(this)

        val attr = arena.alloc<pthread_mutexattr_t>()
        if (pthread_mutexattr_settype(attr.ptr, PTHREAD_MUTEX_RECURSIVE.convert()) != 0) {
            Syscall.throwErrno(errno)
        }

        if (pthread_mutex_init(mutex.ptr, attr.ptr) != 0) {
            Syscall.throwErrno(errno)
        }
    }

    public override fun closeImpl() {
        pthread_mutex_destroy(mutex.ptr)
        arena.clear()
    }

    /**
     * Obtains this lock, runs [block], releases the lock (if possible), then returns the result of
     * [block].
     */
    public override fun <R> withLocked(block: () -> R): R {
        checkOpen()

        if (pthread_mutex_lock(mutex.ptr) != 0) {
            Syscall.throwErrno(errno)
        }

        try {
            return block()
        } finally {
            if (pthread_mutex_unlock(mutex.ptr) != 0) {
                Syscall.throwErrno(errno)
            }
        }
    }

    /**
     * Creates a new [Condition] associated with this lock.
     *
     * This method will acquire the lock, create the condition, then release it.
     */
    public actual fun condition(scope: ClosingScope): Condition {
        checkOpen()

        val cond = Condition(this)
        scope.add(cond)
        return cond
    }

}
