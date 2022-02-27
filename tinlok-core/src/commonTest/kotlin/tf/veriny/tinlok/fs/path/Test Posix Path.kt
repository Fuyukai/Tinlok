/*
 * Copyright (C) 2020-2022 Lura Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.fs.path

import kotlin.test.*

/**
 * Tests the [PosixPurePath] class.
 */
class `Test Posix Path` {
    /**
     * Ensures various paths are absolute and not absolute.
     */
    @Test
    fun `Test isAbsolute`() {
        val etc = PosixPurePath.fromString("/etc")
        assertTrue(etc.isAbsolute)

        val notEtc = PosixPurePath.fromString("./etc")
        assertFalse(notEtc.isAbsolute)
    }

    /**
     * Ensures the name is null for a root path. (Tricky!)
     */
    @Test
    fun `Test name on root paths`() {
        val root = PosixPurePath.fromString("/")
        assertNull(root.rawName)
    }

    /**
     * Ensures parents are equal to eachother.
     */
    @Test
    fun `Test parent matches`() {
        val etc = PosixPurePath.fromString("/etc")
        val passwd = PosixPurePath.fromString("/etc/passwd")
        assertEquals(passwd.parent, etc)

        val root = PosixPurePath.fromString("/")
        assertEquals(root.parent, root)
    }

    /**
     * Ensures the ``allParents`` function returns appropriately.
     */
    @Test
    fun `Test all parents extension`() {
        val longPath = PosixPurePath.fromString("/etc/abc/def/ghi")
        val allParents = longPath.allParents()
        assertEquals(allParents.size, 3)
        assertEquals(allParents[2], PosixPurePath.fromString("/etc"))
    }

    /**
     * Ensures joining two paths works.
     */
    @Test
    fun `Test joining two paths`() {
        val etc = PosixPurePath.fromString("/etc")
        val systemd = PosixPurePath.fromString("systemd/system")
        val etcSystemd = PosixPurePath.fromString("/etc/systemd/system")

        val joinedSystemd = etc / systemd
        assertTrue(joinedSystemd.isAbsolute)
        assertEquals(joinedSystemd, etcSystemd)
    }

    /**
     * Ensures joining absolute paths returns the other one.
     */
    @Test
    fun `Test joining absolute paths`() {
        val etc = PosixPurePath.fromString("/etc")
        val usr = PosixPurePath.fromString("/usr")

        assertEquals(etc.resolveChild(usr), usr)
    }

    /**
     * Ensures replacing names works.
     */
    @Test
    fun `Test withName`() {
        val etc = PosixPurePath.fromString("/etc/passwd")
        val shadow = etc.withName("shadow")

        assertNotEquals(shadow.name, "passwd")
        assertEquals(shadow.name, "shadow")

        assertFailsWith<IllegalArgumentException> {
            etc.withName("fre/nda")
        }

        // ensure withName doesn't replace /
        val root = PosixPurePath.fromString("/")
        val usr = PosixPurePath.fromString("/usr")
        val usrJoined = root.withName("usr")
        assertEquals(usr, usrJoined)
    }

    /**
     * Ensures paths are the children of other paths correctly.
     */
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

    /**
     * Ensures that changing the parent of a path works.
     */
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
