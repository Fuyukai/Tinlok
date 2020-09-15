/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.dns

/**
 * The global address resolver. This is used by default for all resolution requests without an
 * explicit resolver.
 */
public expect object GlobalResolver : AddressResolver
