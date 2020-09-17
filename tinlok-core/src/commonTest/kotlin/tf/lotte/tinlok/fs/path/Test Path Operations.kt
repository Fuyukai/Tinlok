/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

@file:Suppress("MemberVisibilityCanBePrivate")

package tf.lotte.tinlok.fs.path

import tf.lotte.tinlok.Sys
import tf.lotte.tinlok.b
import tf.lotte.tinlok.fs.StandardOpenModes
import tf.lotte.tinlok.util.Unsafe
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests path modification operators.
 */
class `Test Path Operations` {
    /**
     * "Touch"es (creates empty) a file.
     */
    private fun Path.touch() {
        open(StandardOpenModes.CREATE, StandardOpenModes.WRITE) {}
    }

    @Test
    fun `Test mkdir`() = Path.makeTempDirectory("Tinlok-test-") {
        val newPath = it.resolveChild("mkdir-test")
        newPath.createDirectory(parents = false, existOk = false)

        assertTrue(newPath.exists())
        assertTrue(newPath.isDirectory())
    }

    @Test
    fun `Test owner`() = Path.makeTempDirectory("Tinlok-test-") {
        val username = Sys.getUsername()
        assertEquals(it.owner(), username)
    }


    @Test
    fun `Test stat`() = Path.makeTempDirectory("Tinlok-test-") {
        assertTrue(it.isDirectory())
        assertFalse(it.isRegularFile())

        run {
            val file = it.resolveChild("file")
            assertFalse(file.isRegularFile())

            file.touch()
            assertTrue(file.exists())
            assertTrue(file.isRegularFile())
            assertEquals(file.size(), 0L)

            file.unlink()
            assertFalse(file.isRegularFile())
        }

        run {
            val file = it.resolveChild("file2")

            file.writeString("abcdef")
            assertTrue(file.exists())
            assertEquals(file.size(), 6)
        }
    }

    @OptIn(Unsafe::class)
    @Test
    fun `Test rename`() = Path.makeTempDirectory("Tinlok-test-") {
        val first = it.resolveChild("test1.txt")
        val second = it.resolveChild("test2.txt")

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

    @Test
    fun `Test copy`() = Path.makeTempDirectory("Tinlok-test-") {
        val first = it.resolveChild("test1.txt")
        val second = it.resolveChild("test2.txt")

        val toWrite = "happy cirno day 9/9/2020"
        first.writeString(toWrite)
        first.copy(second)

        assertTrue(first.exists())
        assertTrue(second.exists())
        assertEquals(first.readAllString(), toWrite)
    }

    @Test
    fun `Test symlink`() = Path.makeTempDirectory("Tinlok-test-") {
        val first = it.resolveChild("test1.txt")
        // continuation indents are possiibly the dumbest form of kotlin formatting
        // big personal fuck you to whoever made those part of the formatting

        val toWrite = b("kandi boy raver he's the one for me and when the " +
            "music starts to play we'll be dancing to the beat"
        )

        first.writeBytes(toWrite, atomic = false)

        val second = it.resolveChild("test2.txt")
        second.symlinkTo(first)
        assertTrue(second.exists())
        assertTrue(second.isLink())
        assertEquals(second.toAbsolutePath(strict = true), first.toAbsolutePath(strict = true))

        val read = second.readAllBytes()
        assertEquals(read, toWrite)
    }

}
