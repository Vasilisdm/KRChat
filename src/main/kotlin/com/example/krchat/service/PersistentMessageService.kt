package com.example.krchat.service

import com.example.krchat.extensions.asDomainObject
import com.example.krchat.extensions.asRendered
import com.example.krchat.extensions.mapToViewModel
import com.example.krchat.repository.MessageRepository
import kotlinx.coroutines.flow.*
import org.springframework.stereotype.Service

@Service
class PersistentMessageService(val messageRepository: MessageRepository) : MessageService {

    val sender: MutableSharedFlow<MessageVM> = MutableSharedFlow()

    override fun latest(): Flow<MessageVM> =
        messageRepository.findLatest()
            .mapToViewModel()

    override fun after(messageId: String): Flow<MessageVM> =
        messageRepository.findLatest(messageId)
            .mapToViewModel()

    override fun stream(): Flow<MessageVM> = sender

    override suspend fun post(messages: Flow<MessageVM>) =
        messages
            .onEach { sender.emit(it.asRendered()) }
            .map { it.asDomainObject() }
            .let { messageRepository.saveAll(it) }
            .collect()
}