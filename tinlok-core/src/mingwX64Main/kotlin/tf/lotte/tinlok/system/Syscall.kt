/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.system

import ddk._K_REPARSE_DATA_BUFFER
import kotlinx.cinterop.*
import platform.posix.WSAData
import platform.posix.memcpy
import platform.windows.*
import tf.lotte.tinlok.Unsafe
import tf.lotte.tinlok.exc.WindowsException
import tf.lotte.tinlok.fs.DirEntry
import tf.lotte.tinlok.fs.FileType
import tf.lotte.tinlok.fs.path.resolveChild
import tf.lotte.tinlok.io.*
import tf.lotte.tinlok.util.flagged
import tf.lotte.tinlok.util.flags
import tf.lotte.tinlok.util.utf16ToString

/**
 * Platform call container object for all Win32 calls.
 */
@OptIn(ExperimentalUnsignedTypes::class)
public actual object Syscall {
    private val _WSADATA = nativeHeap.allocPointerTo<WSAData>()

    /** The winsock data information. */
    public val WSADATA: WSAData get() = _WSADATA.pointed!!

    init {
        val res = WSAStartup(514u /* (2 << 8) | 2 */, _WSADATA.value)
        if (res != 0) {
            throw Error("Winsock failed to initialise")
        }
    }

    // == Macros and helpers == //
    /**
     * Converts a FILETIME struct into a single ULong.
     */
    @OptIn(ExperimentalUnsignedTypes::class)
    public fun FILETIME.toULong(): ULong {
        return (dwHighDateTime.toULong().shl(32)).or(dwLowDateTime.toULong())
    }

    /**
     * Closes the specified [handle].
     */
    @Unsafe
    public fun CloseHandle(handle: HANDLE) {
        val res = platform.windows.CloseHandle(handle)
        if (res != TRUE) {
            throwErrno()
        }
    }

    /**
     * Closes all of the specified handles.
     */
    @Unsafe
    public fun __closeall(vararg handles: HANDLE) {
        var lastErrno = 0
        var errored = false
        for (handle in handles) {
            val res = platform.windows.CloseHandle(handle)
            if (res != TRUE) {
                errored = true
                lastErrno = GetLastError().toInt()
            }
        }

        if (errored) {
            throwErrno(lastErrno)
        }
    }

    // == Syscall definitions == //
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

    // == Error handling == //
    /**
     * Formats the message for an error code.
     */
    @OptIn(ExperimentalUnsignedTypes::class)
    @Unsafe
    public fun FormatMessage(code: Int): String = memScoped {
        // NB: we manually free because windows allocates it and I don't think K/N will
        // deallocate it properly. I might be wrong about that, but this code is correct no
        // matter what.
        val flags = FORMAT_MESSAGE_ALLOCATE_BUFFER
            .or(FORMAT_MESSAGE_FROM_SYSTEM)
            .or(FORMAT_MESSAGE_IGNORE_INSERTS).toUInt()

        // scary!
        val ptr = alloc<LPWSTRVar>()

        val result = FormatMessageW(
            flags, null, code.toUInt(),
            0,  // default language according to MSDN
            ptr.ptr.reinterpret(), 0, null
        )

        if (result == 0U) {
            throw Error("FormatMessageW failed!")
        } else {
            return ptr.value?.toKStringFromUtf16() ?: "Unknown error"
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

            else -> WindowsException(
                winerror = code,
                message = "[WinError ${code}] ${FormatMessage(code)}"
            )
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
            ERROR_DIR_NOT_EMPTY -> DirectoryNotEmptyException(path)

            ERROR_INVALID_NAME -> IllegalArgumentException("Invalid name: $path")

            else -> throwErrno(code)
        }
    }

    // == Filesystem == //
    /**
     * Checks if the specified path exists.
     */
    @Unsafe
    public fun PathFileExists(path: String): Boolean {
        val exists = PathFileExistsW(path)
        return exists == TRUE
    }

    /**
     * Gets the file size of the file at [handle].
     */
    @Unsafe
    public fun GetFileSize(handle: HANDLE): Long = memScoped {
        val value = alloc<LARGE_INTEGER>()
        val res = GetFileSizeEx(handle, value.ptr)

        if (res != TRUE) {
            throwErrno()
        }

        return value.QuadPart
    }

    /**
     * Checks if a file is a symbolic link.
     *
     * The specified file MUST have the reparse point flag set.
     */
    @Unsafe
    public fun __is_symlink(path: String): Boolean = memScoped {
        // open symlink directly otherwise deviceiocontrol gets pissy
        val handle = CreateFileW(
            path,
            GENERIC_READ,
            FILE_SHARE_VALID_FLAGS,
            null,
            OPEN_EXISTING,
            flags(FILE_FLAG_BACKUP_SEMANTICS, FILE_FLAG_OPEN_REPARSE_POINT).toUInt(),
            null,
        )
        if (handle == null || handle == INVALID_HANDLE_VALUE) {
            val err = GetLastError().toInt()
            throwErrnoPath(err, path)
        }
        defer { CloseHandle(handle) }

        // copied from some online article...
        // nb: with windows extending the path limitation i'm not sure how this will fare.
        // but blame MS if this clobbers all your memory!
        val size = 16 * 1024
        val buffer = alloc(size, 1).reinterpret<_K_REPARSE_DATA_BUFFER>()

        val bytesReturned = alloc<UIntVar>()

        val res = DeviceIoControl(
            handle,                                 // symlink file
            FSCTL_GET_REPARSE_POINT,                // get reparse details
            null, 0,            // empty input buffer
            buffer.reinterpret(), size.toUInt(),    // output REPARSE_DATA_BUFFER
            bytesReturned.ptr,                      // self-explanatory
            null                         // we don't do overlapped I/O (yet)
        )

        if (res != TRUE) {
            val err = GetLastError().toInt()
            throwErrnoPath(err, path)
        }
        // this is the only thing we care about
        val flags = buffer.ReparseTag
        return flagged(flags, IO_REPARSE_TAG_SYMLINK)
    }

    /**
     * Copies the attributes from a WIN32_FILE_ATTRIBUTE_DATA object to a [FileAttributes] object.
     */
    @OptIn(ExperimentalUnsignedTypes::class)
    private fun copyAttributes(path: String, struct: WIN32_FILE_ATTRIBUTE_DATA): FileAttributes {
        val size = (struct.nFileSizeHigh.toULong().shl(32)).or(struct.nFileSizeLow.toULong())

        // copy attributes from the struct into our own data class so that the runtime can manage
        // it instead of our memory scope
        return FileAttributes(
            path = path,
            attributes = struct.dwFileAttributes.toInt(),
            size = size,
            creationTime = struct.ftCreationTime.toULong(),
            modificationTime = struct.ftLastWriteTime.toULong(),
            accessTime = struct.ftLastAccessTime.toULong()
        )
    }

    /**
     * Gets the file attributes for the specified path.
     */
    @OptIn(ExperimentalUnsignedTypes::class)
    @Unsafe
    public fun GetFileAttributesEx(path: String): FileAttributes = memScoped {
        val struct = alloc<WIN32_FILE_ATTRIBUTE_DATA>()

        val result = GetFileAttributesExW(
            path,
            GET_FILEEX_INFO_LEVELS.GetFileExInfoStandard,
            struct.ptr
        )

        if (result != TRUE) {
            throwErrnoPath(GetLastError().toInt(), path)
        }

        return copyAttributes(path, struct)
    }

    /**
     * Gets the file attributes for the specified path, returning null if the file is not found.
     */
    @OptIn(ExperimentalUnsignedTypes::class)
    @Unsafe
    public fun __get_attributes_safer(path: String): FileAttributes? = memScoped {
        val struct = alloc<WIN32_FILE_ATTRIBUTE_DATA>()

        val result = GetFileAttributesExW(
            path,
            GET_FILEEX_INFO_LEVELS.GetFileExInfoStandard,
            struct.ptr
        )

        if (result != TRUE) {
            val err = GetLastError().toInt()
            if (err == ERROR_FILE_NOT_FOUND || err == ERROR_PATH_NOT_FOUND) return null
            else throwErrnoPath(err, path)
        }

        return copyAttributes(path, struct)
    }

    // non-standard name as this isn't a wrapper
    /**
     * Gets the real path of a symlink. Returns null if the symlink doesn't point anywhere valid.
     */
    @OptIn(ExperimentalUnsignedTypes::class)
    @Unsafe
    public fun __real_path(path: String): String? {
        // gross amount of driver calls...
        val attrs = __get_attributes_safer(path) ?: return null
        if (!attrs.isSymlink()) return path

        val openFlag = if (attrs.isDirectory) {
            FILE_FLAG_BACKUP_SEMANTICS
        } else {
            FILE_ATTRIBUTE_NORMAL
        }.toUInt()

        val handle = CreateFileW(
            path,
            GENERIC_READ,
            FILE_SHARE_VALID_FLAGS, // allow all sharing since we only care about the target
            null,
            OPEN_EXISTING,
            openFlag,  // open as directory if needed
            null,
        )

        if (handle == null || handle == INVALID_HANDLE_VALUE) {
            val err = GetLastError().toInt()
            // not found means that the symlink points to somewhere invalid
            // so we just ignore it.
            if (err == ERROR_FILE_NOT_FOUND || err == ERROR_PATH_NOT_FOUND) return null
            throwErrnoPath(GetLastError().toInt(), path)
        }

        // evil solution for getting size of filename buffer
        val size = GetFinalPathNameByHandleW(
            handle, null, 0, FILE_NAME_NORMALIZED
        )

        if (size == 0u) {
            CloseHandle(handle)
            throwErrnoPath(GetLastError().toInt(), path)
        }

        // actually do the read instead of torturing win32
        val buf = UShortArray(size.toInt())
        val res = buf.usePinned {
            GetFinalPathNameByHandleW(
                handle, it.addressOf(0), size, FILE_NAME_NORMALIZED
            )
        }

        // always close by this point
        CloseHandle(handle)

        return when {
            res == 0u -> throwErrnoPath(GetLastError().toInt(), path)
            res > size -> throw Error("GetFinalPathNameByHandleW lied to us!")
            // no \0 !
            else -> buf.utf16ToString(res.toInt())
        }
    }

    /**
     * Gets the full path name of a path.
     */
    @OptIn(ExperimentalUnsignedTypes::class)
    @Unsafe
    public fun GetFullPathName(path: String): String {
        val size = GetFullPathNameW(path, 0, null, null)
        if (size == 0u) throwErrnoPath(GetLastError().toInt(), path)

        val buf = UShortArray(size.toInt())
        val res = buf.usePinned {
            GetFullPathNameW(path, size, it.addressOf(0), null)
        }

        if (res == 0u) throwErrnoPath(GetLastError().toInt(), path)
        // no \0 !
        else return buf.utf16ToString(count = res.toInt())
    }

    /**
     * Gets the current directory.
     */
    @Unsafe
    public fun GetCurrentDirectory(): String {
        val size = GetCurrentDirectoryW(0, null)

        val buf = UShortArray(size.toInt())
        val res = buf.usePinned {
            GetCurrentDirectoryW(size, it.addressOf(0))
        }

        return when {
            res == 0u -> throwErrno()
            // recursive call is basically a retry loop, in case it got longer
            res > size -> GetCurrentDirectory()
            // no \0 !
            else -> buf.utf16ToString(res.toInt())
        }
    }

    /**
     * Gets the path to the temporary directory.
     */
    @Unsafe
    public fun GetTempPath(): String {
        val size = GetTempPathW(0, null)
        if (size == 0u) throwErrno()

        val buf = UShortArray(size.toInt())
        val res = buf.usePinned {
            GetTempPathW(size, it.addressOf(0))
        }

        return when {
            res == 0u -> throwErrno()
            res > size -> throw Error("GetTempPathW lied")
            // no trailing null
            else -> buf.utf16ToString(res.toInt())
        }
    }

    /**
     * Creates or opens a new file.
     */
    @Unsafe
    public fun CreateFile(
        path: String,
        desiredAccess: Int,
        creationMode: Int,
        flagsAttributes: Int,
    ): HANDLE {
        val handle = CreateFileW(
            path,
            desiredAccess.toUInt(),
            FILE_SHARE_VALID_FLAGS,
            null,
            creationMode.toUInt(),
            flagsAttributes.toUInt(),
            null,
        )

        if (handle == null || handle == INVALID_HANDLE_VALUE) {
            throwErrnoPath(GetLastError().toInt(), path)
        }

        return handle
    }

    /**
     * Creates a new directory.
     */
    @Unsafe
    public fun CreateDirectory(path: String, existOk: Boolean = false) {
        val result = CreateDirectoryW(path, null)
        if (result != TRUE) {
            val err = GetLastError().toInt()
            if (err == ERROR_ALREADY_EXISTS && existOk) return
            else throwErrnoPath(err, path)
        }
    }

    /**
     * Removes an empty directory.
     */
    @Unsafe
    public fun RemoveDirectory(path: String) {
        val result = RemoveDirectoryW(path)
        if (result != TRUE) {
            val code = GetLastError().toInt()
            throwErrnoPath(code, path)
        }
    }

    /**
     * Creates a [DirEntry] from the result of the last FindXFile call.
     */
    @Unsafe
    private fun findFileShared(context: DirectoryScanContext, name: String): DirEntry {
        // copy data into a better object cos the raw struct sucks
        val subPath = context.path.resolveChild(name)

        // turn the gross bitfield into a nice enumeration
        val attrs = context.struct.dwFileAttributes.toInt()
        val type = when {
            (attrs.and(FILE_ATTRIBUTE_DIRECTORY)) != 0 -> FileType.DIRECTORY
            (attrs.and(FILE_ATTRIBUTE_REPARSE_POINT)) != 0 -> FileType.SYMLINK  // TODO: Not true~!
            else -> FileType.REGULAR_FILE
        }

        return DirEntry(subPath, type)
    }

    /**
     * Finds the first file in the specified path.
     */
    @Unsafe
    public fun FindFirstFile(context: DirectoryScanContext): DirEntry? = memScoped {
        var strPath = context.path.unsafeToString()
        // fix dumb bug wrt scanning directories
        if (!strPath.endsWith("\\*")) strPath += "\\*"

        val result = FindFirstFileW(strPath, context.struct.ptr)
        if (result == null || result == INVALID_HANDLE_VALUE) {
            val res = GetLastError().toInt()
            if (res == ERROR_FILE_NOT_FOUND) return null
            throwErrnoPath(res, strPath)
        }
        context.isOpen = true
        context.handle = result

        // skip over current and parent directory by calling the next file functiion if its that
        // name
        val name = context.struct.cFileName.toKStringFromUtf16()
        if (name == "." || name == "..") {
            return FindNextFile(context)
        }

        return findFileShared(context, name)
    }

    /**
     * Gets the next file for the current [DirectoryScanContext].
     */
    @Unsafe
    public fun FindNextFile(context: DirectoryScanContext): DirEntry? = memScoped {
        requireNotNull(context.handle) { "Must call FindFirstFile before this!" }

        val result = FindNextFileW(context.handle, context.struct.ptr)
        if (result != TRUE) {
            val res = GetLastError().toInt()
            if (res == ERROR_NO_MORE_FILES) {
                return null
            }
        }

        // see FindFirstFile
        val name = context.struct.cFileName.toKStringFromUtf16()
        if (name == "." || name == "..") {
            return FindNextFile(context)
        }

        return findFileShared(context, name)
    }

    /**
     * Closes the specified scan context.
     */
    @Unsafe
    public fun FindClose(context: DirectoryScanContext) {
        val res = FindClose(context.handle)
        if (res != TRUE) {
            throwErrno()
        }
    }

    /**
     * Sets the file pointer for a handle. (equiv. to lseek)
     */
    @Unsafe
    public fun SetFilePointer(handle: HANDLE, count: Int, whence: SeekWhence): Long = memScoped {
        // ECH
        val result = platform.windows.SetFilePointer(
            handle,
            count,
            null,
            whence.number.toUInt()
        )

        run {
            val err = GetLastError().toInt()
            if (result == INVALID_SET_FILE_POINTER && err != NO_ERROR) {
                throwErrno(err)
            }
        }

        // now get the current address
        val higher = alloc<IntVar>()
        val result2 = platform.windows.SetFilePointer(
            handle, 0, higher.ptr,
            FILE_CURRENT.toUInt()
        )

        val err = GetLastError().toInt()
        if (result2 == INVALID_SET_FILE_POINTER && err != NO_ERROR) {
            throwErrno(err)
        }

        return (higher.value.toLong().shl(32)).or(result2.toLong())
    }

    /**
     * Reads [count] bytes into the buffer [buf] from the specified [handle].
     */
    @Unsafe
    public fun ReadFile(
        handle: HANDLE, buf: ByteArray,
        count: Int = buf.size, offset: Int = 0,
    ): Int = memScoped {
        require(offset <= count) { "Offset must be less than count!" }
        val readCnt = alloc<UIntVar>()

        val res = buf.usePinned {
            platform.windows.ReadFile(
                handle,
                it.addressOf(offset),
                count.toUInt(),
                readCnt.ptr,
                null
            )
        }

        if (res != TRUE) {
            throwErrno()
        }

        return readCnt.value.toInt()
    }

    /**
     * Writes [count] bytes from the buffer [buf] into the specified [handle].
     */
    @Unsafe
    public fun WriteFile(
        handle: HANDLE, buf: ByteArray,
        count: Int = buf.size, offset: Int = 0,
    ): Int = memScoped {
        require(offset <= count) { "Offset must be less than count!" }

        val writtenCnt = alloc<UIntVar>()
        val res = buf.usePinned {
            platform.windows.WriteFile(
                handle,
                it.addressOf(offset),
                count.toUInt(),
                writtenCnt.ptr,
                null
            )
        }

        if (res != TRUE) {
            throwErrno()
        }

        return writtenCnt.value.toInt()
    }

    /**
     * Deletes a file.
     */
    @Unsafe
    public fun DeleteFile(path: String) {
        val res = DeleteFileW(path)
        if (res != TRUE) {
            throwErrno()
        }
    }

    /**
     * Moves a file from its original path to the destination path.
     */
    @Unsafe
    public fun MoveFile(from: String, to: String) {
        val res = MoveFileW(from, to)
        if (res != TRUE) {
            throwErrno()
        }
    }

    /**
     * Copes a file from its original location to the destination location.
     */
    @Unsafe
    public fun CopyFile(from: String, to: String, existOk: Boolean) {
        val bool = if (existOk) TRUE else FALSE
        val res = CopyFileW(from, to, bool)
        if (res != TRUE) {
            throwErrno()
        }
    }

    /**
     * Creates a new symbolic link.
     */
    @Unsafe
    public fun CreateSymbolicLink(from: String, to: String) {
        val attributes = GetFileAttributesEx(to)
        var flags = SYMBOLIC_LINK_FLAG_ALLOW_UNPRIVILEGED_CREATE
        if (attributes.isDirectory) {
            flags = flags.or(SYMBOLIC_LINK_FLAG_DIRECTORY)
        }

        val res = CreateSymbolicLinkW(from, to, flags.toUInt())
        if (res == (0u).toUByte()) {
            throwErrno()
        }
    }

    /**
     * Implements sane high-level unlink(3) like semantics (unlike DeleteFile).
     *
     * This means:
     *   - Deletes regular files using DeleteFileW (thus fails on directories)
     *   - Deletes regular symlinks using DeleteFileW
     *   - Deletes directory symlinks using RemoveDirectoryW
     */
    // thanks to: https://stackoverflow.com/questions/1485155/check-if-a-file-is-real-or-a-symbolic-link
    @OptIn(Unsafe::class)
    public fun __highlevel_unlink(path: String) {
        val attrs = GetFileAttributesEx(path)
        val isLink = attrs.isSymlink()
        return if (!isLink) DeleteFile(path)
        else {
            if (attrs.isDirectory) RemoveDirectory(path)
            else DeleteFile(path)
        }
    }

    // == Generic stuff == //
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

        if (result != TRUE) {
            throwErrno()
        }

        val count = sizePtr.value.toInt()

        return buf.utf16ToString(count - 1)
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

        // no trailing \0
        return buf.utf16ToString(count)
    }

    /**
     * Expands an environment string into its real value (e.g. ``%PATH%``).
     */
    @Unsafe
    public fun ExpandEnvironmentStrings(name: String): String {
        val size = ExpandEnvironmentStringsW(name, null, 0)
        if (size == 0u) throwErrno()

        val buf = UShortArray(size.toInt())
        val res = buf.usePinned {
            ExpandEnvironmentStringsW(name, it.addressOf(0), size)
        }

        // DOES include a trailing \0
        return when {
            res == 0u -> throwErrno()
            res.toInt() > buf.size -> throw Error("ExpandEnvironmentStringsW lied")
            else -> buf.utf16ToString(count = (res - 1u).toInt())
        }
    }
}
