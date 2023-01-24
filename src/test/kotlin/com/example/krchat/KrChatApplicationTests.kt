package com.example.krchat

import com.example.krchat.repository.ContentType
import com.example.krchat.repository.Message
import com.example.krchat.repository.MessageRepository
import com.example.krchat.service.MessageVM
import com.example.krchat.service.UserVM
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.RequestEntity
import prepareForTesting
import java.net.URI
import java.net.URL
import java.time.Instant
import java.time.temporal.ChronoUnit.MILLIS

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "spring.datasource.url=jdbc:h2:mem:testdb"
    ]
)
class KrChatApplicationTests {

    @Autowired
    lateinit var client: TestRestTemplate

    @Autowired
    lateinit var messageRepository: MessageRepository

    lateinit var lastMessageId: String

    val now: Instant = Instant.now()

    @BeforeEach
    fun setUp() {
        val secondBeforeNow = now.minusSeconds(1)
        val twoSecondBeforeNow = now.minusSeconds(2)
        val savedMessages = messageRepository.saveAll(
            listOf(
                Message(
                    content = "*testMessage*",
                    contentType = ContentType.PLAIN,
                    sent = twoSecondBeforeNow,
                    username = "test",
                    userAvatarImageLink = "http://test.com"
                ),
                Message(
                    content = "**testMessage2**",
                    contentType = ContentType.PLAIN,
                    sent = secondBeforeNow,
                    username = "test1",
                    userAvatarImageLink = "http://test.com"
                ),
                Message(
                    content = "`testMessage3`",
                    contentType = ContentType.PLAIN,
                    sent = now,
                    username = "test2",
                    userAvatarImageLink = "http://test.com"
                )
            )
        )
        lastMessageId = savedMessages.first().id ?: ""
    }

    @AfterEach
    fun tearDown() {
        messageRepository.deleteAll()
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `test that messages API returns latest message`(withLastMessageId: Boolean) {
        val messages: List<MessageVM>? = client.exchange(
            RequestEntity<Any>(
                HttpMethod.GET,
                URI("/api/v1/messages?lastMessageId=${if (withLastMessageId) lastMessageId else ""}")
            ),
            object : ParameterizedTypeReference<List<MessageVM>>() {}).body

        if (!withLastMessageId) {
            assertThat(messages?.map { it.prepareForTesting() })
                .first()
                .isEqualTo(
                    MessageVM(
                        "*testMessage*",
                        UserVM("test", URL("http://test.com")),
                        now.minusSeconds(2).truncatedTo(MILLIS)
                    )
                )
        }

        assertThat(messages?.map { it.prepareForTesting() })
            .containsSubsequence(
                MessageVM(
                    "**testMessage2**",
                    UserVM("test1", URL("http://test.com")),
                    now.minusSeconds(1).truncatedTo(MILLIS)
                ),
                MessageVM(
                    "`testMessage3`",
                    UserVM("test2", URL("http://test.com")),
                    now.truncatedTo(MILLIS)
                )
            )
    }

    @Test
    fun `test that messages posted to the API are stored`() {
        client.postForEntity<Any>(
            URI("/api/v1/messages"),
            MessageVM(
                content = "Hello people!",
                user = UserVM(name = "test", avatarImageLink = URL("http://test.com")),
                sent = now.plusSeconds(1)
            )
        )

        messageRepository.findAll()
            .first { it.content.contains("Hello people!") }
            .apply {
                assertThat(this.prepareForTesting())
                    .isEqualTo(
                        Message(
                            content = "Hello people!",
                            contentType = ContentType.PLAIN,
                            sent = now.plusSeconds(1).truncatedTo(MILLIS),
                            username = "test",
                            userAvatarImageLink = "http://test.com"
                        )
                    )
            }
    }
}
