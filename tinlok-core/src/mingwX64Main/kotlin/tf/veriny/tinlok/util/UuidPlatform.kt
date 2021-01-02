/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.util

import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import platform.posix.GUID
import platform.windows.*
import tf.veriny.tinlok.Unsafe

/**
 * Generates a Version 1 (MAC + Time) UUID.
 */
@OptIn(ExperimentalUnsignedTypes::class, Unsafe::class)
public actual fun uuidGenerateV1(): UByteArray {
    val buf = memScoped {
        val guid = alloc<GUID>()
        val res = UuidCreateSequential(guid.ptr)
        if (res == RPC_S_UUID_NO_ADDRESS) {
            throw UnsupportedOperationException(
                "Cannot get Ethernet or token-ring hardware address for this computer."
            )
        }

        // evil misuse of htonl
        guid.Data1 = htonl(guid.Data1)
        guid.Data2 = htons(guid.Data2)
        guid.Data3 = htons(guid.Data3)

        guid.ptr.readBytesFast(16)
    }

    return buf.toUByteArray()
}

/**
 * Generates a Version 4 (psuedorandom) UUID.
 */
@OptIn(ExperimentalUnsignedTypes::class, Unsafe::class)
public actual fun uuidGenerateV4(): UByteArray {
    val buf = memScoped {
        val guid = alloc<GUID>()
        val res = UuidCreate(guid.ptr)
        if (res != RPC_S_OK) {
            error("UuidCreate returned $res (not RPC_S_OK)")
        }

        // evil misuse of htonl
        guid.Data1 = htonl(guid.Data1)
        guid.Data2 = htons(guid.Data2)
        guid.Data3 = htons(guid.Data3)

        guid.ptr.readBytesFast(16)
    }

    return buf.toUByteArray()
}
