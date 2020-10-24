/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.cc

/**
 * Marks an API as "unsafe". Unsafe APIs cannot be used from safe APIs without explicit opt-in.
 */
@RequiresOptIn(
    message = "This API is unsafe and requires explicit opt-in.",
    level = RequiresOptIn.Level.ERROR
)
public annotation class Unsafe

/**
 * Marks an API as experimental, subject to change.
 */
@RequiresOptIn(
    message = "This API is experimental and should be explicitly opted into.",
    level = RequiresOptIn.Level.WARNING
)
public annotation class Experimental