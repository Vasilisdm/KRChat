package com.example.krchat.extensions

import com.example.krchat.repository.ContentType
import com.example.krchat.repository.Message
import com.example.krchat.service.MessageVM
import com.example.krchat.service.UserVM
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser
import java.net.URL

fun ContentType.render(content: String) = when (this) {
    ContentType.PLAIN -> content
    ContentType.MARKDOWN -> {
        val flavourDescriptor = CommonMarkFlavourDescriptor()
        HtmlGenerator(
            content,
            MarkdownParser(flavourDescriptor).buildMarkdownTreeFromString(content),
            flavourDescriptor
        ).generateHtml()
    }
}

fun MessageVM.asDomainObject(contentType: ContentType = ContentType.MARKDOWN): Message =
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
        contentType.render(this.content),
        user = UserVM(
            name = this.username,
            avatarImageLink = URL(this.userAvatarImageLink)
        ),
        sent = this.sent,
        id = this.id
    )

fun List<Message>.mapToViewModel(): List<MessageVM> =
    this.map { it.asViewModel() }