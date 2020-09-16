/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.fs.path

import kotlin.test.*

/**
 * Tests the [PosixPurePath] class.
 */
class `Test Posix Path` {
    @Test
    fun `Test parents`() {
        val etc = PosixPurePath.fromString("/etc")
        assertTrue(etc.isAbsolute)

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

        assertEquals(etc.resolveChild(usr), usr)

        val systemd = PosixPurePath.fromString("systemd/system")
        val etcSystemd = PosixPurePath.fromString("/etc/systemd/system")

        val joinedSystemd = etc / systemd
        assertTrue(joinedSystemd.isAbsolute)
        assertEquals(joinedSystemd, etcSystemd)
    }

    @Test
    fun `Test withName`() {
        val etc = PosixPurePath.fromString("/etc/passwd")
        val shadow = etc.withName("shadow")

        assertNotEquals(shadow.name, "passwd")
        assertEquals(shadow.name, "shadow")

        assertFailsWith<IllegalArgumentException> {
            etc.withName("fre/nda")
        }
    }
}
