/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.knste.fs

import platform.posix.*

/**
 * A POSIX file permission (rwxrwxrwx).
 */
public enum class PosixFilePermission(public val bit: Int) : FilePermission {
    OWNER_READ(S_IRUSR),
    OWNER_WRITE(S_IWUSR),
    OWNER_EXECUTE(S_IXUSR),
    OWNER_RW(S_IRUSR or S_IWUSR),
    OWNER_ALL(S_IRWXU),

    GROUP_READ(S_IRGRP),
    GROUP_WRITE(S_IWGRP),
    GROUP_EXECUTE(S_IXGRP),
    GROUP_RW(S_IRGRP or S_IWGRP),
    GROUP_RX(S_IRGRP or S_IXGRP),
    GROUP_ALL(S_IRWXG),

    ALL_READ(S_IROTH),
    ALL_WRITE(S_IWOTH),
    ALL_EXECUTE(S_IXOTH),
    ALL_RW(S_IROTH or S_IWOTH),
    ALL_RX(S_IROTH or S_IXOTH),
    OTHER_ALL(S_IRWXO),

    // 666, usually umasked to 644
    DEFAULT_FILE(OWNER_RW.bit or GROUP_RW.bit or ALL_RW.bit),
    // 777, usually umasked to 755
    DEFAULT_DIRECTORY(OWNER_ALL.bit or GROUP_ALL.bit or OTHER_ALL.bit),

    ALL(S_IRWXU or S_IRWXG or S_IRWXO)
    ;
}

internal infix fun PosixFilePermission.or(other: PosixFilePermission) = bit or other.bit
internal infix fun Int.or(other: PosixFilePermission) = this or other.bit
internal infix fun PosixFilePermission.or(other: Int) = bit or other
