/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.crypto

import kotlin.native.concurrent.AtomicReference

@OptIn(ExperimentalUnsignedTypes::class)
public actual interface CryptographyProvider {
    public actual companion object {
        private val provider = AtomicReference<CryptographyProvider>(LibsodiumProvider())

        /**
         * Gets the current [CryptographyProvider].
         */
        public actual fun current(): CryptographyProvider = provider.value

        /**
         * Sets the current [CryptographyProvider].
         */
        public actual fun change(provider: CryptographyProvider) { this.provider.value = provider }
    }

    // expect-actual for interfaces is very very stupid.
    // if you got here via C-b, I'm so sorry. Check the actual class in the common module
    // for the doc-strings. Bug jetbrains to fix the fucking jump to definition behaviour for
    // expect-actual or at the very least add the gutter icons outside of the project
    public actual val defaultIntegrityHashAlgorithm: String
    public actual fun getIntegrityHasher(algorithm: String): IntegrityHasher?
    public actual fun secureCompare(first: UByteArray, second: UByteArray): Boolean
}
