/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.crypto

import kotlin.random.nextUBytes
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Ensures password hashes verify properly.
 */
@OptIn(ExperimentalUnsignedTypes::class)
public class `Test password hashes` {
    @Test
    fun `Test hashes with default salt`() {
        val hash = passwordHash("test")
        assertTrue(hash.verify("test"))
        assertFalse(hash.verify("test 2"))
    }

    @Test
    fun `Test hashes with specified salt`() {
        val salt = SecureRandom.nextUBytes(16)
        val hash = passwordHash("password", salt = salt)
        assertTrue(hash.verify("password"))
    }

    @Test
    fun `Test invalid values`() {
        // empty passwords don't work
        assertFails { passwordHash("") }
        // not enough memory
        assertFails { passwordHash("abc", blocks = 0) }
        // invalid salt
        assertFails { passwordHash("def", salt = SecureRandom.nextUBytes(1)) }
    }
}
