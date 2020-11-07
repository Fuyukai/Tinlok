/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.fs

import tf.lotte.cc.io.peek
import tf.lotte.cc.io.readUpTo
import tf.lotte.cc.io.writeAll
import tf.lotte.cc.types.b
import tf.lotte.cc.types.substring
import tf.lotte.tinlok.fs.path.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Tests the [SynchronousFile] class.
 */
class `Test FilesystemFile` {
    /**
     * Tests basic WRITE mode reading and writing.
     */
    @Test
    fun `Test basic read/write`() = Path.makeTempDirectory("Tinlok-test-") {
        val path = it.resolveChild("test.txt")
        val toWrite = b("abcdefghi")

        path.open(StandardOpenModes.WRITE, StandardOpenModes.CREATE) { file ->
            file.writeAll(toWrite)
            file.close()
        }

        run {
            val data = path.open(StandardOpenModes.READ) { file ->
                file.readUpTo(1024)
            }

            assertNotNull(data)
            assertEquals(toWrite, data)
        }

        run {
            val data = path.open(StandardOpenModes.READ) { file ->
                file.readAll()
            }

            assertEquals(toWrite, data)
        }
    }

    @Test
    fun `Test append`() = Path.makeTempDirectory("Tinlok-test-") {
        val path = it.resolveChild("test.xt")

        val part1 = b("When the incident took place, ")
        val part2 = b("the victim was alone at his table, sir.")
        val combined = part1 + part2

        // write first half
        path.open(StandardOpenModes.WRITE, StandardOpenModes.CREATE_NEW) { file ->
            file.writeAll(part1)
        }

        path.open(StandardOpenModes.APPEND) { file ->
            file.writeAll(part2)
        }

        // now read in
        val data = path.open(StandardOpenModes.READ) { file ->
            file.readUpTo(1024)
        }

        assertNotNull(data)
        assertEquals(data, combined)
    }

    @Test
    fun `Test seek`() = Path.makeTempDirectory("Tinlok-test-") {
        val path = it.resolveChild("test.txt")
        val toWrite = b("Every word is getting longer, the mosquitoes are getting louder.")

        val part1 = toWrite.substring(0, 29)
        val part2 = toWrite.substring(29)

        path.writeBytes(toWrite)

        path.open(StandardOpenModes.READ) { file ->
            file.seekAbsolute(29L)
            val data1 = file.readAll()
            assertEquals(data1, part2)

            file.seekAbsolute(0L)
            val data2 = file.readUpTo(29)
            assertNotNull(data2)
            assertEquals(data2, part1)
        }

        path.open(StandardOpenModes.READ) { file ->
            val data = file.peek(toWrite.size)
            assertNotNull(data)
            assertEquals(data, toWrite)
            assertEquals(file.cursor(), 0L)
        }
    }

}
