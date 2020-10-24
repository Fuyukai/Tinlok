/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.system

import kotlinx.cinterop.*
import platform.posix.memcpy
import platform.windows.GetEnvironmentVariableW
import platform.windows.GetUserNameW
import platform.windows.HRESULT
import tf.lotte.cc.Unsafe
import tf.lotte.tinlok.util.utf16ToString

public actual object Syscall {
    /** Corresponds to the windows SUCCEEDED macro. */
    public fun SUCCEEDED(result: HRESULT): Boolean {
        return (result >= 0)
    }

    /**
     * Copies [size] bytes from the pointer at [pointer] to [buf].
     *
     * This uses memcpy() which is faster than the naiive Kotlin method. This will buffer
     * overflow if [pointer] is smaller than size!
     */
    @Unsafe
    public actual fun __fast_ptr_to_bytearray(pointer: COpaquePointer, buf: ByteArray, size: Int) {
        assert(buf.size <= size) { "Size is too big!" }

        buf.usePinned {
            memcpy(it.addressOf(0), pointer, size.toULong())
        }
    }

    @Unsafe
    public fun FormatMessageW(code: Int): String {
        TODO()
    }

    /**
     * Gets the current username.
     */
    @OptIn(ExperimentalUnsignedTypes::class)
    @Unsafe
    public fun GetUserName(): String = memScoped {
        val buf = UShortArray(256)
        val sizePtr = this.alloc<UIntVar>()
        sizePtr.value = buf.size.toUInt()

        val result = buf.usePinned {
            val ptr = it.addressOf(0)
            GetUserNameW(ptr, sizePtr.ptr)
        }

        if (!SUCCEEDED(result)) {

        }

        val count = sizePtr.value.toInt()

        return buf.utf16ToString(count)
    }

    /**
     * Gets an environment variable with the specified [name].
     */
    @OptIn(ExperimentalUnsignedTypes::class)
    @Unsafe
    public fun GetEnvironmentVariable(name: String): String? {
        // i dont care about any of this nonsense with big return values
        // im just allocating this array as 32k
        val buf = UShortArray(16383)
        val count = buf.usePinned {
            val ptr = it.addressOf(0)
            val res = GetEnvironmentVariableW(name, ptr, buf.size.toUInt())
            if (res > buf.size.toUInt()) {
                error("Environment variable is bigger than the maximum allowed")
            } else {
                res.toInt()
            }
        }
        return buf.utf16ToString(count)
    }
}
