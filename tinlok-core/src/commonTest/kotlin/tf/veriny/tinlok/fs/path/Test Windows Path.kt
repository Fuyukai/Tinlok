/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.fs.path

import kotlin.test.*

/**
 * Tests the [WindowsPurePath] class.
 */
class `Test Windows Path` {
    /**
     * Ensures various paths are absolute and not absolute.
     */
    @Test
    fun `Test isAbsolute`() {
        val sys32 = WindowsPurePath.fromString("C:\\Windows\\System32")
        assertTrue(sys32.isAbsolute)

        val notSys32 = WindowsPurePath.fromString(".\\System32")
        assertFalse(notSys32.isAbsolute)
    }

    /**
     * Ensures the name is null for a root path. (Tricky!)
     */
    @Test
    fun `Test name on root paths`() {
        val root = WindowsPurePath.fromString("C:\\")
        assertNull(root.rawName)
    }

    /**
     * Ensures parents are equal to eachother.
     */
    @Test
    fun `Test parent matches`() {
        val p1 = WindowsPurePath.fromString("C:\\Users\\ABC\\")
        val users = WindowsPurePath.fromString("C:\\Users\\")
        assertEquals(users, p1.parent)

    }

    /**
     * Ensures parenting works properly with different anchors.
     */
    @Test
    fun `Test parents with relation to anchors`() {
        val c = WindowsPurePath.fromString("C:\\")
        val d = WindowsPurePath.fromString("D:\\")
        assertNotEquals(d, c.parent)
        assertEquals(c, c.parent)
    }

    /**
     * Ensures the ``allParents`` function returns appropriately.
     */
    @Test
    fun `Test all parents extension`() {
        val longPath = WindowsPurePath.fromString(".\\one\\two\\three\\four")
        val allParents = longPath.allParents()
        println(allParents)
        assertEquals(3, allParents.size)
        assertEquals(WindowsPurePath.fromString(".\\one"), allParents[2])
    }

    /**
     * Ensures joining two paths works.
     */
    @Test
    fun `Test joining two paths`() {
        val windows = WindowsPurePath.fromString("C:\\Windows")
        val hosts = WindowsPurePath.fromString("System32\\etc\\hosts")
        val windowsHosts = WindowsPurePath.fromString("C:\\Windows\\System32\\etc\\hosts")

        val joined = windows.resolveChild(hosts)
        assertEquals(windowsHosts, joined)
    }

    /**
     * Ensures joining absolute paths returns the other one.
     */
    @Test
    fun `Test joining absolute paths`() {
        val c = WindowsPurePath.fromString("C:\\Windows")
        val d = WindowsPurePath.fromString("D:\\Users")

        assertEquals(d, c.resolveChild(d))
    }

    /**
     * Ensures replacing names works.
     */
    @Test
    fun `Test withName`() {
        val windows = WindowsPurePath.fromString("C:\\Windows")
        val users = windows.withName("Users")
        assertNotEquals(users.name, "Windows")
        assertEquals(users.name, "Users")

        // ensure withName doesn't replace /
        val root = WindowsPurePath.fromString("C:\\")
        val win = WindowsPurePath.fromString("C:\\Windows")
        val winJoined = root.withName("Windows")
        assertEquals(winJoined, win)
    }

    /**
     * Ensures paths are the children of other paths correctly.
     */
    @Test
    fun `Test child check`() {
        val windows = WindowsPurePath.fromString("C:\\Windows")
        val sys32 = WindowsPurePath.fromString("C:\\Windows\\System32")
        assertTrue(sys32.isChildOf(windows))

        // can't be a child of yourselves
        assertFalse(windows.isChildOf(windows))

        // a child of your drive letter
        val drive = WindowsPurePath.fromString("C:\\")
        assertTrue(windows.isChildOf(drive))
        // ... but not of a ddifferent one
        val otherDrive = WindowsPurePath.fromString("D:\\")
        assertFalse(windows.isChildOf(otherDrive))
    }

    /**
     * Ensures parsing volumes works correctly.
     */
    @Test
    fun `Test volume parsing`() {
        val path = WindowsPurePath.fromString("\\\\SERVER\\share\\path")
        assertTrue(path.isAbsolute)
        assertEquals("\\\\SERVER\\share\\", path.anchor)
        assertEquals("path", path.name)
    }

    /**
     * Ensures \\\\?\\ paths get parsed correctly.
     */
    @Test
    fun `Test long path parsing`() {
        val path = WindowsPurePath.fromString("C:\\Windows")
        val path2 = WindowsPurePath.fromString("\\\\?\\C:\\Windows")
        assertEquals(path.anchor, path2.anchor)
        assertEquals(path.components.size, path2.components.size)
    }
}
