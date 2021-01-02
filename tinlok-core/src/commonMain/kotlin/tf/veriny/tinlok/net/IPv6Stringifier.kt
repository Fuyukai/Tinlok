/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.net

import tf.veriny.tinlok.util.hexlify
import tf.veriny.tinlok.util.toByteString

// as this is the less common operation, i use *much* slower code stuff here, but it makes the code
// a lot more concise.
/**
 * Stringifies an IPv6 address into either a correct or canonical address.
 */
public class IPv6Stringifier(public val address: ByteArray) {
    init {
        require(address.size == 16) { "IPv6 addresses must be 16 bytes in size" }
    }

    /**
     * Encodes the bytes of this address into a list of hexadecimal parts.
     */
    public fun parts(): List<String> {
        return address.toList().chunked(2).map { it.toByteString().hexlify() }
    }

    /**
     * Encodes the bytes of this address in the canonical (fully expanded) format.
     */
    public fun canonical(): String {
        return parts().joinToString(":")
    }

    /**
     * Encodes the bytes of this address in the correct (unexpanded) format.
     */
    public fun correct(): String {
        // unmodified, fully expanded parts
        val initialParts = parts()

        // the final list of unexpanded parts, the 0s will be nuked eventually
        val shrunkenParts = initialParts.map {
            if (it == "0000") "0"
            else it.trimStart('0')
        }

        // various counters
        // the length of the best run of 0s we've seen so far
        var bestRunLen = 0
        // the index of the best run of 0s we've seen so far
        var bestRunIdx = -1

        // the length of the current run of 0s
        var currentRunLen = 0
        // the index of the current run of 0s
        var currentRunIdx = -1

        for ((idx, part) in initialParts.withIndex()) {
            if (part == "0000") {
                // found all zeroes, let's see how the run is going
                if (currentRunIdx == -1) {
                    // -1 means this is the start of the current run
                    currentRunIdx = idx
                    currentRunLen = 1
                } else {
                    // not the start of the run, add up to the tally
                    currentRunLen += 1
                }
                // now check to see if it's better than the best run
                // we always check better so that the left-mode run of 0s get
                if (currentRunLen > bestRunLen) {
                    // swap over the values
                    // this is ok even when the idx is the same
                    bestRunIdx = currentRunIdx
                    bestRunLen = currentRunLen
                }
            } else {
                // end the current run always
                currentRunIdx = -1
                currentRunLen = 0
            }
        }

        // now that the loop is over, we can check if there's a run worth turning into ::
        if (bestRunLen <= 1) {
            // no runs of more than one zero, return removing trailing zeroes but otherwise
            // unchanged
            return shrunkenParts.joinToString(":")
        }

        // This took about fifteen minutes of pacing for me to figure out a tolerable solution for.
        val output = StringBuilder()
        var counter = 0

        // First, if the list STARTS with the run of zeroes, we start the output with ::
        // and the counter off at the length.
        if (bestRunIdx == 0) {
            output.append("::")
            counter = bestRunIdx
        }

        // Now we enter the writing loop.
        while (true) {
            // Option 1: The current index is after the last item.
            if (counter >= 8) {
                break
            }

            // Option 2: The current index is at the last item.
            if (counter == 7) {
                val item = shrunkenParts[counter]
                output.append(item)
                // No trailing colon; directly escape.
                break
            }

            // Option 3: Current index is the index directly before the run of zeroes.
            // In this case, the list never started with it, so there's no ::, so we can safely
            // add it on after this one.
            if (counter == bestRunIdx - 1) {
                val item = shrunkenParts[counter]
                output.append(item)
                output.append("::")
                counter += 1
                continue
            }

            // Option 4: Current index is contained within the run of zeroes. We skip to the end of
            // the run and continue onwards.
            if (counter in bestRunIdx until (bestRunIdx + bestRunLen)) {
                counter = bestRunIdx + bestRunLen
                continue
            }

            // Option 5: None of the above, so we just add the item and the trailing colon.
            val item = shrunkenParts[counter]
            output.append(item)
            output.append(":")
            counter += 1
        }

        return output.toString()
    }
}
