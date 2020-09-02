/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KSTE.
 *
 * KSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.kste.fs.path

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.expect

/**
 * Tests the [PosixPurePath] class.
 */
class `Test Posix Path` {
    @Test
    fun `Test parents`() {
        val etc = PosixPurePath.fromString("/etc")
        val path = PosixPurePath.fromString("/etc/passwd")

        assertEquals(path.parent, etc)

        val root = PosixPurePath.fromString("/")
        assertEquals(root.parent, root)

        val longPath = PosixPurePath.fromString("/etc/abc/def/ghi")
        val allParents = longPath.allParents()
        assertEquals(allParents.size, 3)
        assertEquals(allParents[2], PosixPurePath.fromString("/etc"))
    }

    @Test
    fun `Test join`() {
        val etc = PosixPurePath.fromString("/etc")
        val usr = PosixPurePath.fromString("/usr")

        assertEquals(etc.join(usr), usr)

        val systemd = PosixPurePath.fromString("systemd/system")
        val etcSystemd = PosixPurePath.fromString("/etc/systemd/system")

        val joinedSystemd = etc / systemd
        assertTrue(joinedSystemd.isAbsolute)
        assertEquals(joinedSystemd, etcSystemd)
    }
}
