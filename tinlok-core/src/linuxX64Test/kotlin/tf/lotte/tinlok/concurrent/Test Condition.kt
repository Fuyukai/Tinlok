/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.concurrent

import tf.lotte.tinlok.util.ClosingScope
import tf.lotte.tinlok.util.microsleep
import kotlin.native.concurrent.*
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests the Condition class.
 */
public class `Test Condition` {
    /**
     * Tests waking up a single thread for a condition variable.
     */
    @OptIn(ExperimentalUnsignedTypes::class)
    @Test
    public fun `Test condition single wakeup`(): Unit = ClosingScope {
        val lock = ReentrantLock(it)
        val cond = lock.condition(it)

        // Overall, this is similar code to the reentrant lock tests.
        // We use an atomic int with a counter, ensure it's not changed before, then ensure it's
        // changed after.
        val counter = AtomicInt(0)
        val worker = Worker.start(name = "Condition test worker")
        it.add { worker.requestTermination(processScheduledJobs = false) }
        val pair = Pair(cond, counter).freeze()
        val future = worker.execute(TransferMode.SAFE, { pair }) {
            // deliberately shadow
            val (cond, counter) = it
            cond.wait()
            counter.increment()
        }

        // perhaps sleeping is not the best idea!
        // but, this needs it to ensure that the worker acquires the lock successfully.
        // otherwise, we wake up the condition before it gets a chance to wait on it.
        microsleep(1000U * 1000U)

        // Now, the worker is waiting on a condition variable, so the counter should still be at 0:
        val counterFirst = counter.compareAndSwap(0, 1)
        assertEquals(0, counterFirst, "Counter was incremented (i.e. worker didn't wait)")
        // Wake up the worker, which is blocked on the condition.
        cond.wakeupOne()
        // Wait for the future to terminate.
        waitForMultipleFutures(setOf(future), 500)
        // Now the worker will have modified the counter, and it should be set to 2.
        assertEquals(
            2, counter.value,
            "Counter wasn't incremented (i.e. worker didn't do anything)"
        )
    }
}
