package com.example.krchat.service

import com.example.krchat.repository.ContentType
import com.example.krchat.repository.Message
import com.example.krchat.repository.MessageRepository
import java.net.URL

class PersistentMessageService(val messageRepository: MessageRepository) : MessageService {
    override fun latest(): List<MessageVM> = messageRepository.findLatest()
        .map {
            with(it) {
                MessageVM(content, UserVM(username, URL(userAvatarImageLink)), sent, id)
            }
        }

    override fun after(messageId: String): List<MessageVM> = messageRepository.findLatest(messageId)
        .map {
            with(it) {
                MessageVM(content, UserVM(username, URL(userAvatarImageLink)), sent, id)
            }
        }

    override fun post(message: MessageVM) {
        messageRepository.save(
            with(message) {
                Message(content, ContentType.PLAIN, sent, user.name, user.avatarImageLink.toString())
            }
        )
    }
}