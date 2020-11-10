/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package tf.lotte.tinlok.system

import kotlinx.cinterop.*
import platform.posix.*
import tf.lotte.tinlok.Unsafe
import tf.lotte.tinlok.io.*
import tf.lotte.tinlok.net.*
import tf.lotte.tinlok.net.dns.GAIException
import tf.lotte.tinlok.net.socket.BsdSocketOption
import tf.lotte.tinlok.net.socket.RecvFrom
import tf.lotte.tinlok.net.socket.ShutdownOption
import tf.lotte.tinlok.util.ByteString
import tf.lotte.tinlok.util.toKotlin
import tf.lotte.tinlok.util.toUInt
import kotlin.experimental.ExperimentalTypeInference

internal typealias FD = Int

// TODO: Probably want to make some of these enums.

/**
 * Namespace object for all the libc calls.
 *
 * This is preferred over regular libc calls as it throws exceptions appropriately. This object
 * is very foot-gunny, but assertions are provided for basic sanity checks.
 *
 * Public extensions (ones not a direct mapping to libc) are prefixed with two underscores.
 */
@OptIn(ExperimentalTypeInference::class, ExperimentalUnsignedTypes::class)
public actual object Syscall {
    // wrapper types, for e.g. accept

    public const val ERROR: Int = -1
    public const val LONG_ERROR: Long = -1L

    private inline val Int.isError: Boolean get() = this == ERROR
    private inline val Long.isError: Boolean get() = this == LONG_ERROR
    private inline val CPointer<*>?.isError: Boolean get() = this == null

    // See: https://www.python.org/dev/peps/pep-0475/#rationale
    // Not completely the same, but similar justification.

    /**
     * Retries a function that uses C error handling semantics if EINTR is returned.
     */
    @Unsafe
    public inline fun retry(block: () -> Int): Int {
        while (true) {
            val result = block()
            if (result == ERROR && errno == EINTR) continue
            return result
        }
    }

    /**
     * Retry, but for Long.
     */
    @Unsafe
    @OverloadResolutionByLambdaReturnType  // magic!
    public inline fun retry(block: () -> Long): Long {
        while (true) {
            val result = block()
            if (result == LONG_ERROR && errno == EINTR) continue
            return result
        }
    }

    /**
     * Throws on errno for paths.
     */
    public fun throwErrnoPath(errno: Int, path: String): Nothing {
        throw when (errno) {
            ENOENT -> FileNotFoundException(path)
            EEXIST -> FileAlreadyExistsException(path)
            EISDIR -> IsADirectoryException(path)
            else -> throwErrno(errno)
        }
    }

    /**
     * Throws on errno.
     */
    public fun throwErrno(errno: Int): Nothing {
        throw when (errno) {
            EACCES -> AccessDeniedException()
            EPERM -> PermissionDeniedException()

            // sockets
            EPIPE -> BrokenPipeException()
            ESHUTDOWN -> SocketShutdownException()
            ECONNRESET -> ConnectionResetException()
            ECONNABORTED -> ConnectionAbortedException()
            ECONNREFUSED -> ConnectionRefusedException()
            ETIMEDOUT -> TimeoutException()
            ENETUNREACH -> NetworkUnreachableException()

            ENOPROTOOPT -> UnsupportedOperationException(
                "The specified option is not supported by this protocol."
            )
            ENOTSOCK -> UnsupportedOperationException("The specified fd is not a socket")

            EINVAL -> IllegalArgumentException()

            else -> OSException(message = "[errno ${errno}] ${strerror(errno)}")
        }
    }

    /**
     * Gets the current errno strerror().
     */
    @Unsafe
    public fun strerror(): String {
        return strerror(posix_errno())
    }

    /**
     * Gets the strerror() for the specified errno.
     */
    public fun strerror(errno: Int): String {
        // this is large, but should always be sufficient
        // if any strerror is too big, i'll update it
        val buf = ByteArray(1024)
        val res = buf.usePinned {
            strerror_r(errno, it.addressOf(0), buf.size.toULong())
        }
        if (res.isError) throw Error("strerror returned errno, can't reasonably do anything")
        // gross!
        return buf.toKString()
    }

    /**
     * Like strerror(), but for getaddrinfo return codes.
     */
    @Unsafe
    public fun gai_strerror(errno: Int): String {
        val res = platform.posix.gai_strerror(errno)
        return res?.readZeroTerminated()?.toKString() ?: "Unknown error!"
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

    // == File opening/closing == //
    // region File opening/closing

    @Unsafe
    private fun openShared(path: String, fd: Int): Int {
        if (fd.isError) {
            throwErrnoPath(errno, path)
        }

        return fd
    }

    @Unsafe
    public fun open(path: String, mode: Int): FD {
        val fd = retry { platform.posix.open(path, mode) }
        return openShared(path, fd)
    }

    /**
     * Opens a new file descriptor for the path [path].
     */
    @Unsafe
    public fun open(path: String, mode: Int, permissions: Int): FD {
        val fd = retry { platform.posix.open(path, mode, permissions) }
        return openShared(path, fd)
    }

    /**
     * Closes a file descriptor.
     */
    @Unsafe
    public fun close(fd: FD) {
        val res = platform.posix.close(fd)
        if (res.isError) {
            throwErrno(errno)
        }
    }

    /**
     * Closes all of the specified file descriptors. This is guaranteed to call close(2) on all,
     * but will only raise an error for the first failed fd.
     *
     * This is an extension to libc.
     */
    @Unsafe
    public fun __closeall(vararg fds: FD) {
        var isErrored = false
        var lastErrno = 0
        for (fd in fds) {
            val res = platform.posix.close(fd)
            if (res.isError && !isErrored) {
                isErrored = true
                lastErrno = errno
            }
        }

        if (isErrored) {
            throwErrno(lastErrno)
        }
    }

    // endregion

    // == Generic Linux I/O == //
    // region Linux I/O
    /** The maximum size for most I/O functions. */
    public const val IO_MAX: Int = 0x7ffff000

    /**
     * Reads up to [size] bytes from file descriptor [fd] into the buffer [buffer], at the offset
     * [offset].
     */
    @Unsafe
    public fun read(fd: FD, buffer: ByteArray, size: Int, offset: Int = 0): BlockingResult {
        require(size <= IO_MAX) { "$size is more than IO_MAX" }
        require(buffer.size >= size) { "$size is more than buffer size (${buffer.size})" }
        require(offset + size <= buffer.size) {
            "offset ($offset) + size ($size) > buffer.size (${buffer.size})"
        }
        val readCount = buffer.usePinned {
            retry { read(fd, it.addressOf(offset), size.toULong()) }
        }

        if (readCount.isError) {
            return when (errno) {
                EAGAIN -> BlockingResult.WOULD_BLOCK
                else -> throwErrno(errno)
            }
        }

        return BlockingResult(readCount)
    }

    /**
     * Writes up to [size] bytes to the specified file descriptor, returning the number of bytes
     * actually written.
     *
     * This handles EINTR transparently, continuing a write if interrupted.
     */
    @Unsafe
    public fun write(
        fd: FD, buffer: ByteArray, size: Int = buffer.size, offset: Int = 0,
    ): BlockingResult {
        require(size <= IO_MAX) { "$size is more than IO_MAX" }
        require(buffer.size >= size) { "$size is more than buffer size (${buffer.size})" }
        require(offset + size <= buffer.size) {
            "offset ($offset) + size ($size) > buffer.size (${buffer.size})"
        }

        var nextOffset = offset
        var hitAgain = false

        // head spinny logic
        buffer.usePinned {
            while (true) {
                val written = write(
                    fd, it.addressOf(nextOffset),
                    (size - nextOffset).toULong()
                )

                // eintr means it didn't write anything, so we can transparently retry
                if (written.isError) {
                    when (errno) {
                        EINTR -> continue
                        EAGAIN -> {
                            hitAgain = true
                            break
                        }
                        else -> throwErrno(errno)
                    }
                }

                // make sure we actually write all of the bytes we want to write
                // this will never be more than INT_MAX, so we're fine
                nextOffset += written.toInt()
                if (nextOffset >= size) {
                    break
                }
            }
        }

        // if we hit EAGAIN, and nextOffset is 0, we didn't write anything
        return if (hitAgain && nextOffset == 0) {
            BlockingResult.WOULD_BLOCK
        } else {
            BlockingResult(nextOffset.toLong())
        }
    }

    /**
     * Writes [size] bytes from the FD [from] into the FD [to] starting from the offset [offset].
     */
    @Unsafe
    public fun sendfile(to: FD, from: FD, size: ULong, offset: Long = 0): ULong = memScoped {
        var totalWritten = offset.toULong()
        // retry loop to ensure we write ALL of the data
        while (true) {
            val longValue = cValuesOf(totalWritten.toLong())  // off_t
            val written = platform.linux.sendfile(
                to, from,
                longValue,
                (size - totalWritten)
            )

            if (written.isError && errno != EINTR) {
                throwErrno(errno)
            }

            // always safe conversion
            totalWritten += written.toULong()
            if (totalWritten >= size) {
                break
            }
        }

        return totalWritten
    }

    /**
     * Performs a seek operation on the file descriptor [fd].
     */
    @Unsafe
    public fun lseek(fd: FD, position: Long, whence: SeekWhence): Long {
        val res = lseek(fd, position, whence.number)
        if (res.isError) {
            throwErrno(errno)
        }

        return res
    }

    // endregion

    // == File Polling == //
    // region File Polling

    /**
     * Performs a stat/lstat() call without throwing, returning null instead.
     */
    @Unsafe
    public fun __stat_safer(alloc: NativePlacement, path: String, followSymlinks: Boolean): stat? {
        val pathStat = alloc.alloc<stat>()

        val res = if (followSymlinks) stat(path, pathStat.ptr) else lstat(path, pathStat.ptr)
        if (res.isError) {
            if (errno == ENOENT) return null
            else throwErrnoPath(errno, path)
        }

        return pathStat
    }

    /**
     * Gets statistics about a file.
     */
    @Unsafe
    public fun stat(alloc: NativePlacement, path: String, followSymlinks: Boolean): stat {
        return __stat_safer(alloc, path, followSymlinks)
            ?: throwErrnoPath(errno, path)
    }

    /**
     * Gets access information about a file.
     */
    @Unsafe
    public fun access(path: String, mode: Int): Boolean {
        val result = platform.posix.access(path, mode)
        if (result.isError) {
            if (errno == EACCES) return false
            if (errno == ENOENT && mode == F_OK) return false
            else throwErrno(errno)
        }

        return true
    }

    /**
     * Opens a directory for file listing.
     */
    @Suppress("FoldInitializerAndIfToElvis")
    @Unsafe
    public fun opendir(path: String): CPointer<DIR> {
        val dirfd = platform.posix.opendir(path)
        if (dirfd == null) {
            throwErrnoPath(errno, path)
        }

        return dirfd
    }

    /**
     * Reads a new entry from an opened directory. Returns null on the last entry.
     */
    @Unsafe
    public fun readdir(dirfd: CValuesRef<DIR>): CPointer<dirent>? {
        // reset errno so we can tell apart an error from an end of stream
        set_posix_errno(0)

        val res = platform.posix.readdir(dirfd)
        if (res == null && errno != 0) {
            throwErrno(errno)
        }

        return res
    }

    /**
     * Closes an opened directory.
     */
    @Unsafe
    public fun closedir(dirfd: CValuesRef<DIR>) {
        val res = platform.posix.closedir(dirfd)
        if (res.isError) {
            throwErrno(errno)
        }
    }

    // endregion

    // == Filesystem access == //
    /**
     * Creates a new filesystem directory.
     */
    @Unsafe
    public fun mkdir(path: String, mode: UInt, existOk: Boolean) {
        val result = mkdir(path, mode)
        if (result.isError) {
            if (errno == EEXIST && existOk) return
            else throwErrnoPath(errno, path)
        }
    }

    /**
     * Removes a filesystem directory.
     */
    @Unsafe
    public fun rmdir(path: String) {
        val result = platform.posix.rmdir(path)
        if (result.isError) {
            throwErrnoPath(errno, path)
        }
    }

    /**
     * Unlinks a symbolic file or deletes a file.
     */
    @Unsafe
    public fun unlink(path: String) {
        val result = platform.posix.unlink(path)
        if (result.isError) {
            throwErrnoPath(errno, path)
        }
    }

    /**
     * Gets the current working directory.
     */
    @Suppress("FoldInitializerAndIfToElvis")
    @Unsafe
    public fun getcwd(): ByteArray = memScoped {
        val buf = allocArray<ByteVar>(PATH_MAX)
        val res = getcwd(buf, PATH_MAX)
        if (res == null) {
            throwErrno(errno)
        }
        res.readZeroTerminated(PATH_MAX)
    }

    /**
     * Fully resolves a path into an absolute path.
     */
    @Suppress("FoldInitializerAndIfToElvis")
    @Unsafe
    public fun realpath(path: String): ByteString = memScoped {
        val buffer = allocArray<ByteVar>(PATH_MAX)
        val res = realpath(path, buffer)
        if (res == null) {
            throwErrnoPath(errno, path)
        }

        val ba = res.readZeroTerminated(PATH_MAX)
        return ByteString.fromUncopied(ba)
    }

    /**
     * Gets the real value of the symbolic link at [path].
     */
    @Unsafe
    public fun readlink(alloc: NativePlacement, path: String): ByteString {
        val buffer = alloc.allocArray<ByteVar>(PATH_MAX)
        val res = readlink(path, buffer, PATH_MAX)
        if (res.isError) {
            throwErrnoPath(errno, path)
        }

        val ba = buffer.readZeroTerminated(res.toInt())
        return ByteString.fromUncopied(ba)
    }

    /**
     * Renames a file or directory.
     */
    @Unsafe
    public fun rename(from: String, to: String) {
        val res = platform.posix.rename(from, to)
        // TODO: figure out error for ENOENT...
        if (res.isError) {
            throwErrno(errno)
        }
    }

    /**
     * Creates a new symlink at [linkpath] that points to [target].
     */
    @Unsafe
    public fun symlink(target: String, linkpath: String) {
        val res = platform.posix.symlink(target, linkpath)
        if (res.isError) {
            throwErrnoPath(errno, linkpath)
        }
    }

    // == Networking == //
    /**
     * Calls getaddrinfo(). You are responsible for calling freeaddrinfo() afterwards.
     */
    @Unsafe
    public fun getaddrinfo(
        alloc: NativePlacement,
        node: String?, service: String?,
        family: Int, type: Int, protocol: Int, flags: Int,
    ): addrinfo {
        val hints = alloc.alloc<addrinfo>()
        val res = alloc.allocPointerTo<addrinfo>()
        memset(hints.ptr, 0, sizeOf<addrinfo>().convert())

        hints.ai_flags = flags
        if (node == null) {
            hints.ai_flags = hints.ai_flags or AI_PASSIVE
        }
        hints.ai_socktype = type
        hints.ai_family = family
        hints.ai_protocol = protocol

        val code = getaddrinfo(node, service, hints.ptr, res.ptr)
        if (code < 0) {
            throw GAIException(errno = code, message = gai_strerror(code))
        }

        // safe (non-null) if this didn't error
        return res.pointed!!
    }

    /**
     * Frees an [addrinfo] object.
     */
    @Unsafe
    public fun freeaddrinfo(addrinfo: CPointer<addrinfo>) {
        platform.posix.freeaddrinfo(addrinfo)
    }

    /**
     * Creates a new socket and returns the file descriptor.
     */
    @Unsafe
    public fun socket(family: AddressFamily, type: SocketType, protocol: IPProtocol): FD {
        val sock = socket(family.number, type.number, protocol.number)
        if (sock.isError) {
            throwErrno(errno)
        }

        return sock
    }

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
            sin6_family = AF_INET6.toUShort()  // ?
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
            sin_family = AF_INET.toUShort()
            sin_addr.s_addr = ipRepresentation.toUInt()
            sin_port = htons(port.toUShort())
        }

        return struct.reinterpret()
    }

    /**
     * Connects a socket to an address.
     */
    @Unsafe
    public fun connect(sock: FD, address: ConnectionInfo): BlockingResult = memScoped {
        val struct: sockaddr
        val size: UInt

        when (address.family) {
            StandardAddressFamilies.AF_INET6 -> {
                val info = (address as InetConnectionInfo)
                struct = __ipv6_to_sockaddr(this, info.ip as IPv6Address, info.port)
                size = sizeOf<sockaddr_in6>().toUInt()
            }
            StandardAddressFamilies.AF_INET -> {
                val info = (address as InetConnectionInfo)
                struct = __ipv4_to_sockaddr(this, info.ip as IPv4Address, info.port)
                size = sizeOf<sockaddr_in>().toUInt()
            }
            else -> throw UnsupportedOperationException("Don't know how to use $address")
        }

        return when (val res = connect(sock, struct.ptr, size)) {
            0 -> BlockingResult.DIDNT_BLOCK
            ERROR -> {
                when (errno) {
                    EINTR, EINPROGRESS, EWOULDBLOCK -> BlockingResult.WOULD_BLOCK
                    else -> throwErrno(errno)
                }
            }
            else -> error("Unknown return value: $res")
        }
    }

    /**
     * Connects a socket to an address.
     *
     * The [timeout] param defines how long to wait when connecting, in milliseconds. A
     * reasonable default of 30 seconds is set.
     */
    @Unsafe
    public fun __connect_blocking(sock: FD, address: ConnectionInfo, timeout: Int = 30_000) {
        memScoped {
            // less than zero timeouts go on the fast path, which is a standard blocking connect
            if (timeout < 0) {
                val res = connect(sock, address)
                if (!res.isSuccess) {
                    throw UnsupportedOperationException(
                        "Attempted to do a blocking connect on a non-blocking socket"
                    )
                }

                return
            }

            // actual timeout, so we go onto the complicated path
            // set non-blocking so we can do timeouts
            val origin = fcntl(sock, FcntlParam.F_GETFL)
            fcntl(sock, FcntlParam.F_SETFL, origin.or(O_NONBLOCK))

            // actually run the connect
            val res = connect(sock, address)
            if (res.isSuccess) {
                // immediately succeeded, set blocking and return
                fcntl(sock, FcntlParam.F_SETFL, origin)
                return
            }

            // need to poll() until the socket is writeable
            val pollfd = allocArray<pollfd>(1)
            pollfd[0].fd = sock
            pollfd[0].events = (POLLOUT).toShort()

            // TODO: Add a proper timeout decrement on eintr.
            val pres = retry { poll(pollfd, 1, timeout) }
            if (pres.isError) {
                throwErrno(errno)
            } else if (pres == 0) {
                // socket timed out
                throw TimeoutException()
            }

            // success
            // don't bother checking pollfd because we only used one socket anyway so we know
            // which one succeeded
            // and we also gotta set the socket to blocking mode again
            fcntl(sock, FcntlParam.F_SETFL, origin)
        }
    }

    /**
     * File CoNTroL. Manipulates a file
     */
    @Unsafe
    public fun fcntl(fd: FD, option: FcntlParam<*>): Int {
        val res = fcntl(fd, option.number)
        if (res.isError) {
            throwErrno(errno)
        }
        return res
    }

    /**
     * Overloaded fcntl with one int parameter.
     */
    @Unsafe
    public fun fcntl(fd: FD, option: FcntlParam<Int>, p1: Int): Int {
        val res = fcntl(fd, option.number, p1)
        if (res.isError) {
            throwErrno(errno)
        }
        return res
    }

    /**
     * Overloaded fcntl with one pointer parameter.
     */
    @Unsafe
    public fun fcntl(fd: FD, option: FcntlParam<CPointer<*>>, p1: CPointer<*>): Int {
        val res = fcntl(fd, option.number, p1)
        if (res.isError) {
            throwErrno(errno)
        }
        return res
    }


    /**
     * Accepts a new connection from the specified socket. The [BlockingResult] returned will
     * contain the file descriptor.
     */
    @Unsafe
    public fun accept(sock: FD): BlockingResult {
        val accepted = retry { accept(sock, null, null) }
        if (accepted.isError) {
            when (errno) {
                EAGAIN -> Unit
                else -> throwErrno(errno)
            }
        }

        return BlockingResult(accepted.toLong())
    }

    /**
     * Binds a socket to an address.
     */
    @Unsafe
    public fun bind(sock: FD, address: ConnectionInfo) {
        val res = memScoped {
            val struct: sockaddr
            val size: UInt

            when (address.family) {
                StandardAddressFamilies.AF_INET6 -> {
                    val info = (address as InetConnectionInfo)
                    struct = __ipv6_to_sockaddr(this, info.ip as IPv6Address, info.port)
                    size = sizeOf<sockaddr_in6>().toUInt()
                }
                StandardAddressFamilies.AF_INET -> {
                    val info = (address as InetConnectionInfo)
                    struct = __ipv4_to_sockaddr(this, info.ip as IPv4Address, info.port)
                    size = sizeOf<sockaddr_in>().toUInt()
                }
                else -> error("Unknown or unsupported address family: ${address.family}")
            }

            bind(sock, struct.ptr, size)
        }

        if (res.isError) {
            throwErrno(errno)
        }
    }

    /**
     * Marks a socket as "passive" (a listening socket) with the specified backlog of queued
     * requests.
     */
    @Unsafe
    public fun listen(sock: FD, backlog: Int) {
        val res = platform.posix.listen(sock, backlog)
        if (res.isError) {
            throwErrno(errno)
        }
    }

    /**
     * Receives bytes from a socket into the specified buffer.
     */
    @Unsafe
    public fun recv(
        fd: FD,
        buffer: ByteArray,
        size: Int = buffer.size, offset: Int = 0,
        flags: Int = 0,
    ): BlockingResult {
        require(size <= IO_MAX) { "$size is more than IO_MAX" }
        require(buffer.size >= size) { "$size is more than buffer size (${buffer.size})" }
        require(offset + size <= buffer.size) {
            "offset ($offset) + size ($size) > buffer.size (${buffer.size})"
        }

        val read = buffer.usePinned {
            retry { recv(fd, it.addressOf(offset), size.toULong(), flags) }
        }

        if (read.isError) {
            if (errno == EAGAIN) return BlockingResult.WOULD_BLOCK
            throwErrno(errno)
        }

        return BlockingResult(read)
    }

    // Eww!
    /**
     * Receives bytes from a socket into the specified buffer, returning the number of bytes read as
     * well as the
     */
    @Unsafe
    public fun <I: ConnectionInfo> recvfrom(
        fd: FD,
        buffer: ByteArray,
        size: Int = buffer.size, offset: Int = 0,
        flags: Int = 0,

        /* extra flags for address creation */
        creator: ConnectionInfoCreator<I>
    ): RecvFrom<I>? = memScoped {
        require(size <= IO_MAX) { "$size is more than IO_MAX" }
        require(buffer.size >= size) { "$size is more than buffer size (${buffer.size})" }
        require(offset + size <= buffer.size) {
            "offset ($offset) + size ($size) > buffer.size (${buffer.size})"
        }

        val addr = alloc<sockaddr_storage>().reinterpret<sockaddr>()
        val sizePtr = alloc<UIntVar>()
        sizePtr.value = sizeOf<sockaddr_storage>().toUInt()

        val read = buffer.usePinned {
            retry {
                recvfrom(fd, it.addressOf(offset), size.toULong(), flags, addr.ptr, sizePtr.ptr)
            }
        }

        if (read.isError) {
            if (errno != EAGAIN) throwErrno(errno)
            else return null
        }

        val kAddr = addr.toKotlin()
            ?: error("null addr")

        return RecvFrom(BlockingResult(read), creator.from(kAddr.first, kAddr.second))
    }

    /**
     * Writes bytes to a socket from the specified buffer.
     */
    @Unsafe
    public fun send(
        fd: FD, buffer: ByteArray, size: Int = buffer.size, offset: Int = 0,
        flags: Int
    ): BlockingResult {
        require(size <= IO_MAX) { "$size is more than IO_MAX" }
        require(buffer.size >= size) { "$size is more than buffer size (${buffer.size})" }
        require(offset + size <= buffer.size) {
            "offset ($offset) + size ($size) > buffer.size (${buffer.size})"
        }

        // copied directly from write()

        var nextOffset = offset
        var hitAgain = false

        // head spinny logic
        buffer.usePinned {
            while (true) {
                val written = send(
                    fd, it.addressOf(nextOffset),
                    (size - nextOffset).toULong(),
                    flags
                )

                // eintr means it didn't write anything, so we can transparently retry
                if (written.isError) {
                    when (errno) {
                        EINTR -> continue
                        EAGAIN -> {
                            hitAgain = true
                            break
                        }
                        else -> throwErrno(errno)
                    }
                }

                // make sure we actually write all of the bytes we want to write
                // this will never be more than INT_MAX, so we're fine
                nextOffset += written.toInt()
                if (nextOffset >= size) {
                    break
                }
            }
        }

        // if we hit EAGAIN, and nextOffset is 0, we didn't write anything
        return if (hitAgain && nextOffset == 0) {
            BlockingResult.WOULD_BLOCK
        } else {
            BlockingResult(nextOffset.toLong())
        }
    }

    /**
     * Writes bytes to a socket from the specified buffer.
     */
    @Unsafe
    public fun <I> sendto(
        fd: FD, buffer: ByteArray, size: Int, offset: Int, flags: Int,
        address: I,
    ): BlockingResult = memScoped {
        val struct: sockaddr
        val addrSize: UInt

        when (address) {
            is InetConnectionInfo -> {
                when (val ip = address.ip) {
                    is IPv6Address -> {
                        struct = __ipv6_to_sockaddr(this, ip, address.port)
                        addrSize = sizeOf<sockaddr_in6>().toUInt()
                    }
                    is IPv4Address -> {
                        struct = __ipv4_to_sockaddr(this, ip, address.port)
                        addrSize = sizeOf<sockaddr_in>().toUInt()
                    }
                }
            }
            else -> throw IllegalArgumentException(
                "Don't know how to translatee $address into a sockaddr"
            )
        }

        val result = buffer.usePinned {
            retry {
                sendto(fd, it.addressOf(offset), size.toULong(), flags, struct.ptr, addrSize)
            }
        }
        if (result.isError) {
            return if (errno == EAGAIN) BlockingResult.WOULD_BLOCK
            else throwErrno(errno)
        }

        return BlockingResult(result)
    }

    // These are both really gross!
    // Consider making a better API, future me!
    /**
     * Sets the socket [option] to the [value] provided.
     */
    @Unsafe
    public fun <T> setsockopt(sock: FD, option: BsdSocketOption<T>, value: T): Unit = memScoped {
        val native = option.toNativeStructure(this, value)
        val size = option.nativeSize()
        val err = setsockopt(sock, option.level, option.bsdOptionValue, native, size.toUInt())
        if (err.isError) {
            throwErrno(errno)
        }
    }

    /**
     * Gets a socket option.
     */
    @Unsafe
    public fun <T> getsockopt(sock: FD, option: BsdSocketOption<T>): T = memScoped {
        val storage = option.allocateNativeStructure(this)
        val size = cValuesOf(option.nativeSize().toUInt())
        val res = getsockopt(
            sock, option.level, option.bsdOptionValue,
            storage, size
        )
        if (res.isError) {
            throwErrno(errno)
        }

        option.fromNativeStructure(this, storage)
    }

    /**
     * Shuts down part of a fully duplex connection.
     */
    @Unsafe
    public fun shutdown(sock: FD, how: ShutdownOption) {
        val res = shutdown(sock, how.number)
        if (res.isError) {
            throwErrno(errno)
        }
    }

    // == Misc == //
    // region Misc

    /**
     * Gets the current user ID.
     */
    @Unsafe
    public fun getuid(): UInt {
        return platform.posix.getuid()
    }

    /**
     * Gets a [passwd] entry for the specified uid.
     */
    @Unsafe
    public fun getpwuid_r(alloc: NativePlacement, uid: UInt): passwd? {
        val passwd = alloc.alloc<passwd>()
        val starResult = alloc.allocPointerTo<passwd>()

        var bufSize = sysconf(_SC_GETPW_R_SIZE_MAX)
        if (bufSize == -1L) bufSize = 16384
        val buffer = alloc.allocArray<ByteVar>(bufSize)

        @Suppress("UNUSED_VARIABLE")
        val res = getpwuid_r(uid, passwd.ptr, buffer, bufSize.toULong(), starResult.ptr)
        if (starResult.value == null) {
            throwErrno(errno)
        }

        return passwd
    }

    // endregion
}
