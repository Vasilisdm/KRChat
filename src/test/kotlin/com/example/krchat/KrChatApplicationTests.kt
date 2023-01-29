package com.example.krchat

import app.cash.turbine.test
import com.example.krchat.repository.ContentType
import com.example.krchat.repository.Message
import com.example.krchat.repository.MessageRepository
import com.example.krchat.service.MessageVM
import com.example.krchat.service.UserVM
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.dataWithType
import org.springframework.messaging.rsocket.retrieveFlow
import prepareForTesting
import java.net.URI
import java.net.URL
import java.time.Instant
import java.time.temporal.ChronoUnit.MILLIS
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

class KrChatApplicationTests(
    @Autowired val rsocketBuilder: RSocketRequester.Builder,
    @Autowired val messageRepository: MessageRepository,
    @LocalServerPort val serverPort: Int
) {

    private val now = Instant.now()

    @ExperimentalTime
    @ExperimentalCoroutinesApi
    @Test
    fun `test that messages API streams latest messages`() {
        runBlocking {
            val rSocketRequester =
                rsocketBuilder.websocket(URI("ws://localhost:${serverPort}/rsocket"))

            rSocketRequester
                .route("api.v1.messages.stream")
                .retrieveFlow<MessageVM>()
                .test {
                    assertThat(expectItem().prepareForTesting())
                        .isEqualTo(
                            MessageVM(
                                "*testMessage*",
                                UserVM("test", URL("http://test.com")),
                                now.minusSeconds(2).truncatedTo(MILLIS)
                            )
                        )

                    assertThat(expectItem().prepareForTesting())
                        .isEqualTo(
                            MessageVM(
                                "<body><p><strong>testMessage2</strong></p></body>",
                                UserVM("test1", URL("http://test.com")),
                                now.minusSeconds(1).truncatedTo(MILLIS)
                            )
                        )
                    assertThat(expectItem().prepareForTesting())
                        .isEqualTo(
                            MessageVM(
                                "<body><p><code>testMessage3</code></p></body>",
                                UserVM("test2", URL("http://test.com")),
                                now.truncatedTo(MILLIS)
                            )
                        )

                    expectNoEvents()

                    launch {
                        rSocketRequester.route("api.v1.messages.stream")
                            .dataWithType(flow {
                                emit(
                                    MessageVM(
                                        "`HelloWorld`",
                                        UserVM("test", URL("http://test.com")),
                                        now.plusSeconds(1)
                                    )
                                )
                            })
                            .retrieveFlow<Void>()
                            .collect()
                    }

                    assertThat(expectItem().prepareForTesting())
                        .isEqualTo(
                            MessageVM(
                                "<body><p><code>HelloWorld</code></p></body>",
                                UserVM("test", URL("http://test.com")),
                                now.plusSeconds(1).truncatedTo(MILLIS)
                            )
                        )

                    cancelAndIgnoreRemainingEvents()
                }
        }
    }

    @ExperimentalTime
    @Test
    fun `test that messages streamed to the API is stored`() {
        runBlocking {
            launch {
                val rSocketRequester =
                    rsocketBuilder.websocket(URI("ws://localhost:${serverPort}/rsocket"))

                rSocketRequester.route("api.v1.messages.stream")
                    .dataWithType(flow {
                        emit(
                            MessageVM(
                                "`HelloWorld`",
                                UserVM("test", URL("http://test.com")),
                                now.plusSeconds(1)
                            )
                        )
                    })
                    .retrieveFlow<Void>()
                    .collect()
            }

            delay(2.seconds)

            messageRepository.findAll()
                .first { it.content.contains("HelloWorld") }
                .apply {
                    assertThat(this.prepareForTesting())
                        .isEqualTo(
                            Message(
                                "`HelloWorld`",
                                ContentType.MARKDOWN,
                                now.plusSeconds(1).truncatedTo(MILLIS),
                                "test",
                                "http://test.com"
                            )
                        )
                }
        }
    }
}