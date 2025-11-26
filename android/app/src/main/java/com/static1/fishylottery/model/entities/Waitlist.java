package com.static1.fishylottery.model.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * Maintains a list of unique WaitlistEntry objects.
 */
public class Waitlist {
    private final List<WaitlistEntry> entries = new ArrayList<>();

    /**
     * Adds the provided entry if it does not already exist in the list.
     *
     * @param entry to add
     * @throws IllegalArgumentException if entry already exists
     */
    public void add(WaitlistEntry entry) {
        if (entries.contains(entry)) {
            throw new IllegalArgumentException("City already exists: " + entry);
        }
        entries.add(entry);
    }

    /**
     * Check whether the provided entry exists in the list.
     *
     * @param entry the entry to check
     * @return true if the entry exists in the list, false otherwise
     */
    public boolean hasEntry(WaitlistEntry entry) {
        return entries.contains(entry);
    }

    /**
     * Deletes the provided entry from the list if present.
     *
     * @param entry the entry to delete
     * @throws IllegalArgumentException if the city does not exist in the list
     */
    public void delete(WaitlistEntry entry) {
        boolean removed = entries.remove(entry);
        if (!removed) {
            throw new IllegalArgumentException("Entry not found: " + entry);
        }
    }

    /**
     * Return the number of entries currently in the list.
     *
     * @return count of entries
     */
    public int countEntries() {
        return entries.size();
    }
}

