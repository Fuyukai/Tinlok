/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.knste.net

/**
 * Actual enum (with POSIX numbers) for StandardAddressFamilies, with Linux-specific families
 * available.
 */
public actual enum class AddressFamily(public actual val number: Int) {
    AF_UNSPEC(platform.posix.AF_UNSPEC),
    AF_INET(platform.posix.AF_INET),
    AF_INET6(platform.posix.AF_INET6),

    AF_IPX(platform.posix.AF_IPX),
    AF_SNA(platform.posix.AF_SNA),
    AF_APPLETALK(platform.posix.AF_APPLETALK),
    AF_UNIX(platform.posix.AF_UNIX)
    ;
}
