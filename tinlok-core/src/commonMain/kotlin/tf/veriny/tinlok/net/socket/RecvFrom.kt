/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.net.socket

import tf.veriny.tinlok.net.ConnectionInfo
import tf.veriny.tinlok.system.BlockingResult

/**
 * Wraps the result of a recvfrom() socket call. This is a data class to prevent boxing with
 * [BlockingResult].
 */
public data class RecvFrom<I : ConnectionInfo>(
    public val result: BlockingResult,
    public val address: I,
)
