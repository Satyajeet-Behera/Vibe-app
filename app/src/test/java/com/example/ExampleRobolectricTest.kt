package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.vibe.social.data.auth.AuthRepository
import com.vibe.social.data.auth.AuthResult
import com.vibe.social.data.auth.AuthState
import com.vibe.social.data.messages.DirectMessageRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        AuthRepository.instance.initialize(context)
    }

    @Test
    fun `read string from context`() {
        val appName = context.getString(R.string.app_name)
        assertEquals("Vibe", appName)
    }

    @Test
    fun `auth repository initial state and login success`() = runBlocking {
        val authRepo = AuthRepository.instance
        val result = authRepo.login("alex_rivera@vibe.social", "password123")
        assertTrue("Login should succeed", result is AuthResult.Success)

        val currentUser = authRepo.currentUser.value
        assertNotNull("Current user should not be null", currentUser)
        assertEquals("alex_rivera", currentUser?.username)
    }

    @Test
    fun `auth repository registration and profile setup flow`() = runBlocking {
        val authRepo = AuthRepository.instance
        val regResult = authRepo.register("newuser@vibe.social", "secretpass123")
        assertTrue("Registration should succeed", regResult is AuthResult.Success)

        // After registration, state should be NeedsProfileSetup or Authenticated
        assertTrue(
            "State should be NeedsProfileSetup or Authenticated",
            authRepo.authState.value is AuthState.NeedsProfileSetup || authRepo.authState.value is AuthState.Authenticated
        )

        // Setup profile
        val profileResult = authRepo.completeProfileSetup(
            fullName = "Star Creator",
            username = "star_creator",
            bio = "Building the future of social vibe",
            avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500&auto=format&fit=crop&q=80"
        )
        assertTrue("Profile setup should succeed", profileResult is AuthResult.Success)
        assertEquals("star_creator", authRepo.currentUser.value?.username)
    }

    @Test
    fun `direct message repository loads conversations and sends messages`() = runBlocking {
        val msgRepo = DirectMessageRepository.instance
        val conversations = msgRepo.conversations.value
        assertTrue("Should have initial demo conversations", conversations.isNotEmpty())

        val firstConv = conversations.first()
        val initialMessages = msgRepo.getMessagesForConversation(firstConv.id).value
        val initialCount = initialMessages.size

        // Send a message
        val sent = msgRepo.sendMessage(firstConv.id, "Hello from Robolectric test!")
        assertNotNull("Sent message should not be null", sent)

        val updatedMessages = msgRepo.getMessagesForConversation(firstConv.id).value
        assertEquals("Message count should increase by 1", initialCount + 1, updatedMessages.size)

        // Mark as read
        msgRepo.markConversationAsRead(firstConv.id)
        val updatedConv = msgRepo.getConversation(firstConv.id)
        assertEquals("Unread count should be 0", 0, updatedConv?.unreadCount ?: 0)
    }
}
