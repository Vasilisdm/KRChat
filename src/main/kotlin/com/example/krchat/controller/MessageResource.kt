package com.example.krchat.controller

import com.example.krchat.service.MessageService
import com.example.krchat.service.MessageVM
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.onStart
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*


@Controller
@MessageMapping("api.v1.messages")
class MessageResource(val messageService: MessageService) {

    @MessageMapping("stream")
    suspend fun receive(@Payload inboundMessages: Flow<MessageVM>) =
        messageService.post(inboundMessages)

    @MessageMapping("stream")
    fun send(): Flow<MessageVM> = messageService
        .stream()
        .onStart {
            emitAll(messageService.latest())
        }
}