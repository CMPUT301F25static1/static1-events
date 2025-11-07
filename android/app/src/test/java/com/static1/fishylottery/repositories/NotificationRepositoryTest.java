package com.static1.fishylottery.repositories;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.static1.fishylottery.model.entities.AppNotification;
import com.static1.fishylottery.model.repositories.NotificationRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class NotificationRepositoryTest {

    @Mock FirebaseFirestore mockDb;
    @Mock CollectionReference mockProfiles;
    @Mock DocumentReference mockProfileDoc;
    @Mock CollectionReference mockNotifs;
    @Mock DocumentReference mockNotifDoc;

    NotificationRepository repo;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        repo = new NotificationRepository();   // ✅ real constructor
    }

    // ---------------------------------------------------------------
    // ✅ TEST 1 — addNotification()
    // ---------------------------------------------------------------
    @Test
    public void test_addNotification_success() {

        try (MockedStatic<FirebaseFirestore> fs = Mockito.mockStatic(FirebaseFirestore.class)) {

            fs.when(FirebaseFirestore::getInstance).thenReturn(mockDb);

            when(mockDb.collection("profiles")).thenReturn(mockProfiles);
            when(mockProfiles.document("U1")).thenReturn(mockProfileDoc);
            when(mockProfileDoc.collection("notifications")).thenReturn(mockNotifs);

            when(mockNotifs.add(any())).thenReturn(Tasks.forResult(mockNotifDoc));

            Task<DocumentReference> task =
                    repo.addNotification("U1", new AppNotification());

            assertTrue(task.isComplete());
        }
    }

    // ---------------------------------------------------------------
    // ✅ TEST 2 — markRead()
    // ---------------------------------------------------------------
    @Test
    public void test_markRead_updatesField() {

        try (MockedStatic<FirebaseFirestore> fs = Mockito.mockStatic(FirebaseFirestore.class)) {

            fs.when(FirebaseFirestore::getInstance).thenReturn(mockDb);

            when(mockDb.collection("profiles")).thenReturn(mockProfiles);
            when(mockProfiles.document("U1")).thenReturn(mockProfileDoc);
            when(mockProfileDoc.collection("notifications")).thenReturn(mockNotifs);
            when(mockNotifs.document("N1")).thenReturn(mockNotifDoc);

            when(mockNotifDoc.update("read", true)).thenReturn(Tasks.forResult(null));

            Task<Void> task = repo.markRead("U1", "N1");

            assertTrue(task.isComplete());
        }
    }

    // ---------------------------------------------------------------
    // ✅ TEST 3 — respondToInvitation()
    // ---------------------------------------------------------------
    @Test
    public void test_respondToInvitation_updatesStatus() {

        try (MockedStatic<FirebaseFirestore> fs = Mockito.mockStatic(FirebaseFirestore.class)) {

            fs.when(FirebaseFirestore::getInstance).thenReturn(mockDb);

            when(mockDb.collection("profiles")).thenReturn(mockProfiles);
            when(mockProfiles.document("U1")).thenReturn(mockProfileDoc);
            when(mockProfileDoc.collection("notifications")).thenReturn(mockNotifs);
            when(mockNotifs.document("N1")).thenReturn(mockNotifDoc);

            when(mockNotifDoc.update(eq("status"), eq("accepted")))
                    .thenReturn(Tasks.forResult(null));

            Task<Void> task = repo.respondToInvitation("U1", "N1", true);

            assertTrue(task.isComplete());
        }
    }
}
