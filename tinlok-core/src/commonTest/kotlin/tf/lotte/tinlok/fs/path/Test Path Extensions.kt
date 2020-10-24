/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.fs.path

import tf.lotte.cc.exc.IsADirectoryException
import tf.lotte.cc.exc.OSException
import kotlin.test.*

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
    fun `Test recursive delete`() = Path.makeTempDirectory("Tinlok-test-") {
        val parent = it.resolveChild("delete-parent")
        parent.createDirectory(parents = false, existOk = false)
        parent.resolveChild("first").apply {
            createDirectory(parents = false, existOk = false)
            resolveChild("nested").createDirectory(parents = false, existOk = false)
        }
        parent.resolveChild("second").createDirectory(parents = false, existOk = false)

        assertFailsWith<OSException> { parent.removeDirectory() }

        parent.recursiveDelete()
        assertFalse(parent.exists())
    }

    /**
     * Tests recursively copying a directory.
     */
    @Test
    fun `Test recursive copy`() = Path.makeTempDirectory("Tinlok-test-") {
        val parent = it.resolveChild("parent")
        parent.createDirectory()
        parent.resolveChild("child").apply {
            createDirectory(parents = false, existOk = false)
            val p = resolveChild("one.txt").also { one ->
                one.writeString("one!")
            }
            resolveChild("two.txt").symlinkTo(p.toAbsolutePath())
        }

        val copyTo = it.resolveChild("parent2")
        parent.recursiveCopy(copyTo)

        assertTrue(copyTo.exists())
        val copyChild = copyTo.resolveChild("child")
        assertTrue(copyChild.exists())
        val one = copyChild.resolveChild("one.txt")
        assertEquals(one.readAllText(), "one!")
        val two = copyChild.resolveChild("two.txt")
        assertTrue(two.isLink())
        assertEquals(two.readAllText(), "one!")
    }

    @Test
    fun `Test writeAll`() = Path.makeTempDirectory("Tinlok-test-") {
        // ensure atomic writes work properly
        val fileA = it.resolveChild("fileA")
        fileA.writeString("test!", atomic = true)
        assertTrue(fileA.exists())
        val contentA = fileA.readAllText()
        assertEquals(contentA, "test!")

        // ensure attempting to write over a directory throws the right error
        val fileB = it.resolveChild("fileB")
        fileB.createDirectory(parents = false, existOk = false)

        // separate tests for atomic true and false
        assertFailsWith<IsADirectoryException> {
            fileB.writeString("failure!", atomic = false)
        }
        assertFailsWith<IsADirectoryException> {
            fileB.writeString("failure!", atomic = true)
        }

        Unit
    }
}
