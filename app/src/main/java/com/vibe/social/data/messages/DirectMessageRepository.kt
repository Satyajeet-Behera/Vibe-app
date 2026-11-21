package com.vibe.social.data.messages

import android.util.Log
import com.example.BuildConfig
import com.vibe.social.data.MockDataProvider
import com.vibe.social.data.auth.AuthRepository
import com.vibe.social.model.Conversation
import com.vibe.social.model.DirectMessage
import com.vibe.social.model.MessageStatus
import com.vibe.social.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

interface DirectMessageRepository {
    val conversations: StateFlow<List<Conversation>>
    val totalUnreadCount: StateFlow<Int>
    val isRealtimeConnected: StateFlow<Boolean>

    fun getConversation(conversationId: String): Conversation?
    fun getMessagesForConversation(conversationId: String): StateFlow<List<DirectMessage>>
    fun markConversationAsRead(conversationId: String)
    suspend fun sendMessage(conversationId: String, text: String): DirectMessage
    fun initiateConversation(targetUser: User): Conversation
    fun searchConversations(query: String): List<Conversation>

    companion object {
        val instance: DirectMessageRepository by lazy { DirectMessageRepositoryImpl.instance }
    }
}

class DirectMessageRepositoryImpl private constructor() : DirectMessageRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val okHttpClient = OkHttpClient.Builder()
        .pingInterval(25, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null

    private val _isRealtimeConnected = MutableStateFlow(false)
    override val isRealtimeConnected: StateFlow<Boolean> = _isRealtimeConnected.asStateFlow()

    private val _messagesByConversation = MutableStateFlow<Map<String, List<DirectMessage>>>(emptyMap())

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    override val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    override val totalUnreadCount: StateFlow<Int> = _conversations.map { list ->
        list.sumOf { it.unreadCount }
    }.stateIn(scope, SharingStarted.Eagerly, 0)

    init {
        seedInitialConversations()
        connectSupabaseRealtime()
    }

    private fun connectSupabaseRealtime() {
        val authRepo = AuthRepository.instance
        if (!authRepo.isSupabaseConfigured) {
            _isRealtimeConnected.value = true
            return
        }

        try {
            val rawUrl = authRepo.supabaseUrl.trimEnd('/')
            val wsHost = rawUrl.removePrefix("https://").removePrefix("http://")
            val wsUrl = "wss://$wsHost/realtime/v1/websocket?apikey=${authRepo.supabaseAnonKey}&vsn=1.0.0"

            val request = Request.Builder().url(wsUrl).build()
            webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    _isRealtimeConnected.value = true
                    Log.d(TAG, "Supabase Realtime WebSocket connected")
                    // Join channel
                    val joinMsg = JSONObject().apply {
                        put("topic", "realtime:public:direct_messages")
                        put("event", "phx_join")
                        put("payload", JSONObject())
                        put("ref", "1")
                    }
                    webSocket.send(joinMsg.toString())
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    handleIncomingRealtimeMessage(text)
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    _isRealtimeConnected.value = false
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    Log.w(TAG, "Supabase Realtime WebSocket error: ${t.message}")
                    _isRealtimeConnected.value = false
                }
            })
        } catch (e: Exception) {
            Log.w(TAG, "Could not initialize Supabase Realtime: ${e.message}")
            _isRealtimeConnected.value = false
        }
    }

    private fun handleIncomingRealtimeMessage(rawText: String) {
        try {
            val json = JSONObject(rawText)
            val event = json.optString("event")
            if (event == "broadcast" || event == "INSERT") {
                val payload = json.optJSONObject("payload")?.optJSONObject("record")
                    ?: json.optJSONObject("payload")
                if (payload != null) {
                    val msgId = payload.optString("id", UUID.randomUUID().toString())
                    val convId = payload.optString("conversation_id", "")
                    val senderId = payload.optString("sender_id", "")
                    val content = payload.optString("text", "")
                    if (convId.isNotBlank() && content.isNotBlank()) {
                        val newMsg = DirectMessage(
                            id = msgId,
                            conversationId = convId,
                            senderId = senderId,
                            text = content,
                            timestamp = System.currentTimeMillis(),
                            status = MessageStatus.DELIVERED
                        )
                        deliverIncomingMessage(convId, newMsg)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse realtime message", e)
        }
    }

    private fun seedInitialConversations() {
        val now = System.currentTimeMillis()
        val currentUserId = MockDataProvider.currentUser.id

        val elena = MockDataProvider.users.firstOrNull { it.id == "user_elena" } ?: MockDataProvider.users[1]
        val marcus = MockDataProvider.users.firstOrNull { it.id == "user_marcus" } ?: MockDataProvider.users[2]
        val maya = MockDataProvider.users.firstOrNull { it.id == "user_maya" } ?: MockDataProvider.users[3]

        val elenaConvId = "conv_elena"
        val marcusConvId = "conv_marcus"
        val mayaConvId = "conv_maya"

        val elenaMessages = listOf(
            DirectMessage(
                id = "m_e1",
                conversationId = elenaConvId,
                senderId = elena.id,
                text = "Hey Alex! Loved your latest photo set from Shibuya!",
                timestamp = now - 3600000 * 3,
                status = MessageStatus.READ
            ),
            DirectMessage(
                id = "m_e2",
                conversationId = elenaConvId,
                senderId = currentUserId,
                text = "Thanks Elena! The light after the rain was just magical 🌧️",
                timestamp = now - 3600000 * 2,
                status = MessageStatus.READ
            ),
            DirectMessage(
                id = "m_e3",
                conversationId = elenaConvId,
                senderId = elena.id,
                text = "Are you still doing the architectural gallery feature next weekend?",
                timestamp = now - 60000 * 25,
                status = MessageStatus.DELIVERED
            )
        )

        val marcusMessages = listOf(
            DirectMessage(
                id = "m_m1",
                conversationId = marcusConvId,
                senderId = currentUserId,
                text = "Hey Marcus, what lens did you use for that night skyline shot?",
                timestamp = now - 3600000 * 8,
                status = MessageStatus.READ
            ),
            DirectMessage(
                id = "m_m2",
                conversationId = marcusConvId,
                senderId = marcus.id,
                text = "Sony 35mm f/1.4 GM! It captures so much low light sharpness 📷",
                timestamp = now - 3600000 * 7,
                status = MessageStatus.READ
            )
        )

        val mayaMessages = listOf(
            DirectMessage(
                id = "m_my1",
                conversationId = mayaConvId,
                senderId = maya.id,
                text = "Alex, I sent you the Figma prototypes for the exhibition layout!",
                timestamp = now - 3600000 * 24,
                status = MessageStatus.READ
            ),
            DirectMessage(
                id = "m_my2",
                conversationId = mayaConvId,
                senderId = maya.id,
                text = "Let me know your feedback whenever you have a second ✨",
                timestamp = now - 3600000 * 18,
                status = MessageStatus.READ
            )
        )

        _messagesByConversation.value = mapOf(
            elenaConvId to elenaMessages,
            marcusConvId to marcusMessages,
            mayaConvId to mayaMessages
        )

        _conversations.value = listOf(
            Conversation(
                id = elenaConvId,
                participant = elena,
                lastMessage = elenaMessages.last(),
                unreadCount = 1,
                isOnline = true,
                updatedAt = elenaMessages.last().timestamp
            ),
            Conversation(
                id = marcusConvId,
                participant = marcus,
                lastMessage = marcusMessages.last(),
                unreadCount = 0,
                isOnline = false,
                updatedAt = marcusMessages.last().timestamp
            ),
            Conversation(
                id = mayaConvId,
                participant = maya,
                lastMessage = mayaMessages.last(),
                unreadCount = 0,
                isOnline = true,
                updatedAt = mayaMessages.last().timestamp
            )
        )
    }

    override fun getConversation(conversationId: String): Conversation? {
        return _conversations.value.firstOrNull { it.id == conversationId }
    }

    override fun getMessagesForConversation(conversationId: String): StateFlow<List<DirectMessage>> {
        val initial = _messagesByConversation.value[conversationId] ?: emptyList()
        return _messagesByConversation.map { map ->
            map[conversationId] ?: emptyList()
        }.stateIn(scope, SharingStarted.Eagerly, initial)
    }

    override fun markConversationAsRead(conversationId: String) {
        val currentUserId = MockDataProvider.currentUser.id

        // Mark unread received messages as READ
        _messagesByConversation.update { map ->
            val list = map[conversationId] ?: return@update map
            val updated = list.map { msg ->
                if (msg.senderId != currentUserId && msg.status != MessageStatus.READ) {
                    msg.copy(status = MessageStatus.READ)
                } else {
                    msg
                }
            }
            map + (conversationId to updated)
        }

        // Clear conversation unread count
        _conversations.update { list ->
            list.map { conv ->
                if (conv.id == conversationId) {
                    conv.copy(unreadCount = 0)
                } else {
                    conv
                }
            }
        }
    }

    override suspend fun sendMessage(conversationId: String, text: String): DirectMessage {
        val currentUserId = MockDataProvider.currentUser.id
        val messageId = "msg_${UUID.randomUUID().toString().take(8)}"
        val now = System.currentTimeMillis()

        val newMessage = DirectMessage(
            id = messageId,
            conversationId = conversationId,
            senderId = currentUserId,
            text = text.trim(),
            timestamp = now,
            status = MessageStatus.SENT
        )

        // Append locally
        _messagesByConversation.update { map ->
            val current = map[conversationId] ?: emptyList()
            map + (conversationId to (current + newMessage))
        }

        // Update conversation summary
        _conversations.update { list ->
            val existing = list.firstOrNull { it.id == conversationId }
            if (existing != null) {
                val updated = existing.copy(
                    lastMessage = newMessage,
                    updatedAt = now
                )
                // Move updated conversation to top
                listOf(updated) + list.filter { it.id != conversationId }
            } else {
                list
            }
        }

        // Broadcast to Supabase Realtime if connected
        try {
            val broadcastPayload = JSONObject().apply {
                put("topic", "realtime:public:direct_messages")
                put("event", "broadcast")
                put("payload", JSONObject().apply {
                    put("id", messageId)
                    put("conversation_id", conversationId)
                    put("sender_id", currentUserId)
                    put("text", text.trim())
                    put("timestamp", now)
                })
                put("ref", UUID.randomUUID().toString().take(6))
            }
            webSocket?.send(broadcastPayload.toString())
        } catch (e: Exception) {
            Log.w(TAG, "Failed to broadcast message: ${e.message}")
        }

        // Simulate recipient delivery, read receipt, and realistic interactive reply
        simulateRecipientFlow(conversationId, newMessage)

        return newMessage
    }

    private fun simulateRecipientFlow(conversationId: String, sentMessage: DirectMessage) {
        scope.launch {
            val conversation = getConversation(conversationId) ?: return@launch
            val recipient = conversation.participant

            // 1. Delivered state
            delay(500)
            _messagesByConversation.update { map ->
                val list = map[conversationId] ?: return@update map
                map + (conversationId to list.map {
                    if (it.id == sentMessage.id) it.copy(status = MessageStatus.DELIVERED) else it
                })
            }

            // 2. Read receipt (double blue checkmark / seen)
            delay(800)
            _messagesByConversation.update { map ->
                val list = map[conversationId] ?: return@update map
                map + (conversationId to list.map {
                    if (it.id == sentMessage.id) it.copy(status = MessageStatus.READ) else it
                })
            }

            // 3. Typing indicator
            delay(1000)
            _conversations.update { list ->
                list.map {
                    if (it.id == conversationId) it.copy(isTyping = true) else it
                }
            }

            // 4. Generate intelligent response based on recipient persona
            delay(1500)
            val replyText = generatePersonaReply(recipient, sentMessage.text)
            val replyMessage = DirectMessage(
                id = "msg_${UUID.randomUUID().toString().take(8)}",
                conversationId = conversationId,
                senderId = recipient.id,
                text = replyText,
                timestamp = System.currentTimeMillis(),
                status = MessageStatus.DELIVERED
            )

            _messagesByConversation.update { map ->
                val current = map[conversationId] ?: emptyList()
                map + (conversationId to (current + replyMessage))
            }

            _conversations.update { list ->
                val existing = list.firstOrNull { it.id == conversationId }
                if (existing != null) {
                    val updated = existing.copy(
                        lastMessage = replyMessage,
                        isTyping = false,
                        updatedAt = System.currentTimeMillis(),
                        unreadCount = existing.unreadCount + 1
                    )
                    listOf(updated) + list.filter { it.id != conversationId }
                } else {
                    list
                }
            }
        }
    }

    private fun generatePersonaReply(user: User, userMessage: String): String {
        val lower = userMessage.lowercase()
        return when {
            lower.contains("hello") || lower.contains("hey") || lower.contains("hi") -> {
                "Hey! Great hearing from you! How is your day going? ✨"
            }
            lower.contains("photo") || lower.contains("camera") || lower.contains("shot") || lower.contains("lens") -> {
                "Totally agree! Lighting makes all the difference. I usually shoot during golden hour or right after rain 📸"
            }
            lower.contains("collab") || lower.contains("project") || lower.contains("work") -> {
                "I'd love to collaborate! Let's draft a moodboard together sometime this week 🎨"
            }
            user.id == "user_elena" -> {
                "Thank you so much! Really appreciate your support on my latest gallery post 💫"
            }
            user.id == "user_marcus" -> {
                "Awesome! Let me know if you want to test some gear together next time you're in town!"
            }
            else -> {
                "Thanks for the message! Let's definitely catch up soon 🙌"
            }
        }
    }

    private fun deliverIncomingMessage(conversationId: String, message: DirectMessage) {
        _messagesByConversation.update { map ->
            val list = map[conversationId] ?: emptyList()
            map + (conversationId to (list + message))
        }
        _conversations.update { list ->
            val existing = list.firstOrNull { it.id == conversationId }
            if (existing != null) {
                val updated = existing.copy(
                    lastMessage = message,
                    unreadCount = existing.unreadCount + 1,
                    updatedAt = message.timestamp
                )
                listOf(updated) + list.filter { it.id != conversationId }
            } else {
                list
            }
        }
    }

    override fun initiateConversation(targetUser: User): Conversation {
        val existing = _conversations.value.firstOrNull { it.participant.id == targetUser.id }
        if (existing != null) {
            return existing
        }

        val newConvId = "conv_${targetUser.id}"
        val newConv = Conversation(
            id = newConvId,
            participant = targetUser,
            lastMessage = null,
            unreadCount = 0,
            isOnline = true,
            updatedAt = System.currentTimeMillis()
        )

        _conversations.update { listOf(newConv) + it }
        _messagesByConversation.update { it + (newConvId to emptyList()) }

        return newConv
    }

    override fun searchConversations(query: String): List<Conversation> {
        val trimmed = query.trim().lowercase()
        if (trimmed.isEmpty()) return _conversations.value
        return _conversations.value.filter { conv ->
            conv.participant.fullName.lowercase().contains(trimmed) ||
            conv.participant.username.lowercase().contains(trimmed) ||
            (conv.lastMessage?.text?.lowercase()?.contains(trimmed) == true)
        }
    }

    companion object {
        private const val TAG = "DirectMessageRepo"
        val instance: DirectMessageRepository by lazy { DirectMessageRepositoryImpl() }
    }
}
