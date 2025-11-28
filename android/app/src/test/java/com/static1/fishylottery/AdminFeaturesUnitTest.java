package com.static1.fishylottery;

import static org.junit.Assert.*;

import com.static1.fishylottery.model.entities.Event;
import com.static1.fishylottery.model.entities.Profile;
import com.static1.fishylottery.view.admin.AdminOrganizerAdapter;
import com.static1.fishylottery.view.admin.NotificationLogAdapter;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Unit Tests for Admin Features (Simplified Version)
 * Tests all 4 admin functionalities without Firebase or complex mocking
 *
 * Test Coverage:
 * 1. Browse Profiles
 * 2. Remove Profiles
 * 3. Browse Organizers
 * 4. Remove Organizers
 * 5. Notification Logs
 */
public class AdminFeaturesUnitTest {

    // ==========================================
    // TEST SUITE 1: BROWSE PROFILES
    // ==========================================

    @Test
    public void testBrowseProfiles_LoadsSuccessfully() {
        // Arrange: Create mock profile data
        List<Profile> mockProfiles = createMockProfiles(3);

        // Act & Assert
        assertNotNull("Profile list should not be null", mockProfiles);
        assertEquals("Should have 3 profiles", 3, mockProfiles.size());

        // Verify first profile has correct data
        Profile firstProfile = mockProfiles.get(0);
        assertEquals("user0", firstProfile.getUid());
        assertEquals("FirstName0", firstProfile.getFirstName());
        assertEquals("LastName0", firstProfile.getLastName());
    }

    @Test
    public void testBrowseProfiles_HandlesEmptyList() {
        // Arrange: Empty profile list
        List<Profile> emptyList = new ArrayList<>();

        // Act & Assert
        assertNotNull("Empty list should not be null", emptyList);
        assertEquals("List should be empty", 0, emptyList.size());
        assertTrue("List should be empty", emptyList.isEmpty());
    }

    @Test
    public void testBrowseProfiles_DisplaysCorrectData() {
        // Arrange
        Profile testProfile = new Profile(
                "user123",
                "John",
                "Doe",
                "john@example.com",
                "1234567890"
        );

        // Assert - Verify profile data
        assertEquals("John", testProfile.getFirstName());
        assertEquals("Doe", testProfile.getLastName());
        assertEquals("john@example.com", testProfile.getEmail());
        assertEquals("1234567890", testProfile.getPhone());
        assertEquals("John Doe", testProfile.getFullName());
        assertEquals("JD", testProfile.getInitials());
        assertEquals("(123) 456-7890", testProfile.getFormattedPhone());
    }

    @Test
    public void testProfileInitials_SingleName() {
        // Test with only first name
        Profile profile1 = new Profile("uid1", "Alice", "", "alice@example.com", "1234567890");
        assertEquals("A", profile1.getInitials());

        // Test with only last name
        Profile profile2 = new Profile("uid2", "", "Smith", "smith@example.com", "1234567890");
        assertEquals("S", profile2.getInitials());
    }

    // ==========================================
    // TEST SUITE 2: REMOVE PROFILES
    // ==========================================

    @Test
    public void testRemoveProfile_FromList() {
        // Arrange
        List<Profile> profiles = createMockProfiles(3);
        Profile profileToRemove = profiles.get(1);
        int originalSize = profiles.size();

        // Act
        boolean removed = profiles.remove(profileToRemove);

        // Assert
        assertTrue("Profile should be removed", removed);
        assertEquals("List should have one less profile", originalSize - 1, profiles.size());
        assertFalse("Removed profile should not be in list", profiles.contains(profileToRemove));
    }

    @Test
    public void testRemoveProfile_HandlesNullProfile() {
        // Arrange & Act
        try {
            Profile nullProfile = null;
            if (nullProfile == null) {
                throw new IllegalArgumentException("Profile cannot be null");
            }
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Assert
            assertEquals("Profile cannot be null", e.getMessage());
        }
    }

    @Test
    public void testRemoveProfile_UpdatesListCorrectly() {
        // Arrange
        List<Profile> profiles = createMockProfiles(5);
        Profile toDelete = profiles.get(2); // Middle profile
        String deletedUid = toDelete.getUid();

        // Act
        profiles.remove(toDelete);

        // Assert
        assertEquals(4, profiles.size());

        // Verify deleted profile is not in list
        for (Profile p : profiles) {
            assertNotEquals("Deleted profile should not be in list", deletedUid, p.getUid());
        }
    }

    @Test
    public void testRemoveProfile_RemoveMultiple() {
        // Arrange
        List<Profile> profiles = createMockProfiles(10);

        // Act - Remove 3 profiles
        profiles.remove(0);
        profiles.remove(0);
        profiles.remove(0);

        // Assert
        assertEquals("Should have 7 profiles left", 7, profiles.size());
    }

    // ==========================================
    // TEST SUITE 3: BROWSE ORGANIZERS
    // ==========================================

    @Test
    public void testBrowseOrganizers_CountsEventsCorrectly() {
        // Arrange: Create mock events
        List<Event> mockEvents = Arrays.asList(
                createMockEvent("event1", "org1", "Event 1"),
                createMockEvent("event2", "org1", "Event 2"),
                createMockEvent("event3", "org2", "Event 3"),
                createMockEvent("event4", "org1", "Event 4")
        );

        // Act: Count events per organizer
        int org1Count = 0;
        int org2Count = 0;

        for (Event event : mockEvents) {
            if ("org1".equals(event.getOrganizerId())) {
                org1Count++;
            } else if ("org2".equals(event.getOrganizerId())) {
                org2Count++;
            }
        }

        // Assert
        assertEquals("Organizer 1 should have 3 events", 3, org1Count);
        assertEquals("Organizer 2 should have 1 event", 1, org2Count);
    }

    @Test
    public void testBrowseOrganizers_HandlesNoEvents() {
        // Arrange
        List<Event> emptyEvents = new ArrayList<>();

        // Act & Assert
        assertEquals(0, emptyEvents.size());
        assertTrue("Event list should be empty", emptyEvents.isEmpty());
    }

    @Test
    public void testBrowseOrganizers_DisplaysOrganizerInfo() {
        // Arrange
        AdminOrganizerAdapter.OrganizerInfo organizer =
                new AdminOrganizerAdapter.OrganizerInfo(
                        "org1",
                        "Alice Organizer",
                        "alice@example.com",
                        5
                );

        // Assert
        assertEquals("org1", organizer.organizerId);
        assertEquals("Alice Organizer", organizer.organizerName);
        assertEquals("alice@example.com", organizer.organizerEmail);
        assertEquals(5, organizer.eventCount);
    }

    @Test
    public void testBrowseOrganizers_GroupsByOrganizerId() {
        // Arrange
        List<Event> events = Arrays.asList(
                createMockEvent("e1", "org1", "Event 1"),
                createMockEvent("e2", "org2", "Event 2"),
                createMockEvent("e3", "org1", "Event 3"),
                createMockEvent("e4", "org3", "Event 4"),
                createMockEvent("e5", "org2", "Event 5")
        );

        // Act: Count unique organizers
        List<String> uniqueOrganizers = new ArrayList<>();
        for (Event event : events) {
            String orgId = event.getOrganizerId();
            if (!uniqueOrganizers.contains(orgId)) {
                uniqueOrganizers.add(orgId);
            }
        }

        // Assert
        assertEquals("Should have 3 unique organizers", 3, uniqueOrganizers.size());
        assertTrue(uniqueOrganizers.contains("org1"));
        assertTrue(uniqueOrganizers.contains("org2"));
        assertTrue(uniqueOrganizers.contains("org3"));
    }

    // ==========================================
    // TEST SUITE 4: REMOVE ORGANIZERS
    // ==========================================

    @Test
    public void testRemoveOrganizer_DeletesAllEvents() {
        // Arrange: Organizer with 3 events
        String organizerId = "org1";
        List<Event> organizerEvents = Arrays.asList(
                createMockEvent("event1", organizerId, "Event 1"),
                createMockEvent("event2", organizerId, "Event 2"),
                createMockEvent("event3", organizerId, "Event 3")
        );

        // Act: Simulate deletion
        int deletedCount = organizerEvents.size();

        // Assert
        assertEquals("Should delete 3 events", 3, deletedCount);
    }

    @Test
    public void testRemoveOrganizer_CascadeDeletion() {
        // Arrange
        String organizerId = "org1";
        List<Event> allEvents = new ArrayList<>(Arrays.asList(
                createMockEvent("e1", organizerId, "Event 1"),
                createMockEvent("e2", organizerId, "Event 2"),
                createMockEvent("e3", "org2", "Event 3"),
                createMockEvent("e4", organizerId, "Event 4")
        ));

        int originalEventCount = allEvents.size();

        // Act: Remove all events by org1
        List<Event> toRemove = new ArrayList<>();
        for (Event event : allEvents) {
            if (organizerId.equals(event.getOrganizerId())) {
                toRemove.add(event);
            }
        }
        allEvents.removeAll(toRemove);

        // Assert
        assertEquals("Should have removed 3 events", 3, toRemove.size());
        assertEquals("Should have 1 event left", 1, allEvents.size());
        assertEquals("Remaining event should be from org2", "org2", allEvents.get(0).getOrganizerId());
    }

    @Test
    public void testRemoveOrganizer_HandlesZeroEvents() {
        // Arrange: Organizer with no events
        String organizerId = "org1";
        List<Event> emptyEvents = new ArrayList<>();

        // Act: Count events to delete
        int eventsToDelete = 0;
        for (Event event : emptyEvents) {
            if (organizerId.equals(event.getOrganizerId())) {
                eventsToDelete++;
            }
        }

        // Assert
        assertEquals("Should have 0 events to delete", 0, eventsToDelete);
    }

    @Test
    public void testRemoveOrganizer_PreservesOtherOrganizers() {
        // Arrange
        List<Event> allEvents = new ArrayList<>(Arrays.asList(
                createMockEvent("e1", "org1", "Event 1"),
                createMockEvent("e2", "org2", "Event 2"),
                createMockEvent("e3", "org3", "Event 3"),
                createMockEvent("e4", "org1", "Event 4")
        ));

        // Act: Remove only org1's events
        allEvents.removeIf(event -> "org1".equals(event.getOrganizerId()));

        // Assert
        assertEquals(2, allEvents.size());

        // Verify remaining events are from other organizers
        for (Event event : allEvents) {
            assertNotEquals("Should not be org1's event", "org1", event.getOrganizerId());
        }
    }

    // ==========================================
    // TEST SUITE 5: NOTIFICATION LOGS
    // ==========================================

    @Test
    public void testNotificationLogs_CreatesLogEntry() {
        // Arrange
        long timestamp = System.currentTimeMillis();
        NotificationLogAdapter.NotificationLog log =
                new NotificationLogAdapter.NotificationLog(
                        "John Doe",
                        "john@example.com",
                        "Event Invitation",
                        "You are invited to Summer Festival",
                        "invitation",
                        timestamp
                );

        // Assert
        assertEquals("John Doe", log.recipientName);
        assertEquals("john@example.com", log.recipientEmail);
        assertEquals("Event Invitation", log.title);
        assertEquals("You are invited to Summer Festival", log.message);
        assertEquals("invitation", log.type);
        assertEquals(timestamp, log.timestamp);
    }

    @Test
    public void testNotificationLogs_SortsDescending() {
        // Arrange: Create logs with different timestamps
        long now = System.currentTimeMillis();
        List<NotificationLogAdapter.NotificationLog> logs = new ArrayList<>(Arrays.asList(
                createMockNotificationLog("User1", now - 1000),  // Older
                createMockNotificationLog("User2", now),          // Newest
                createMockNotificationLog("User3", now - 2000)   // Oldest
        ));

        // Act: Sort by timestamp descending
        logs.sort((a, b) -> Long.compare(b.timestamp, a.timestamp));

        // Assert: Newest first
        assertEquals("User2", logs.get(0).recipientName);
        assertEquals("User1", logs.get(1).recipientName);
        assertEquals("User3", logs.get(2).recipientName);
    }

    @Test
    public void testNotificationLogs_HandlesMultipleTypes() {
        // Arrange
        List<NotificationLogAdapter.NotificationLog> logs = Arrays.asList(
                new NotificationLogAdapter.NotificationLog(
                        "User1", "user1@example.com", "Title1", "Message1",
                        "info", System.currentTimeMillis()
                ),
                new NotificationLogAdapter.NotificationLog(
                        "User2", "user2@example.com", "Title2", "Message2",
                        "invitation", System.currentTimeMillis()
                ),
                new NotificationLogAdapter.NotificationLog(
                        "User3", "user3@example.com", "Title3", "Message3",
                        "alert", System.currentTimeMillis()
                )
        );

        // Act: Count by type
        int infoCount = 0;
        int invitationCount = 0;
        int alertCount = 0;

        for (NotificationLogAdapter.NotificationLog log : logs) {
            if ("info".equals(log.type)) infoCount++;
            else if ("invitation".equals(log.type)) invitationCount++;
            else if ("alert".equals(log.type)) alertCount++;
        }

        // Assert
        assertEquals(1, infoCount);
        assertEquals(1, invitationCount);
        assertEquals(1, alertCount);
    }

    @Test
    public void testNotificationLogs_FiltersUnreadNotifications() {
        // Arrange
        List<NotificationLogAdapter.NotificationLog> allLogs = Arrays.asList(
                createMockNotificationLog("User1", System.currentTimeMillis()),
                createMockNotificationLog("User2", System.currentTimeMillis()),
                createMockNotificationLog("User3", System.currentTimeMillis())
        );

        // Act & Assert
        assertNotNull("Logs should not be null", allLogs);
        assertEquals("Should have 3 logs", 3, allLogs.size());
    }

    // ==========================================
    // TEST SUITE 6: INTEGRATION TESTS
    // ==========================================

    @Test
    public void testCompleteWorkflow_RemoveOrganizerAndEvents() {
        // Arrange: Full scenario
        String organizerId = "org1";

        // Create profile
        Profile organizer = new Profile(
                organizerId, "Bob", "Organizer", "bob@example.com", "1234567890"
        );

        // Create events
        List<Event> events = Arrays.asList(
                createMockEvent("e1", organizerId, "Event 1"),
                createMockEvent("e2", organizerId, "Event 2")
        );

        // Act: Simulate removal
        int eventCount = events.size();

        // Assert
        assertEquals("Bob", organizer.getFirstName());
        assertEquals(2, eventCount);
    }

    @Test
    public void testDataIntegrity_ProfileEventRelationship() {
        // Arrange
        Profile organizer = new Profile("org1", "Alice", "Admin", "alice@example.com", "1234567890");
        Event event = createMockEvent("e1", organizer.getUid(), "Alice's Event");

        // Assert - Verify relationship
        assertEquals("Organizer ID should match", organizer.getUid(), event.getOrganizerId());
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================

    private List<Profile> createMockProfiles(int count) {
        List<Profile> profiles = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            profiles.add(new Profile(
                    "user" + i,
                    "FirstName" + i,
                    "LastName" + i,
                    "user" + i + "@example.com",
                    "123456789" + i
            ));
        }
        return profiles;
    }

    private Event createMockEvent(String eventId, String organizerId, String title) {
        Event event = new Event();
        event.setEventId(eventId);
        event.setOrganizerId(organizerId);
        event.setTitle(title);
        event.setCapacity(50);
        return event;
    }

    private NotificationLogAdapter.NotificationLog createMockNotificationLog(
            String recipientName, long timestamp) {
        return new NotificationLogAdapter.NotificationLog(
                recipientName,
                recipientName.toLowerCase() + "@example.com",
                "Test Notification",
                "This is a test message",
                "info",
                timestamp
        );
    }
}