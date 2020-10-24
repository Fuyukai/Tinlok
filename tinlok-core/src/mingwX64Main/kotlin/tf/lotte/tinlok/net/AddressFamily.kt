/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net

/**
 * An enumeration of the available address families.
 */
public actual enum class AddressFamily(public actual val number: Int) {
    /** Unspecified address family. */
    AF_UNSPEC(platform.windows.AF_UNSPEC),

    /** IPv4 */
    AF_INET(platform.windows.AF_INET),

    /** IPv6 */
    AF_INET6(23 /* platform.windows.AF_INET6 */),

    /** Unix pipes */
    AF_UNIX(platform.windows.AF_UNIX),
    ;

}