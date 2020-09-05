/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

@file:Suppress("MemberVisibilityCanBePrivate")

package tf.lotte.knste.fs.path

import tf.lotte.knste.Sys
import tf.lotte.knste.exc.OSException
import tf.lotte.knste.util.Unsafe
import kotlin.test.*

/**
 * Tests path modification operators.
 */
@OptIn(Unsafe::class)
class `Test Path Operations` {
    /**
     * Tests creating directories.
     */
    @Test
    fun `Test mkdir`() = Paths.makeTempDirectory("knste-test-") {
        val newPath = it.join("mkdir-test")
        newPath.createDirectory(parents = false, existOk = false)

        assertTrue(newPath.exists())
        assertTrue(newPath.isDirectory())
    }

    @Test
    fun `Test owner`() = Paths.makeTempDirectory("knste-test-") {
        val username = Sys.getUsername()
        assertEquals(it.owner(), username)
    }

    /**
     * Tests the recursive delete extension.
     */
    @Test
    fun `Test recursive delete`() = Paths.makeTempDirectory("knste-test-") {
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
