/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.knste.util

/**
 * Marks an API as "unsafe". Unsafe APIs cannot be used from safe APIs
 */
@RequiresOptIn(
    message = "This API is unsafe and requires explicit opt-in.",
    level = RequiresOptIn.Level.ERROR
)
public annotation class Unsafe
