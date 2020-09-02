/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KSTE.
 *
 * KSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.kste

/**
 * KSTE version holder class.
 */
public class KSTEVersion(public val major: Int, public val minor: Int, public val patch: Int) {
    public companion object {
        /** The current version. */
        public val CURRENT: KSTEVersion = KSTEVersion(1, 0, 0)
    }
}
