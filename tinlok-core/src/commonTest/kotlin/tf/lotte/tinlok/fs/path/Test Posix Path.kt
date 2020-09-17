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
    fun `Test isAbsolute`() {
        val etc = PosixPurePath.fromString("/etc")
        assertTrue(etc.isAbsolute)

        val notEtc = PosixPurePath.fromString("./etc")
        assertFalse(notEtc.isAbsolute)
    }

    @Test
    fun `Test parents`() {
        val etc = PosixPurePath.fromString("/etc")
        val passwd = PosixPurePath.fromString("/etc/passwd")
        assertEquals(passwd.parent, etc)

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

    @Test
    fun `Test child check`() {
        val usr = PosixPurePath.fromString("/usr")
        val usrlib = PosixPurePath.fromString("/usr/lib")
        assertTrue(usrlib.isChildOf(usr))

        // can't be a child of yourselves
        assertFalse(usr.isChildOf(usr))

        // always a child of root if you're absolute and not root
        val root = PosixPurePath.fromString("/")
        assertTrue(usr.isChildOf(root))

        // relative paths
        val path1 = PosixPurePath.fromString("some/directory")
        val path2 = PosixPurePath.fromString("some/directory/child")
        assertTrue(path2.isChildOf(path1))
        // relative paths are never children of absolute paths
        assertFalse(path1.isChildOf(root))
    }

    @Test
    fun `Test reparenting`() {
        // use the docstring tests as examples!
        val usr = PosixPurePath.fromString("/usr")
        val local = PosixPurePath.fromString("/usr/local")
        val sitePackages = PosixPurePath.fromString("/usr/lib/python3.8/site-packages")
        val localSitePackages = PosixPurePath.fromString("/usr/local/lib/python3.8/site-packages")

        val newPath = sitePackages.reparent(usr, local)
        assertEquals(newPath, localSitePackages)

        assertFailsWith<IllegalArgumentException> {
            localSitePackages.reparent(usr, local)
        }
    }
}
