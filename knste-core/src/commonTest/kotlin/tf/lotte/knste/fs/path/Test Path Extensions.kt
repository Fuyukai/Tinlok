/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.knste.fs.path

import tf.lotte.knste.exc.OSException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

/**
 * Tests various extensions to [PurePath] and [Path].
 */
class `Test Path Extensions` {
    /**
     * Tests the suffix extensions for paths.
     */
    @Test
    fun `Test suffix functions`() {
        val path = PurePath.native("file.tar.gz")
        assertEquals(path.suffix, "gz")
        assertEquals(path.suffixes, listOf("tar", "gz"))
    }

    /**
     * Tests the recursive delete extension.
     */
    @Test
    fun `Test recursive delete`() = Path.makeTempDirectory("knste-test-") {
        val parent = it.join("delete-parent")
        parent.createDirectory(parents = false, existOk = false)
        parent.join("first").apply {
            createDirectory(parents = false, existOk = false)
            join("nested").createDirectory(parents = false, existOk = false)
        }
        parent.join("second").createDirectory(parents = false, existOk = false)

        assertFailsWith<OSException> { parent.removeDirectory() }

        parent.recursiveDelete()
        assertFalse(parent.exists())
    }
}
