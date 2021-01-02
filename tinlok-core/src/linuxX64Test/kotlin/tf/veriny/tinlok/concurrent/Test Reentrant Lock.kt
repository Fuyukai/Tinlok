/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.concurrent

import tf.veriny.tinlok.util.ClosingScope
import kotlin.native.concurrent.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Tests the reentrant lock, using workers.
 */
class `Test Reentrant Lock` {
    /**
     * Ensures recursive locking doesn't lock up (pun intended).
     */
    @Test
    public fun `Test recursive locking`(): Unit = ClosingScope {
        val lock = ReentrantLock(it)

        val res = lock.withLocked {
            lock.withLocked {
                1
            }
        }

        assertEquals(1, res)
    }

    /**
     * Ensures a lock is unlocked on an exception.
     */
    @Test
    public fun `Test unlocking on exception`(): Unit = ClosingScope {
        val lock = ReentrantLock(it)

        assertFailsWith<IllegalStateException> {
            lock.withLocked {
                lock.withLocked {
                    error("Should error!")
                }
            }
        }
    }

    /**
     * Ensures that locking works for locking.
     */
    @Test
    public fun `Test concurrent lock acquiring`(): Unit = ClosingScope {
        val lock = ReentrantLock(it)

        val worker = Worker.start()
        it.add { worker.requestTermination(false) }

        // Inside the outer withLocked block, we make sure the atomic isn't updated.
        // It won't be updated because the worker is also waiting for a lock, which the test thread
        // is holding on to. Then, when the outer withLock finishes, we can check the atomic again
        // (which should have been updated the worker thread)
        // and ensure it grabbed the lock, and is now at 1.

        val atomic = AtomicInt(0)
        // The pair needs to be frozen to be safely shared between threads.
        val pair = Pair(lock, atomic).also { it.freeze() }

        val fut = lock.withLocked {
            val fut = worker.execute(TransferMode.SAFE, { pair }) {
                val (lock, atomic) = it
                lock.withLocked {
                    atomic.increment()
                }
            }

            // Check the atomic is still at zero
            val result = atomic.compareAndSwap(0, 1)
            assertEquals(0, result, "Atomic int was not zero " +
                "(i.e. was modified by worker)")

            fut
        }

        // Wait for the worker to acquire the lock, increment the atomic, and exit.
        fut.consume {}
        // Now verify that it did edit the atomic
        val result = atomic.compareAndSwap(2, 2)
        assertEquals(2, result, "Atomic int was not 2 (i.e. was not modified by worker)")
    }
}
