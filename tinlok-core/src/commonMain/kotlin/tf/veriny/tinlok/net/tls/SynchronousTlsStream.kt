/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.net.tls

import tf.veriny.tinlok.Unsafe
import tf.veriny.tinlok.io.BidirectionalStream
import tf.veriny.tinlok.io.Buffer
import tf.veriny.tinlok.net.socket.Socket
import tf.veriny.tinlok.net.socket.happyEyeballsTcpConnect
import tf.veriny.tinlok.net.tcp.TcpConnectionInfo
import tf.veriny.tinlok.net.tcp.TcpSocketAddress
import tf.veriny.tinlok.system.ensureNonBlock
import tf.veriny.tinlok.util.ClosingScope
import tf.veriny.tinlok.util.ClosingScopeImpl
import tf.veriny.tinlok.util.use

/**
 * A [BidirectionalStream] that uses TLS. This wraps a [Socket] and a [TlsObject], funneling data to
 * and from the socket.
 *
 * The passed-in [TlsObject] MUST have had the handshake already performed; things will fail if
 * this is not the case!
 */
public class SynchronousTlsStream
@Unsafe public constructor(
    private val sock: Socket<TcpConnectionInfo>,
    public val tls: TlsObject,
) : BidirectionalStream {
    public companion object {
        /**
         * Opens a new [SynchronousTlsStream], and performs the TLS handshake from a client
         * perspective.
         */
        @Unsafe
        public fun openClientHandshake(
            tls: TlsObject,
            sock: Socket<TcpConnectionInfo>,
        ): SynchronousTlsStream {
            require(tls.context.config is TlsClientConfig) {
                "This method requires a client-sided socket"
            }

            val wrapper = SynchronousTlsStream(sock, tls)
            while (true) {
                // force the next step in the handshake, which may have underlying data
                val isDone = tls.handshake()
                if (isDone) break
                // since we're on the client, we write the outgoing data first, as we start the
                // handshake.
                wrapper.drainOutgoing()
                // then we read any incoming data, such as server acks
                wrapper.readIncoming()
            }

            return wrapper
        }

        /**
         * Performs an unsafe connect, returning a [SynchronousTlsStream].
         */
        @Unsafe
        public fun unsafeConnect(
            context: TlsContext,
            address: TcpSocketAddress,
        ): SynchronousTlsStream {
            require(address.hostname != null) { "Address must have a hostname" }

            return ClosingScope {
                val socket = happyEyeballsTcpConnect(address)
                it.add(socket)

                val tls = TlsObject(context, address.hostname)
                it.add(tls)

                val stream = openClientHandshake(tls, socket)
                // if that didnt fail, we have a stream and can remove the objects from
                // our enclosing scope, so that they don't get closed.
                it.remove(socket)
                it.remove(tls)

                stream
            }

        }

        /**
         * Opens a new [SynchronousTlsStream] to the specified [address], using [context], passing
         * it to the specified [block].
         */
        @OptIn(Unsafe::class)
        public inline fun <R> connect(
            context: TlsContext,
            address: TcpSocketAddress, block: (SynchronousTlsStream) -> R,
        ): R {
            val stream = unsafeConnect(context, address)
            return stream.use(block)
        }

        /**
         * Opens a new [SynchronousTlsStream] to the specified [address], using [context], adding it
         * to the specified [scope], and returning it.
         */
        @OptIn(Unsafe::class)
        public fun connect(
            scope: ClosingScope, context: TlsContext, address: TcpSocketAddress,
        ): SynchronousTlsStream {
            val stream = unsafeConnect(context, address)
            scope.add(stream)
            return stream
        }
    }

    @OptIn(Unsafe::class)
    private val scope = ClosingScopeImpl()

    init {
        scope.add(sock)
        scope.add(tls)
    }

    override fun close() {
        scope.close()
    }

    /**
     * Drains the outgoing BIO from the TLS object, returning the number of bytes written to the
     * socket.
     */
    @OptIn(Unsafe::class)
    private fun drainOutgoing(working: ByteArray = ByteArray(4096)): Int {
        var written: Int = 0
        while (tls.outgoingPending() > 0L) {
            val count = tls.outgoing(buf = working, count = working.size, offset = 0)
            // this will always send all the data, or throw a broken pipe next time around
            // so i don't care to track the return of this.
            sock.sendall(buf = working, size = count, offset = 0, flags = 0)
            written += count
        }

        return written
    }

    /**
     * Reads incoming data from the socket and writes it to the incoming BIO.
     */
    @OptIn(Unsafe::class)
    private fun readIncoming(working: ByteArray = ByteArray(4096)): Int {
        val count = sock.recv(working, working.size, 0, 0).ensureNonBlock()
        tls.incoming(working, count.toInt(), 0)
        return count.toInt()
    }

    /**
     * Writes out un-encrypted bytes from [ba].
     */
    @OptIn(Unsafe::class)
    override fun writeFrom(ba: ByteArray, size: Int, offset: Int): Int {
        // I believe there is no way for this to return less than size, as memory BIOs grow forever.
        val res = tls.write(buf = ba, count = size, offset = offset)
        check(res.isSuccess) { "Failed to write to in-memory buffers?" }

        return drainOutgoing()
    }

    /**
     * Writes out un-encrypted bytes from [buffer].
     */
    override fun writeFrom(buffer: Buffer, size: Int): Int {
        // no memory support! (yet)
        val arr = buffer.readArray(size)
        return writeFrom(arr, size, 0)
    }

    /**
     * Reads in decrypted bytes into [ba].
     */
    @OptIn(Unsafe::class)
    override fun readInto(ba: ByteArray, size: Int, offset: Int): Int {
        // this is more complex than the outgoing method...
        // since when we actually read in, there could be non-application record data, or a
        // truncated record. so we need a retry loop until SSL_read returns an actual count.

        // first we check if the BIO has any data, because if it does we might not need to recv.
        // this can happen if the last read contained two records somehow.
        if (tls.incomingPending() > 0L) {
            // try an SSL read, and return that
            val read = tls.read(ba, size, offset)
            if (read.isSuccess) {
                // read decrypted data successfully,
                return read.count.toInt()
            }
        }

        // maybe the TLS object has consumed some stuff from the BIO. try and read before
        // entering the retry loop, to avoid reading from the socket if we have data pending.
        val initialOut = tls.read(ba, size, offset)
        if (initialOut.isSuccess) {
            // we DID have data in the buffers. return the read out application data.
            return initialOut.count.toInt()
        }

        // no pending data, so we enter a retry loop until openssl has given us the heads up
        // that there's a decrypted record to read.

        // we allocate our own buffer for incoming data, as we read the decrypted data into ba,
        // so it can't be used (properly) for the encrypted application records.
        val incoming = ByteArray(4096)

        while (true) {
            val count = sock.recv(
                buf = incoming, size = 4096, offset = 0, flags = 0
            ).ensureNonBlock()
            // This should equally never be less than count() bytes.
            tls.incoming(incoming, count.toInt(), 0)

            val readOut = tls.read(ba, size, offset)

            // see if we need to read more, if so that means there was no record
            if (!readOut.isSuccess) continue
            // success!
            return readOut.count.toInt()
        }
    }

    override fun readInto(buffer: Buffer, size: Int): Int {
        // equally no memory support (yet)
        val ba = ByteArray(size)
        val amount = readInto(ba, size)
        buffer.writeFrom(ba, amount, 0)
        return amount
    }
}
