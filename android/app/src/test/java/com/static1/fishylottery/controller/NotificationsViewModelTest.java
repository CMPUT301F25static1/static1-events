package com.static1.fishylottery.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.static1.fishylottery.model.entities.AppNotification;
import com.static1.fishylottery.model.repositories.NotificationRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

public class NotificationsViewModelTest {

    @Rule
    public InstantTaskExecutorRule rule = new InstantTaskExecutorRule();

    @Mock
    NotificationRepository repo;

    @Mock
    QuerySnapshot snap;

    @Mock
    DocumentSnapshot d1;

    @Mock
    DocumentSnapshot d2;

    private NotificationsViewModel viewModel;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        viewModel = new NotificationsViewModel(repo);
    }

    @Test
    public void inboxUpdatesOnSnapshot() {

        // ----- Mock AppNotification #1 -----
        AppNotification n1 = new AppNotification();
        n1.setId("1");
        n1.setTitle("A");
        n1.setMessage("M1");
        n1.setType("info");

        // ----- Mock AppNotification #2 -----
        AppNotification n2 = new AppNotification();
        n2.setId("2");
        n2.setTitle("B");
        n2.setMessage("M2");
        n2.setType("warning");

        when(d1.getId()).thenReturn("1");
        when(d2.getId()).thenReturn("2");

        when(d1.toObject(AppNotification.class)).thenReturn(n1);
        when(d2.toObject(AppNotification.class)).thenReturn(n2);

        when(snap.getDocuments()).thenReturn(Arrays.asList(d1, d2));

        // Capture callback
        final EventListener<QuerySnapshot>[] cb = new EventListener[1];

        doAnswer(invocation -> {
            cb[0] = invocation.getArgument(1);
            return null;
        }).when(repo).listenToInbox(eq("TEST_UID"), any());

        // Attach inbox listener
        viewModel.startListening("TEST_UID");

        // Fire snapshot event
        cb[0].onEvent(snap, (FirebaseFirestoreException) null);

        // Verify result
        List<AppNotification> list = viewModel.getInbox().getValue();

        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals("A", list.get(0).getTitle());
        assertEquals("B", list.get(1).getTitle());
    }
}
