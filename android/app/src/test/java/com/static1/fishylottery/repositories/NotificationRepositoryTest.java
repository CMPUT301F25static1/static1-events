package com.static1.fishylottery.repositories;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mockStatic;

import com.google.firebase.firestore.FirebaseFirestore;
import com.static1.fishylottery.model.repositories.NotificationRepository;

import org.junit.Test;
import org.mockito.MockedStatic;

public class NotificationRepositoryTest {

    @Test
    public void repository_canBeCreated() {
        try (MockedStatic<FirebaseFirestore> fs = mockStatic(FirebaseFirestore.class)) {

            fs.when(FirebaseFirestore::getInstance).thenReturn(null);

            NotificationRepository repo = new NotificationRepository();
            assertNotNull(repo);
        }
    }
}
