package com.static1.fishylottery.model.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Pure-Java selector used for unit testing the draw rule. */
public final class DrawSelector {
    private DrawSelector() {}

    /** Select up to n unique IDs using the provided RNG (deterministic in tests). */
    public static List<String> select(List<String> entrantIds, int n, Random rng) {
        if (entrantIds == null) throw new IllegalArgumentException("entrantIds is null");
        if (n < 0) throw new IllegalArgumentException("n must be >= 0");

        List<String> copy = new ArrayList<>(entrantIds);   // don’t mutate caller
        Collections.shuffle(copy, rng);
        int limit = Math.min(n, copy.size());
        return new ArrayList<>(copy.subList(0, limit));
    }

    /** Convenience overload with a fixed seed for reproducible tests. */
    public static List<String> select(List<String> entrantIds, int n, long seed) {
        return select(entrantIds, n, new Random(seed));
    }
}


