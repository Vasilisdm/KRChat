package com.example.krchat.service

import com.example.krchat.extensions.asDomainObject
import com.example.krchat.extensions.mapToViewModel
import com.example.krchat.repository.MessageRepository
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service

@Service
@Primary
class PersistentMessageService(val messageRepository: MessageRepository) : MessageService {
    override suspend fun latest(): List<MessageVM> = messageRepository.findLatest().mapToViewModel()

    override suspend fun after(messageId: String): List<MessageVM> = messageRepository.findLatest(messageId).mapToViewModel()

    override suspend fun post(message: MessageVM) {
        messageRepository.save(
            with(message) {
                this.asDomainObject()
            }
        )
    }
}