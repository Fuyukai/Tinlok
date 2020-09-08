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
import tf.lotte.knste.fs.StandardOpenModes
import tf.lotte.knste.util.Unsafe
import kotlin.test.*

/**
 * Tests path modification operators.
 */
@OptIn(Unsafe::class)
class `Test Path Operations` {
    /**
     * "Touch"es (creates empty) a file.
     */
    private fun Path.touch() {
        open(StandardOpenModes.CREATE, StandardOpenModes.WRITE) {}
    }

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

    /**
     * Tests the owner() function.
     */
    @Test
    fun `Test owner`() = Paths.makeTempDirectory("knste-test-") {
        val username = Sys.getUsername()
        assertEquals(it.owner(), username)
    }


    /**
     * Tests getting certain stat() info about a file.
     */
    @Test
    fun `Test stat`() = Paths.makeTempDirectory("knste-test-") {
        assertTrue(it.isDirectory())
        assertFalse(it.isRegularFile())

        run {
            val file = it.join("file")
            assertFalse(file.isRegularFile())

            file.touch()
            assertTrue(file.exists())
            assertTrue(file.isRegularFile())
            assertEquals(file.size(), 0L)

            file.unlink()
            assertFalse(file.isRegularFile())
        }

        run {
            val file = it.join("file2")

            file.writeString("abcdef")
            assertTrue(file.exists())
            assertEquals(file.size(), 6)
        }
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

    /**
     * Tests renaming.
     */
    @Unsafe
    @Test
    fun `Test rename`() = Paths.makeTempDirectory("knste-test-") {
        val first = it.join("test1.txt")
        val second = it.join("test2.txt")

        first.touch()
        first.rename(second)

        assertFalse(first.exists())
        assertTrue(second.exists())

        second.delete()
        first.createDirectory(parents = false, existOk = false)
        first.rename(second)

        assertFalse(first.exists())
        assertTrue(second.isDirectory(followSymlinks = false))
    }

}
