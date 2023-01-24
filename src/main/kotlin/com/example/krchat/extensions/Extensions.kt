package com.example.krchat.extensions

import com.example.krchat.repository.ContentType
import com.example.krchat.repository.Message
import com.example.krchat.service.MessageVM
import com.example.krchat.service.UserVM
import java.net.URL

fun MessageVM.asDomainObject(contentType: ContentType = ContentType.PLAIN): Message =
    Message(
        content = this.content,
        contentType = contentType,
        sent = this.sent,
        username = this.user.name,
        userAvatarImageLink = this.user.avatarImageLink.toString(),
        id = this.id
    )


fun Message.asViewModel(): MessageVM =
    MessageVM(
        content = this.content,
        user = UserVM(
            name = this.username,
            avatarImageLink = URL(this.userAvatarImageLink)
        ),
        sent = this.sent,
        id = this.id
    )

fun List<Message>.mapToViewModel(): List<MessageVM> =
    this.map { it.asViewModel() }