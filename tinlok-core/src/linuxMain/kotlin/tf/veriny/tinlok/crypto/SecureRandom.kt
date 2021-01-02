/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.crypto

import platform.posix.O_RDONLY
import tf.veriny.tinlok.Unsafe
import tf.veriny.tinlok.system.Syscall
import tf.veriny.tinlok.util.b

/**
 * Linux implementation of the SecureRandom class.
 */
public actual object SecureRandom : RandomShared() {
    // it's ok for this to leak, as we will never close it
    @OptIn(Unsafe::class)
    private val fd = Syscall.open(b("/dev/urandom"), O_RDONLY)

    @OptIn(Unsafe::class)
    override fun readBytesImpl(buf: ByteArray) {
        Syscall.read(fd, buf, buf.size)
    }

}
