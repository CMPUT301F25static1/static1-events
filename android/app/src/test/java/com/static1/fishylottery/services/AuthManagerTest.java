package com.static1.fishylottery.services;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class AuthManagerTest {

    private FirebaseAuth mockAuth;
    private FirebaseUser mockUser;
    private AuthManager authManager;

    @Before
    public void setUp() {
        // Create mocks
        mockAuth = mock(FirebaseAuth.class);
        mockUser = mock(FirebaseUser.class);

        // Setup AuthManager with mocked FirebaseAuth
        authManager = new AuthManager(mockAuth);
        AuthManager.setInstanceForTesting(authManager);
    }

    @After
    public void tearDown() {
        AuthManager.setInstanceForTesting(null);
    }

    @Test
    public void testSignInAnonymously() {
        Task<AuthResult> mockTask = Tasks.forResult(mock(AuthResult.class));
        when(mockAuth.signInAnonymously()).thenReturn(mockTask);

        Task<AuthResult> task = authManager.signInAnonymously();

        assertTrue(task.isComplete());
        verify(mockAuth).signInAnonymously();
    }

    @Test
    public void testGetUserId_whenUserSignedIn() {
        when(mockAuth.getCurrentUser()).thenReturn(mockUser);
        when(mockUser.getUid()).thenReturn("user123");

        String uid = authManager.getUserId();

        assertEquals("user123", uid);
    }

    @Test
    public void testGetUserId_whenNoUser() {
        when(mockAuth.getCurrentUser()).thenReturn(null);

        String uid = authManager.getUserId();

        assertNull(uid);
    }

    @Test
    public void testIsSignedIn_true() {
        when(mockAuth.getCurrentUser()).thenReturn(mockUser);

        assertTrue(authManager.isSignedIn());
    }

    @Test
    public void testIsSignedIn_false() {
        when(mockAuth.getCurrentUser()).thenReturn(null);

        assertFalse(authManager.isSignedIn());
    }

    @Test
    public void testDeleteUser_success() {
        when(mockAuth.getCurrentUser()).thenReturn(mockUser);
        when(mockUser.delete()).thenReturn(Tasks.forResult(null));

        Task<Void> task = authManager.deleteUser();

        assertTrue(task.isComplete());
        verify(mockUser).delete();
    }

    @Test
    public void testDeleteUser_noUser() {
        when(mockAuth.getCurrentUser()).thenReturn(null);

        Task<Void> task = authManager.deleteUser();

        assertTrue(task.isComplete());
        assertTrue(task.isCanceled() || task.isSuccessful() || task.getException() instanceof IllegalStateException);
    }
}