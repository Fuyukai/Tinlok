/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.collection


/**
 * Defines an ordered hash set that uses object identity rather than ``Any#hashCode`` for objects.
 * This is used for e.g. closing scopes which need to keep track of objects by their identity,
 * rather by their equality, as two different objects may hold different handles but may be
 * equal to each-other.
 *
 * Warning: This may box the values in a wrapper value. This should be avoided for
 * performance-sensitive code.
 */
public expect class IdentitySet<E> public constructor() : MutableSet<E>
