package com.hanziflow.shared.testgen

/**
 * Deterministic, cross-platform PRNG (SplitMix32-style xorshift on Int).
 *
 * Deliberately NOT built on kotlin.random.Random (whose output is not
 * guaranteed byte-identical across JVM/JS implementations in the way this
 * module needs) and never relies on hashCode()/HashMap/HashSet iteration
 * order, which can differ between the JVM and JS targets.
 */
class Rng(seed: Int) {
    private var state: Int = seed

    private fun nextRawInt(): Int {
        state += GOLDEN_GAMMA
        var z = state
        z = (z xor (z ushr 16)) * MIX1
        z = (z xor (z ushr 13)) * MIX2
        z = z xor (z ushr 16)
        return z
    }

    /** Returns an Int uniformly distributed in [0, bound). Requires bound > 0. */
    fun nextInt(bound: Int): Int {
        require(bound > 0) { "bound must be positive, was $bound" }
        val raw = nextRawInt() and 0x7fffffff
        return raw % bound
    }

    fun nextBoolean(): Boolean = nextInt(2) == 0

    /** Deterministic Fisher-Yates shuffle; does not mutate [list]. */
    fun <T> shuffled(list: List<T>): List<T> {
        val result = list.toMutableList()
        for (i in result.size - 1 downTo 1) {
            val j = nextInt(i + 1)
            val tmp = result[i]
            result[i] = result[j]
            result[j] = tmp
        }
        return result
    }

    fun <T> pick(list: List<T>): T = list[nextInt(list.size)]

    companion object {
        private const val GOLDEN_GAMMA = -0x61c88647
        private const val MIX1 = -0x7ee3623b
        private const val MIX2 = -0x3d4d51cb
    }
}

/**
 * Deterministic FNV-1a 32-bit hash of a string. Used instead of String.hashCode()
 * so the derived seed is explicit, documented, and identical by construction on
 * every Kotlin target (we don't want to depend on stdlib's cross-platform
 * guarantees holding indefinitely for this course's determinism requirement).
 */
fun stableStringHash(value: String): Int {
    var hash = -0x7ee3623b // arbitrary non-zero seed, must stay fixed across releases
    for (ch in value) {
        hash = hash xor ch.code
        hash *= FNV_PRIME
    }
    return hash
}

private const val FNV_PRIME = 0x01000193

/**
 * Derives a child seed from a parent seed and an ordered list of string/int salts
 * (e.g. item id, question type, attempt index). Same inputs always produce the
 * same seed, on every target.
 */
fun deriveSeed(seed: Int, vararg salts: Any): Int {
    var h = seed
    for (s in salts) {
        val salt = when (s) {
            is Int -> s
            is String -> stableStringHash(s)
            else -> error("unsupported salt type for deriveSeed: $s")
        }
        h = (h xor salt) * -0x61c88647
        h = h xor (h ushr 15)
    }
    return h
}
