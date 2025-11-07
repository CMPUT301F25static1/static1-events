package com.static1.fishylottery.model.logic;

import org.junit.Test;
import java.util.Collections;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;

public class DrawSelectorTest {

    @Test
    public void selectsExactlyN_whenNLessThanSize() {
        List<String> ids = Arrays.asList("A","B","C","D","E");
        List<String> out = DrawSelector.select(ids, 3, 12345L);
        assertEquals(3, out.size());
        assertEquals(out.size(), new HashSet<>(out).size()); // unique
        assertTrue(ids.containsAll(out));
    }

    @Test
    public void capsAtListSize_whenNExceedsSize() {
        List<String> ids = Arrays.asList("X","Y");
        List<String> out = DrawSelector.select(ids, 5, 999L);
        assertEquals(2, out.size());
        assertTrue(ids.containsAll(out));
    }

    @Test
    public void deterministicWithSeed_sameSeedSameOrder() {
        List<String> ids = Arrays.asList("1","2","3","4","5","6");
        List<String> out1 = DrawSelector.select(ids, 4, 42L);
        List<String> out2 = DrawSelector.select(ids, 4, 42L);
        assertEquals(out1, out2);
    }

    @Test
    public void zeroN_returnsEmpty() {
        List<String> ids = Arrays.asList("A","B","C");
        List<String> out = DrawSelector.select(ids, 0, 123L);
        assertTrue(out.isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeN_throws() {
        DrawSelector.select(Collections.singletonList("A"), -1, 7L);
    }
}
