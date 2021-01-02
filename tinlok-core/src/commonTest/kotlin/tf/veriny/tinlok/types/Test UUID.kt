/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.types

import tf.veriny.tinlok.util.UUID
import tf.veriny.tinlok.util.toByteString
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests the UUID class.
 */
@OptIn(ExperimentalUnsignedTypes::class)
public class `Test UUID` {
    private val UUID_BYTES = arrayOf(
        197, 94, 201, 255, 202, 224, 64, 110, 176, 230, 3, 120, 241, 1, 40, 95
    ).map { it.toByte() }.toByteArray().toByteString()

    @Test
    fun `Test V1 UUID creation`() {
        val uuid = UUID.uuid1()
        assertEquals(uuid.version, UUID.Version.VERSION_ONE)
    }

    @Test
    fun `Test V4 UUID creation`() {
        val uuid = UUID.uuid4()
        assertEquals(uuid.version, UUID.Version.VERSION_FOUR)
    }

    /**
     * Ensures a fixed UUID has the correct string representation.
     */
    @Test
    fun `Test UUID toString`() {
        val hexString = "c55ec9ff-cae0-406e-b0e6-0378f101285f"
        val uuid = UUID(UUID_BYTES)
        assertEquals(hexString, uuid.toString())
    }

    @Test
    fun `Test UUID fromString`() {
        val hexString = "c55ec9ff-cae0-406e-b0e6-0378f101285f"
        val uuid = UUID.fromString(hexString)
        assertEquals(UUID_BYTES, uuid.bytes)
    }
}
