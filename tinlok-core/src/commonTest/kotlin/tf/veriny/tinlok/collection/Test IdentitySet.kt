/*
 * Copyright (C) 2020-2022 Lura Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.collection

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests the IdentitySet class.
 */
public class `Test IdentitySet` {
    private class IdentityObject(val value: Int) {
        override fun equals(other: Any?): Boolean {
            return (other is IdentityObject) && other.value == value
        }

        override fun hashCode(): Int {
            return value.hashCode()
        }
    }

    /**
     * Tests the behaviour of the identity set when the same two objects are provided.
     */
    @Test
    public fun `Test identity set with same object`() {
        val set = IdentitySet<IdentityObject>()
        val item = IdentityObject(1)
        set.add(item)
        assertEquals(1, set.size)
        set.add(item)
        assertEquals(1, set.size)
    }

    @Test
    public fun `Test identity set with different objects`() {
        val set = IdentitySet<IdentityObject>()
        val one = IdentityObject(1)
        val two = IdentityObject(1)

        assertEquals(two, one)
        set.add(one)
        assertEquals(1, set.size)
        set.add(two)
        assertEquals(2, set.size)
    }
}
