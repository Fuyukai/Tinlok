/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.time

import tf.veriny.tinlok.util.microsleep
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Tests the monotonic timestamp functionality.
 */
@OptIn(ExperimentalUnsignedTypes::class)
public class `Test Monotonic Timestamp` {
    @Test
    public fun `Test timestamps obey the linear passage of time`() {
        val ts1 = monotonicTimestamp()
        microsleep(1_000_000U)
        val ts2 = monotonicTimestamp()
        assertTrue(ts2 > ts1)

        // td2 needs to be at least 1 second after ts1
        assertTrue(ts2 > (ts1 + 1_000_000))
    }
}
