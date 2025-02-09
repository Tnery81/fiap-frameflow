package com.fiap.video.infrastructure.subscriber;

import com.fiap.video.core.application.usecases.ProcessVideoUseCase;
import com.fiap.video.core.domain.VideoMessage;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.Message;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

class SQSSubscriberTest {

    @Mock
    private ProcessVideoUseCase processVideoUseCase;

    @Mock
    private Message<String> message;

    @InjectMocks
    private SQSSubscriber sqsConsumer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testReceiveMessage_shouldProcessVideoMessage() {
        try {
            // Arrange
            String snsMessageContent = "{\"id\":\"123\",\"email\":\"test@example.com\",\"user\":\"user1\",\"videoKeyS3\":\"video.mp4\",\"intervalSeconds\":10}";
            String snsMessageWrapper = new JSONObject().put("Message", snsMessageContent).toString();

            when(message.getPayload()).thenReturn(snsMessageWrapper);

            // Act
            sqsConsumer.receiveMessage(message);

            // Assert
            verify(processVideoUseCase, times(1)).process(any(VideoMessage.class));

        } catch (JSONException e) {
            fail("Falha ao criar JSON: " + e.getMessage());
        }
    }


    @Test
    void testReceiveMessage_shouldHandleNullMessage() {
        when(message.getPayload()).thenReturn(null);
        sqsConsumer.receiveMessage(message);
        verify(processVideoUseCase, never()).process(any(VideoMessage.class));
    }

    @Test
    void testReceiveMessage_shouldHandleInvalidJson() {
        when(message.getPayload()).thenReturn("invalid-json");
        sqsConsumer.receiveMessage(message);
        verify(processVideoUseCase, never()).process(any(VideoMessage.class));
    }
}
