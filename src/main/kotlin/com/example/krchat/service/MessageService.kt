package com.example.krchat.service

interface MessageService {

    suspend fun latest(): List<MessageVM>

    suspend fun after(messageId: String): List<MessageVM>

    suspend fun post(message: MessageVM)
}