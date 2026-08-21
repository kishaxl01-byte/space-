package com.example.model

enum class MessageSender {
    USER,
    AI
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isGenerating: Boolean = false,
    val suggestedFollowups: List<String> = emptyList()
)
