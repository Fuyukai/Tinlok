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
import platform.windows.*
import tf.lotte.cc.Unsafe
import tf.lotte.cc.exc.*
import tf.lotte.tinlok.exc.WindowsException
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

    /**
     * Formats the message for an error code.
     */
    @OptIn(ExperimentalUnsignedTypes::class)
    @Unsafe
    public fun FormatMessage(code: Int): String {
        // NB: we manually free because windows allocates it and I don't think K/N will
        // deallocate it properly. I might be wrong about that, but this code is correct no
        // matter what.
        val flags = FORMAT_MESSAGE_ALLOCATE_BUFFER
            .or(FORMAT_MESSAGE_FROM_SYSTEM)
            .or(FORMAT_MESSAGE_IGNORE_INSERTS).toUInt()

        // scary!
        val ptr = nativeHeap.alloc<LPWSTRVar>()

        val result = try {
            FormatMessageW(
                flags, null, code.toUInt(),
                0,  // default language according to MSDN
                ptr.ptr.reinterpret(), 0, null
            )
        } catch (e: Throwable) {
            // just in case toUInt/reinterpret throws... (maybe in the future?)
            HeapFree(GetProcessHeap(), 0, ptr.ptr)
            throw e
        }

        if (result == 0U) {
            HeapFree(GetProcessHeap(), 0, ptr.ptr)
            throw Error("FormatMessageW failed!")
        } else {
            val data = ptr.value?.toKStringFromUtf16() ?: "Unknown error"
            HeapFree(GetProcessHeap(), 0, ptr.ptr)
            return data
        }
    }

    /**
     * Throws the appropriate error based on the errno.
     */
    @OptIn(ExperimentalUnsignedTypes::class)
    @Unsafe
    public fun throwErrno(): Nothing {
        throwErrno(GetLastError().toInt())
    }

    /**
     * Throws an error based on the error code provided.
     */
    @Unsafe
    public fun throwErrno(code: Int): Nothing {
        throw when (code) {
            ERROR_ACCESS_DENIED -> AccessDeniedException()
            ERROR_BROKEN_PIPE -> BrokenPipeException()

            // winsock
            WSAETIMEDOUT -> TimeoutException()
            WSAECONNABORTED -> ConnectionAbortedException()
            WSAECONNREFUSED -> ConnectionRefusedException()
            WSAECONNRESET -> ConnectionResetException()
            WSAESHUTDOWN -> SocketShutdownException()
            WSAENETUNREACH -> NetworkUnreachableException()

            else -> WindowsException(winerror = code, message = FormatMessage(code))
        }
    }

    /**
     * Throws an error based on the error code provided. Special function for path-related
     * exception.
     */
    @Unsafe
    public fun throwErrnoPath(code: Int, path: String): Nothing {
        throw when (code) {
            ERROR_FILE_NOT_FOUND, ERROR_PATH_NOT_FOUND -> FileNotFoundException(path)
            ERROR_FILE_EXISTS -> FileAlreadyExistsException(path)

            ERROR_INVALID_NAME -> IllegalArgumentException("Invalid name: $path")

            else -> throwErrno(code)
        }
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
            throwErrno()
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

        // error condition?
        if (count < buf.size) {
            val err = GetLastError().toInt()
            if (err == ERROR_ENVVAR_NOT_FOUND) return null
            else throwErrno(err)
        }

        return buf.utf16ToString(count)
    }
}
