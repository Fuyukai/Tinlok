/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.knste.fs.path

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests various extensions to [PurePath] and [Path].
 */
class `Test Path Extensions` {
    /**
     * Tests the suffix extensions for paths.
     */
    @Test
    fun `Test suffix functions`() {
        val path = Paths.purePath("file.tar.gz")
        assertEquals(path.suffix, "gz")
        assertEquals(path.suffixes, listOf("tar", "gz"))
    }
}
