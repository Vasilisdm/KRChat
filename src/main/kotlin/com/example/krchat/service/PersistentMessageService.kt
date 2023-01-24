package com.example.krchat.service

import com.example.krchat.asDomainObject
import com.example.krchat.mapToViewModel
import com.example.krchat.repository.MessageRepository
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service

@Service
@Primary
class PersistentMessageService(val messageRepository: MessageRepository) : MessageService {
    override fun latest(): List<MessageVM> = messageRepository.findLatest().mapToViewModel()

    override fun after(messageId: String): List<MessageVM> = messageRepository.findLatest(messageId).mapToViewModel()

    override fun post(message: MessageVM) {
        messageRepository.save(
            with(message) {
                this.asDomainObject()
            }
        )
    }
}