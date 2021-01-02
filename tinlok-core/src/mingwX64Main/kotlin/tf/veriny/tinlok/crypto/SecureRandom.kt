/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.crypto

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.windows.BCRYPT_USE_SYSTEM_PREFERRED_RNG
import platform.windows.BCryptGenRandom

/**
 * Implements a cryptographically secure random generator.
 */
public actual object SecureRandom : RandomShared() {
    override fun readBytesImpl(buf: ByteArray) {
        val res = buf.usePinned {
            BCryptGenRandom(
                null,
                it.addressOf(0).reinterpret(),
                buf.size.toUInt(),
                BCRYPT_USE_SYSTEM_PREFERRED_RNG
            )
        }

        if (res != 0 /* STATUS_SUCCESS */) {
            throw Error("BCryptGenRandom failed")
        }
    }
}
