/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.fs.path

import tf.lotte.tinlok.Sys
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Tests the special path functions.
 */
class `Test special Paths`() {
    @Test
    public fun `Test home`() {
        val homeDir = Path.home()
        val owner = homeDir.owner()
        assertNotNull(owner)
        assertEquals(owner, Sys.getUsername())
    }
}
