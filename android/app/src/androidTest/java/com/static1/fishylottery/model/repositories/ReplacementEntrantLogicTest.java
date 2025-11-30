package com.static1.fishylottery.model.repositories;

import static org.junit.Assert.*;

import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.model.entities.Profile;
import com.static1.fishylottery.model.entities.WaitlistEntry;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for the replacement-entrant logic implemented in FakeWaitlistRepository.
 * These are pure-Java tests (no Android, no Firestore).
 */
public class ReplacementEntrantLogicTest {

    @Test
    public void invitedDeclines_waitingEntrantIsPromoted() {
        // ----- Arrange -----
        Event event = new Event();
        event.setEventId("event1");

        // Invited entrant (the one who will decline)
        Profile invitedProfile = new Profile();
        invitedProfile.setUid("user_invited");
        WaitlistEntry invitedEntry = new WaitlistEntry();
        invitedEntry.setProfile(invitedProfile);
        invitedEntry.setStatus("invited");

        // Waiting entrant (should be promoted)
        Profile waitingProfile = new Profile();
        waitingProfile.setUid("user_waiting");
        WaitlistEntry waitingEntry = new WaitlistEntry();
        waitingEntry.setProfile(waitingProfile);
        waitingEntry.setStatus("waiting");

        Map<String, List<WaitlistEntry>> initial = new HashMap<>();
        initial.put("event1", Arrays.asList(invitedEntry, waitingEntry));

        FakeWaitlistRepository repo = new FakeWaitlistRepository(initial);

        // ----- Act -----
        // In the fake repo this completes synchronously.
        repo.declineInvitationAndDrawReplacement(event, "user_invited");

        // ----- Assert -----
        List<WaitlistEntry> updated = repo.getWaitlist(event).getResult();
        WaitlistEntry updatedInvited = findByUid(updated, "user_invited");
        WaitlistEntry updatedWaiting = findByUid(updated, "user_waiting");

        assertNotNull(updatedInvited);
        assertNotNull(updatedWaiting);

        // Declining invited entrant becomes "declined"
        assertEquals("declined", updatedInvited.getStatus());
        // One waiting entrant is promoted to "invited"
        assertEquals("invited", updatedWaiting.getStatus());
    }

    @Test
    public void waitingDeclines_noPromotionOccurs() {
        // This checks the branch where previousStatus != "invited":
        // the declining entrant becomes "declined", but no reroll happens.

        Event event = new Event();
        event.setEventId("event1");

        Profile p1 = new Profile();
        p1.setUid("user1");
        WaitlistEntry e1 = new WaitlistEntry();
        e1.setProfile(p1);
        e1.setStatus("waiting");

        Profile p2 = new Profile();
        p2.setUid("user2");
        WaitlistEntry e2 = new WaitlistEntry();
        e2.setProfile(p2);
        e2.setStatus("waiting");

        Map<String, List<WaitlistEntry>> initial = new HashMap<>();
        initial.put("event1", Arrays.asList(e1, e2));

        FakeWaitlistRepository repo = new FakeWaitlistRepository(initial);

        // user1 is only "waiting", not invited
        repo.declineInvitationAndDrawReplacement(event, "user1");

        List<WaitlistEntry> updated = repo.getWaitlist(event).getResult();
        WaitlistEntry updated1 = findByUid(updated, "user1");
        WaitlistEntry updated2 = findByUid(updated, "user2");

        assertNotNull(updated1);
        assertNotNull(updated2);

        // Declining waiting entrant becomes declined
        assertEquals("declined", updated1.getStatus());
        // The other waiting entrant is not promoted
        assertEquals("waiting", updated2.getStatus());
    }

    private WaitlistEntry findByUid(List<WaitlistEntry> list, String uid) {
        for (WaitlistEntry e : list) {
            if (e != null && e.getProfile() != null &&
                    uid.equals(e.getProfile().getUid())) {
                return e;
            }
        }
        return null;
    }
}
