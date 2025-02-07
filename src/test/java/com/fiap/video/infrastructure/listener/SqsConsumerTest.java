package com.fiap.video.infrastructure.listener;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.video.core.application.usecases.ProcessVideoUseCase;
import com.fiap.video.core.domain.SnsMessageWrapper;
import com.fiap.video.core.domain.VideoMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

public class SqsConsumerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ProcessVideoUseCase processVideoUseCase;

    @InjectMocks
    private SqsConsumer sqsConsumer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testReceiveMessage_shouldProcessVideoMessage() throws Exception {
        // Arrange
        SnsMessageWrapper snsMessageWrapper = new SnsMessageWrapper();
        snsMessageWrapper.setMessage("{\"videoId\":\"123\",\"bucket\":\"my-bucket\",\"path\":\"video.mp4\"}");
        VideoMessage videoMessage = new VideoMessage();
        when(objectMapper.readValue(snsMessageWrapper.getMessage(), VideoMessage.class)).thenReturn(videoMessage);
        sqsConsumer.receiveMessage(snsMessageWrapper);
        verify(processVideoUseCase, times(1)).process(videoMessage);
    }

    @Test
    void testReceiveMessage_shouldHandleNullMessage() {
        SnsMessageWrapper snsMessageWrapper = new SnsMessageWrapper();
        snsMessageWrapper.setMessage(null);
        sqsConsumer.receiveMessage(snsMessageWrapper);
        verify(processVideoUseCase, never()).process(any(VideoMessage.class));
    }

    @Test
    void testReceiveMessage_shouldHandleNullWrapper() {
        sqsConsumer.receiveMessage(null);
        verify(processVideoUseCase, never()).process(any(VideoMessage.class));
    }

    @Test
    void testReceiveMessage_shouldHandleJsonParsingException() throws Exception {
        SnsMessageWrapper snsMessageWrapper = new SnsMessageWrapper();
        snsMessageWrapper.setMessage("invalid-json");
        when(objectMapper.readValue(snsMessageWrapper.getMessage(), VideoMessage.class)).thenThrow(new RuntimeException("JSON parsing error"));
        sqsConsumer.receiveMessage(snsMessageWrapper);
        verify(processVideoUseCase, never()).process(any(VideoMessage.class));
    }
}
