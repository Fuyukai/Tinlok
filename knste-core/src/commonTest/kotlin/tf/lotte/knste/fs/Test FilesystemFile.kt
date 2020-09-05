/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.knste.fs

import tf.lotte.knste.b
import tf.lotte.knste.fs.path.Paths
import tf.lotte.knste.fs.path.makeTempDirectory
import tf.lotte.knste.fs.path.open
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Tests the [FilesystemFile] class.
 */
class `Test FilesystemFile` {
    /**
     * Tests basic WRITE mode reading and writing.
     */
    @Test
    fun `Test basic read/write`() = Paths.makeTempDirectory("knste-test-") {
        val path = it.join("test.txt")
        val toWrite = b("abcdefghi")

        path.open(StandardOpenModes.WRITE, StandardOpenModes.CREATE) { file ->
            file.writeAll(toWrite)
            file.close()
        }

        val data = path.open(StandardOpenModes.READ) { file ->
            file.readUpTo(1024)
        }

        assertNotNull(data)
        assertEquals(data, toWrite)
    }

    @Test
    fun `Test append`() = Paths.makeTempDirectory("knste-test-") {
        val path = it.join("test.xt")

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

}