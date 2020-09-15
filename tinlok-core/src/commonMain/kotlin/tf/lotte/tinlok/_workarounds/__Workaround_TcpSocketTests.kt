/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok._workarounds

/**
 * Native-implemented TCP socket tests that are called by the real test class.
 */
internal expect object __Workaround_TcpSocketTests {
    fun testSocketAccept()
}
