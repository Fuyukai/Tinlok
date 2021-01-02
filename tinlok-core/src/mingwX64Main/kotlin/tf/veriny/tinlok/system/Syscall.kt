/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

@file:Suppress("RemoveRedundantQualifierName")

package tf.veriny.tinlok.system

import ddk._K_REPARSE_DATA_BUFFER
import kotlinx.cinterop.*
import platform.posix.*
import platform.windows.*
import platform.windows.WSAECONNABORTED
import platform.windows.WSAECONNREFUSED
import platform.windows.WSAECONNRESET
import platform.windows.WSAENETUNREACH
import platform.windows.WSAESHUTDOWN
import platform.windows.WSAETIMEDOUT
import platform.winsock2.posix_getsockopt
import platform.winsock2.posix_setsockopt
import tf.veriny.tinlok.Unsafe
import tf.veriny.tinlok.exc.WindowsException
import tf.veriny.tinlok.fs.DirEntry
import tf.veriny.tinlok.fs.FileType
import tf.veriny.tinlok.fs.path.resolveChild
import tf.veriny.tinlok.io.*
import tf.veriny.tinlok.net.*
import tf.veriny.tinlok.net.dns.GAIException
import tf.veriny.tinlok.net.socket.BsdSocketOption
import tf.veriny.tinlok.net.socket.RecvFrom
import tf.veriny.tinlok.net.socket.ShutdownOption
import tf.veriny.tinlok.util.*

// FIXME: kotlinx.cinterop can do typealias CPointer<out CPointed>, but we can't!
/** The type of a native file handle. */
public actual class FILE(public val handle: HANDLE)

/** The type of a native socket. */
public actual typealias SOCKET = Long

/**
 * Platform call container object for all Win32 calls.
 */
@OptIn(ExperimentalUnsignedTypes::class)
public actual object Syscall {

    init {
        platform.posix.init_sockets()
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
     * Closes a file.
     */
    @Unsafe
    public actual fun __close_file(fd: FILE) {
        return CloseHandle(fd.handle)
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

    // == Error handling == //
    /**
     * Formats the message for an error code.
     */
    @OptIn(ExperimentalUnsignedTypes::class)
    @Unsafe
    public fun FormatMessage(code: Int): String = memScoped {
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
            ERROR_FILE_NOT_FOUND, ERROR_PATH_NOT_FOUND -> FileNotFoundException(path.toByteString())
            ERROR_FILE_EXISTS -> FileAlreadyExistsException(path.toByteString())
            ERROR_DIR_NOT_EMPTY -> DirectoryNotEmptyException(path.toByteString())

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
        val result = SetFilePointer(
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
        val result2 = SetFilePointer(
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
     * Gets the current cursor for a file (the seek point).
     */
    @Unsafe
    public actual fun __get_file_cursor(fd: FILE): Long {
        return SetFilePointer(fd.handle, 0, SeekWhence.CURRENT)
    }

    /**
     * Sets the current absolute cursor for a file.
     */
    @Unsafe
    public actual fun __set_file_cursor(fd: FILE, point: Long) {
        SetFilePointer(fd.handle, point.toInt(), SeekWhence.START)
    }

    /**
     * Reads [size] bytes into [address] from the specified [handle], returning the number of bytes
     * read.
     */
    @Unsafe
    public fun ReadFile(
        handle: HANDLE, address: CPointer<ByteVar>, size: Int,
    ): BlockingResult = memScoped {
        val readCnt = alloc<UIntVar>()

        val res = ReadFile(
            handle,
            address,
            size.toUInt(),
            readCnt.ptr,
            null
        )

        if (res != TRUE) {
            if (GetLastError().toInt() == ERROR_IO_PENDING) return BlockingResult.WOULD_BLOCK
            throwErrno()
        }

        return BlockingResult(readCnt.value.toLong())
    }

    /**
     * Reads [count] bytes into the buffer [buf] from the specified [handle].
     */
    @Unsafe
    public fun ReadFile(
        handle: HANDLE, buf: ByteArray,
        count: Int = buf.size, offset: Int = 0,
    ): BlockingResult = memScoped {
        require(count + offset <= buf.size) {
            "count + offset > buf.sizew"
        }
        return buf.usePinned {
            ReadFile(handle, it.addressOf(offset), count)
        }
    }

    /**
     * Reads [size] bytes from the file [fd] to [address], returning the number of bytes written.
     *
     * This method will NOT attempt to retry.
     */
    @Unsafe
    public actual fun __read_file(fd: FILE, address: CPointer<ByteVar>, size: Int): BlockingResult {
        return this.ReadFile(fd.handle, address, size)
    }

    /**
     * Writes [count] bytes from the pointer [buf] into the specified [handle].
     */
    @Unsafe
    public fun WriteFile(
        handle: HANDLE, buf: CPointer<ByteVar>,
        count: Int,
    ): BlockingResult = memScoped {
        val writtenCnt = alloc<UIntVar>()
        val res = WriteFile(
            handle,
            buf,
            count.toUInt(),
            writtenCnt.ptr,
            null
        )

        if (res != TRUE) {
            if (GetLastError().toInt() == ERROR_IO_PENDING) return BlockingResult.WOULD_BLOCK
            throwErrno()
        }

        return BlockingResult(writtenCnt.value.toLong())
    }

    /**
     * Writes [count] bytes from the buffer [buf] into the specified [handle].
     */
    @Unsafe
    public fun WriteFile(
        handle: HANDLE, buf: ByteArray,
        count: Int = buf.size, offset: Int = 0,
    ): BlockingResult = memScoped {
        require(count + offset <= buf.size) {
            "count + offset > buf.sizew"
        }

        return buf.usePinned {
            WriteFile(handle, it.addressOf(offset), count)
        }
    }

    /**
     * Writes [size] bytes from [address] to the file [fd], returning the number of bytes written.
     *
     * This method will NOT attempt to retry.
     */
    @Unsafe
    public actual fun __write_file(
        fd: FILE, address: CPointer<ByteVar>, size: Int,
    ): BlockingResult {
        return this.WriteFile(fd.handle, address, size)
    }

    // Copied from linux Syscall
    /**
     * Writes [size] bytes from [address] to the file [fd], returning the number of bytes written.
     *
     * This method will attempt to retry until the full [size] bytes are written. Use a
     * platform-specific method if you wish to avoid that.
     */
    @Unsafe
    public actual fun __write_file_with_retry(
        fd: FILE, address: CPointer<ByteVar>, size: Int,
    ): BlockingResult {
        var lastOffset = 0

        while (true) {
            // pointer arithmetic...
            // not fun!
            // address is always the base address, so we always add the last offset
            // and last offset is always incremented from the amount we've actually written

            val ptr = (address + lastOffset) ?: error("pointer arithmetic returned null?")

            //
            val amount = this.WriteFile(fd.handle, ptr, size - lastOffset)
            if (!amount.isSuccess) break

            lastOffset += amount.count.toInt()

            if (lastOffset >= size) break
        }

        return if (lastOffset <= 0) {
            // lastOffset of zero means write() returned EAGAIN immediately
            BlockingResult.WOULD_BLOCK
        } else {
            // lastOffset of greater means we hit EAGAIN or finished writing fully
            BlockingResult(lastOffset.toLong())
        }
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

    // == Networking == //
    /**
     * Throws an appropriate WinSock error for the specified [code].
     */
    @Unsafe
    public fun throwErrnoWSA(code: Int): Nothing {
        throw when (code) {
            WSAENETUNREACH -> NetworkUnreachableException()
            WSAECONNRESET -> ConnectionResetException()
            WSAECONNABORTED -> ConnectionAbortedException()
            WSAECONNREFUSED -> ConnectionRefusedException()
            WSAESHUTDOWN -> SocketShutdownException()
            WSAETIMEDOUT -> TimeoutException()
            in 0..999 -> throwErrno(code)
            else -> {
                WindowsException(
                    winerror = code,
                    message = "[WinError ${code}] ${FormatMessage(code)}"
                )
            }
        }
    }

    @Unsafe
    public fun throwErrnoWSA(): Nothing {
        val code = platform.windows.WSAGetLastError()
        throwErrnoWSA(code)
    }

    // copied from linuxMain...
    /**
     * Converts an [IPv6Address] to a [sockaddr].
     */
    @Unsafe
    public fun __ipv6_to_sockaddr(alloc: NativePlacement, ip: IPv6Address, port: Int): sockaddr {
        val ipRepresentation = ip.representation.unwrap()
        // runtime safety check!!
        val size = ipRepresentation.size
        require(size == 16) {
            "IPv6 address size was mismatched (expected 16, got $size), refusing to clobber memory"
        }

        val struct = alloc.alloc<sockaddr_in6> {
            sin6_family = AF_INET6.toShort()  // ?
            // have to manually write to the array contained within
            sin6_addr.arrayMemberAt<ByteVar>(0L).unsafeClobber(ipRepresentation)
            sin6_port = htons(port.toUShort())
        }

        return struct.reinterpret()
    }

    /**
     * Converts an [IPv4Address] to a [sockaddr].
     */
    @Unsafe
    public fun __ipv4_to_sockaddr(alloc: NativePlacement, ip: IPv4Address, port: Int): sockaddr {
        val ipRepresentation = ip.representation.toUByteArray()
        val struct = alloc.alloc<sockaddr_in> {
            sin_family = platform.windows.AF_INET.toShort()
            sin_addr.S_un.S_addr = ipRepresentation.toUInt()
            sin_port = htons(port.toUShort())
        }

        return struct.reinterpret()
    }

    /**
     * Opens a new socket, with the specified [family], [type], and [protocol].
     */
    @Unsafe
    public fun socket(family: AddressFamily, type: SocketType, protocol: IPProtocol): SOCKET {
        val sock = platform.windows.socket(
            // socket numbers
            family.number, type.number, protocol.number
        )

        if (sock == INVALID_SOCKET) {
            throwErrnoWSA()
        }

        return sock.toLong()
    }

    /**
     * Sets the socket [option] to the [value] provided.
     */
    @Unsafe
    public fun <T> setsockopt(
        sock: SOCKET, option: BsdSocketOption<T>, value: T,
    ): Unit = memScoped {
        val native = option.toNativeStructure(this, value)
        val size = option.nativeSize()
        val res = posix_setsockopt(
            sock.toULong(), option.level, option.bsdOptionValue, native,
            size.toInt()
        )

        if (res == SOCKET_ERROR) {
            throwErrnoWSA()
        }
    }

    /**
     * Gets a socket option.
     */
    @Unsafe
    public fun <T> getsockopt(sock: SOCKET, option: BsdSocketOption<T>): T = memScoped {
        val storage = option.allocateNativeStructure(this)
        val size = cValuesOf(option.nativeSize().toInt())
        val res = posix_getsockopt(
            sock.toULong(), option.level, option.bsdOptionValue,
            storage, size
        )

        if (res == SOCKET_ERROR) {
            throwErrnoWSA()
        }

        option.fromNativeStructure(this, storage)
    }


    /**
     * Connects a socket to an address.
     */
    @Suppress("RemoveRedundantQualifierName")
    @Unsafe
    public fun connect(sock: SOCKET, address: ConnectionInfo): BlockingResult = memScoped {
        val struct: sockaddr
        val size: UInt

        when (address.family) {
            AddressFamily.AF_INET6 -> {
                val info = (address as InetConnectionInfo)
                struct = __ipv6_to_sockaddr(this, info.ip as IPv6Address, info.port)
                size = sizeOf<sockaddr_in6>().toUInt()
            }
            AddressFamily.AF_INET -> {
                val info = (address as InetConnectionInfo)
                struct = __ipv4_to_sockaddr(this, info.ip as IPv4Address, info.port)
                size = sizeOf<sockaddr_in>().toUInt()
            }
            else -> throw UnsupportedOperationException("Don't know how to use $address")
        }

        return when (val res = platform.windows.connect(sock.toULong(), struct.ptr, size.toInt())) {
            0 -> BlockingResult.DIDNT_BLOCK
            SOCKET_ERROR -> {
                when (val err = platform.windows.WSAGetLastError()) {
                    platform.windows.WSAEINPROGRESS,
                    platform.windows.WSAEWOULDBLOCK,
                    -> BlockingResult.WOULD_BLOCK
                    else -> throwErrnoWSA(err)
                }
            }
            else -> error("Unknown return value: $res")
        }
    }

    /**
     * Performs an ioctl on a socket.
     */
    @Unsafe
    public fun ioctlsocket(sock: SOCKET, cmd: Int, param: UInt): Unit = memScoped {
        val ptr = alloc<UIntVar>()
        ptr.value = param
        val res = platform.windows.ioctlsocket(sock.toULong(), cmd, ptr.ptr)
        if (res == SOCKET_ERROR) {
            throwErrnoWSA()
        }
    }

    /**
     * Connects a socket with a timeout.
     */
    @Unsafe
    public fun __connect_timeout(sock: SOCKET, address: ConnectionInfo, timeout: Int = 30_000) {
        // same fast path as on linux
        if (timeout < 0) {
            connect(sock, address).ensureNonBlock()
            return
        }

        // slow path......
        memScoped {
            // similar slow path
            ioctlsocket(sock, platform.windows.FIONBIO.toInt(), 1u)

            val connRes = connect(sock, address)
            if (connRes.isSuccess) {
                // connected immediately, set back to blocking and return
                ioctlsocket(sock, platform.windows.FIONBIO.toInt(), 0u)
                return
            }

            // start a poll operation
            val pollfd = allocArray<WSAPOLLFD>(1)
            pollfd[0].fd = sock.toULong()
            pollfd[0].events = POLLOUT.toShort()

            val res = WSAPoll(pollfd, 1u, timeout)
            if (res == SOCKET_ERROR) {
                throwErrnoWSA()
            } else if (res == 0) {
                // no sockets available
                throw TimeoutException()
            }

            // succeeded... maybe?
            val revent = pollfd[0].revents.toInt()
            if (flagged(revent, POLLERR) || flagged(revent, POLLHUP) || flagged(revent, POLLNVAL)) {
                error("WSAPoll() gave revent '$revent'")
            }
            // ok, we definitely succeeded if we're here
            // set non-blocking and return
            ioctlsocket(sock, platform.windows.FIONBIO.toInt(), 0u)
        }
    }

    /**
     * Binds a socket to an address.
     */
    @Unsafe
    public fun bind(sock: SOCKET, address: ConnectionInfo) {
        val res = memScoped {
            val struct: sockaddr
            val size: Long

            when (address.family) {
                AddressFamily.AF_INET6 -> {
                    val info = (address as InetConnectionInfo)
                    struct = __ipv6_to_sockaddr(this, info.ip as IPv6Address, info.port)
                    size = sizeOf<sockaddr_in6>()
                }
                AddressFamily.AF_INET -> {
                    val info = (address as InetConnectionInfo)
                    struct = __ipv4_to_sockaddr(this, info.ip as IPv4Address, info.port)
                    size = sizeOf<sockaddr_in>()
                }
                else -> error("Unknown or unsupported address family: ${address.family}")
            }

            platform.windows.bind(sock.toULong(), struct.ptr, size.toInt())
        }

        if (res == SOCKET_ERROR) {
            throwErrnoWSA()
        }
    }

    /**
     * Marks a socket as listening, with the specified [backlog].
     */
    @Unsafe
    public fun listen(sock: SOCKET, backlog: Int) {
        val res = platform.windows.listen(sock.toULong(), backlog)
        if (res == SOCKET_ERROR) {
            throwErrnoWSA()
        }
    }

    /**
     * Accepts a new connection on a socket.
     */
    @OptIn(Unsafe::class)
    public fun accept(sock: SOCKET): BlockingResult {
        val res = WSAAccept(sock.toULong(), null, null, null, 0UL)
        if (res == INVALID_SOCKET) {
            val error = platform.windows.WSAGetLastError()
            return if (error == platform.windows.WSAEWOULDBLOCK) BlockingResult.WOULD_BLOCK
            else throwErrnoWSA(error)
        }

        return BlockingResult(res.toLong())
    }

    /**
     * Receives data from the specified socket.
     */
    @Unsafe
    public fun recv(
        sock: SOCKET, address: CPointer<ByteVar>, size: Int, flags: Int,
    ): BlockingResult {
        val res = platform.windows.recv(sock.toULong(), address, size, flags)
        if (res == SOCKET_ERROR) {
            val error = platform.windows.WSAGetLastError()
            return if (error == platform.windows.WSAEWOULDBLOCK) BlockingResult.WOULD_BLOCK
            else throwErrnoWSA(error)
        }

        return BlockingResult(res.toLong())
    }

    /**
     * Receives data from the specified socket.
     */
    @Unsafe
    @Suppress("RemoveRedundantQualifierName")
    public fun recv(
        sock: SOCKET, buf: ByteArray, size: Int, offset: Int, flags: Int,
    ): BlockingResult {
        // TODO: Use WSARecv always?
        require(size + offset <= buf.size) {
            "offset ($offset) + size ($size) > buf.size (${buf.size})"
        }

        return buf.usePinned {
            recv(sock, it.addressOf(offset), size, flags)
        }

    }

    /**
     * Receives bytes from a socket into the specified buffer, returning the number of bytes read as
     * well as the address of the sender.
     */
    @Unsafe
    public fun <I : ConnectionInfo> recvfrom(
        sock: SOCKET,
        buf: ByteArray,
        size: Int = buf.size, offset: Int = 0,
        flags: Int = 0,

        /* extra flags for address creation */
        creator: ConnectionInfoCreator<I>,
    ): RecvFrom<I>? = memScoped {
        require(offset + size <= buf.size) {
            "offset ($offset) + size ($size) > buf.size (${buf.size})"
        }

        val addr = alloc<sockaddr_storage>().reinterpret<sockaddr>()
        val sizePtr = alloc<IntVar>()
        sizePtr.value = sizeOf<sockaddr_storage>().toInt()

        val read = buf.usePinned {
            platform.posix.recvfrom(
                sock.toULong(), it.addressOf(offset), size, flags, addr.ptr,
                sizePtr.ptr
            )
        }

        // closed...
        if (read == 0) return null

        // errored...
        if (read == SOCKET_ERROR) {
            val error = platform.windows.WSAGetLastError()
            return if (error == platform.windows.WSAEWOULDBLOCK) null
            else throwErrnoWSA(error)
        }

        val (ip, port) = addr.toKotlin() ?: error("null address")
        val kAddr = creator.from(ip, port)
        return RecvFrom(BlockingResult(read.toLong()), kAddr)
    }

    /**
     * Sends data on the specified socket.
     */
    @Unsafe
    public fun send(
        sock: SOCKET, address: CPointer<ByteVar>, size: Int, flags: Int,
    ): BlockingResult {
        val res = platform.posix.send(sock.toULong(), address, size, flags)
        if (res == SOCKET_ERROR) {
            val error = platform.windows.WSAGetLastError()
            return if (error == platform.windows.WSAEWOULDBLOCK) BlockingResult.WOULD_BLOCK
            else throwErrnoWSA(error)
        }

        return BlockingResult(res.toLong())
    }

    /**
     * Sends data on the specified socket.
     */
    @Unsafe
    public fun send(
        sock: SOCKET, buf: ByteArray, size: Int, offset: Int, flags: Int,
    ): BlockingResult {
        require(size + offset <= buf.size) {
            "offset ($offset) + size ($size) > buf.size (${buf.size})"
        }

        return buf.usePinned {
            // XXX: ``platform.windows.send`` has buf turned into a CString for some reason.
            send(sock, it.addressOf(offset), size, flags)
        }
    }

    // Copied from linuxMain
    /**
     * Writes [size] bytes into [socket] from [address].
     *
     * This method will attempt to retry until the full [size] bytes are written. Use a
     * platform-specific method if you wish to avoid that.
     */
    @Unsafe
    public fun __write_socket_with_retry(
        socket: SOCKET, address: CPointer<ByteVar>, size: Int, flags: Int,
    ): BlockingResult {
        var lastOffset = 0

        // copied from write()

        while (true) {
            val ptr = (address + lastOffset) ?: error("pointer arithmetic returned null?")
            val amount = this.send(socket, ptr, size - lastOffset, flags)
            if (!amount.isSuccess) break

            lastOffset += amount.count.toInt()

            if (lastOffset >= size) break
        }

        return if (lastOffset <= 0) {
            // lastOffset of zero means write() returned EAGAIN immediately
            BlockingResult.WOULD_BLOCK
        } else {
            // lastOffset of greater means we hit EAGAIN or finished writing fully
            BlockingResult(lastOffset.toLong())
        }
    }

    /**
     * Writes bytes to a socket from the specified buffer.
     */
    @Unsafe
    public fun <I> sendto(
        sock: SOCKET, buffer: ByteArray, size: Int, offset: Int, flags: Int,
        address: I,
    ): BlockingResult = memScoped {
        val struct: sockaddr
        val addrSize: Int

        when (address) {
            is InetConnectionInfo -> {
                when (val ip = address.ip) {
                    is IPv6Address -> {
                        struct = __ipv6_to_sockaddr(this, ip, address.port)
                        addrSize = sizeOf<sockaddr_in6>().toInt()
                    }
                    is IPv4Address -> {
                        struct = __ipv4_to_sockaddr(this, ip, address.port)
                        addrSize = sizeOf<sockaddr_in>().toInt()
                    }
                }
            }
            else -> throw IllegalArgumentException(
                "Don't know how to translatee $address into a sockaddr"
            )
        }

        val result = buffer.usePinned {
            platform.posix.sendto(
                sock.toULong(), it.addressOf(offset), size, flags, struct.ptr, addrSize
            )
        }

        if (result == SOCKET_ERROR) {
            return if (errno == EAGAIN) BlockingResult.WOULD_BLOCK
            else throwErrno(errno)
        }

        return BlockingResult(result.toLong())
    }

    /**
     * Shuts down one or both sides of a socket.
     */
    @Unsafe
    @Suppress("RemoveRedundantQualifierName")
    public fun shutdown(sock: SOCKET, how: ShutdownOption) {
        val res = platform.windows.shutdown(sock.toULong(), how.number)
        if (res == SOCKET_ERROR) throwErrnoWSA()
    }

    /**
     * Closes the specified socket.
     */
    @Unsafe
    public fun closesocket(sock: SOCKET) {
        val res = platform.windows.closesocket(sock.toULong())
        if (res == SOCKET_ERROR) throwErrnoWSA()
    }

    /**
     * Looks up DNS information.
     */
    @Unsafe
    public actual fun getaddrinfo(
        node: String?, service: String?,
        family: Int, type: Int, protocol: Int, flags: Int,
    ): List<AddrInfo> = memScoped {
        // copied from linuxMain
        val hints = alloc<ADDRINFOW>()
        val res = allocPointerTo<ADDRINFOW>()
        memset(hints.ptr, 0, sizeOf<ADDRINFOW>().convert())

        hints.ai_flags = flags
        if (node == null) {
            hints.ai_flags = hints.ai_flags or AI_PASSIVE
        }
        hints.ai_socktype = type
        hints.ai_family = family
        hints.ai_protocol = protocol

        val nodePtr = node?.wcstr?.getPointer(this)
        val servicePtr = service?.wcstr?.getPointer(this)

        val code = GetAddrInfoW(nodePtr, servicePtr, hints.ptr, res.ptr)
        if (code < 0) {
            val err = platform.windows.WSAGetLastError()
            throw GAIException(errno = code, message = FormatMessage(err))
        }

        // copy over the structures, then free them
        val items = mutableListOf<AddrInfo>()

        var nextPtr = res.value
        while (true) {
            if (nextPtr == null) break
            val addrinfo = nextPtr.pointed
            // ?
            if (addrinfo.ai_addr == null) {
                nextPtr = addrinfo.ai_next
                continue
            }


            val sockaddr = addrinfo.ai_addr!!.readBytesFast(addrinfo.ai_addrlen.toInt())
            val kAddrinfo = AddrInfo(
                addr = sockaddr,
                canonname = addrinfo.ai_canonname?.toKStringFromUtf16(),
                family = addrinfo.ai_family,
                flags = addrinfo.ai_flags,
                protocol = addrinfo.ai_protocol,
                type = addrinfo.ai_socktype,
            )
            items.add(kAddrinfo)
            nextPtr = addrinfo.ai_next
        }

        // now free the linked-list C structure, now that kotlin is managing the object
        FreeAddrInfoW(res.value)

        return items
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
