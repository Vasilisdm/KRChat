package com.example.krchat.service

import com.example.krchat.asDomainObject
import com.example.krchat.asViewModel
import com.example.krchat.repository.ContentType
import com.example.krchat.repository.Message
import com.example.krchat.repository.MessageRepository
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import java.net.URL

@Service
@Primary
class PersistentMessageService(val messageRepository: MessageRepository) : MessageService {
    override fun latest(): List<MessageVM> = messageRepository.findLatest()
        .map {
            it.asViewModel()
        }

    override fun after(messageId: String): List<MessageVM> = messageRepository.findLatest(messageId)
        .map {
            it.asViewModel()
        }

    override fun post(message: MessageVM) {
        messageRepository.save(
            with(message) {
                this.asDomainObject()
            }
        )
    }
}