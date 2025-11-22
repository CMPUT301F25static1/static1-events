package com.static1.fishylottery.controller;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mockStatic;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.google.firebase.firestore.FirebaseFirestore;
import com.static1.fishylottery.viewmodel.NotificationsViewModel;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.MockedStatic;

public class NotificationsViewModelTest {

    @Rule
    public InstantTaskExecutorRule rule = new InstantTaskExecutorRule();

    @Test
    public void viewModel_canBeCreated() {
        try (MockedStatic<FirebaseFirestore> fs = mockStatic(FirebaseFirestore.class)) {

            fs.when(FirebaseFirestore::getInstance).thenReturn(null);

            NotificationsViewModel vm = new NotificationsViewModel();
            assertNotNull(vm);
        }
    }

    @Test
    public void inbox_isNotNull() {
        try (MockedStatic<FirebaseFirestore> fs = mockStatic(FirebaseFirestore.class)) {

            fs.when(FirebaseFirestore::getInstance).thenReturn(null);

            NotificationsViewModel vm = new NotificationsViewModel();
            assertNotNull(vm.getInbox());
        }
    }
}
