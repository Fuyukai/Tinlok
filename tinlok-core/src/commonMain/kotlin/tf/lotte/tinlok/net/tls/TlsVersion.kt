/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.tls

/**
 * An enumeration of supported TLS versions.
 */
public enum class TlsVersion(public val number: Int) {
    /** TLS version 1.2. */
    TLS_V12(0x0303),
    /** TLS version 1.3. */
    TLS_V13(0x0304),
    ;
}
