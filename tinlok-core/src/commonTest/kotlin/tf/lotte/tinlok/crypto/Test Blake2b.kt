/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.crypto

import tf.lotte.cc.types.b
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Ensures that the blake2b functions
 */
public class `Test Blake2b` {
    companion object {
        const val EMPTY = "786a02f742015903c6c6fd852552d272912f4740e15847618a86e217f71f5419d25e1031afee585313896444934eb04b903a685b1448b755d56f701afe9be2ce"
        const val FOX = "a8add4bdddfd93e4877d2746e62817b116364a1fa7bc148d95090bc7333b3673f82401cf7aa2e4cb1ecd90296e3f14cb5413f8ed77be73045b13914cdcd6a918"
        const val DOF = "ab6b007747d8068c02e25a6008db8a77c218d94f3b40d2291a7dc8a62090a744c082ea27af01521a102e42f480a31e9844053f456b4b41e8aa78bbe5c12957bb"
    }

    @Test
    fun `Test known hash values`() {
        // These are some of the wikipedia example digests.
        val empty = Blake2b {
            it.feed(b(""))
            it.hash()
        }
        assertEquals(EMPTY, empty.hexdigest())

        val dog = Blake2b {
            it.feed(b("The quick brown fox jumps over the lazy dog"))
            it.hash()
        }
        assertEquals(FOX, dog.hexdigest())

        val dof = Blake2b {
            it.feed(b("The quick brown fox jumps over the lazy dof"))
            it.hash()
        }
        assertEquals(DOF, dof.hexdigest())
    }
}
