/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.fs.path

import tf.veriny.tinlok.Sys
import kotlin.test.*

/**
 * Tests the special path functions.
 */
@Ignore
class `Test special Paths`() {
    @Test
    public fun `Test home`() {
        val homeDir = Path.home()
        val owner = homeDir.owner()
        assertNotNull(owner)
        assertEquals(owner, Sys.getUsername())
    }
}
