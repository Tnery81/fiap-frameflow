package com.fiap.video.infrastructure.adapters;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sns.model.Topic;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.video.core.application.enums.VideoStatus;
import com.fiap.video.core.domain.VideoMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SNSAdapterTest {

    @InjectMocks
    private SNSAdapter snsAdapter;

    @Mock
    private AmazonSNS snsClient;

    @Mock
    private Topic productEventsTopic;

    @Mock
    private VideoMessage videoMessage;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        snsAdapter = new SNSAdapter(productEventsTopic, snsClient);
    }

    @Test
    void shouldPublishMessageSuccessfully() throws Exception {
        when(videoMessage.getId()).thenReturn(1L);
        when(videoMessage.getUser()).thenReturn("testUser");
        when(videoMessage.getEmail()).thenReturn("test@example.com");
        when(videoMessage.getVideoKeyS3()).thenReturn("videoKey");
        when(productEventsTopic.getTopicArn()).thenReturn("arn:aws:sns:region:123456789012:MyTopic");

        PublishResult publishResult = mock(PublishResult.class);
        when(publishResult.getMessageId()).thenReturn(UUID.randomUUID().toString());
        when(snsClient.publish(any(PublishRequest.class))).thenReturn(publishResult);

        assertDoesNotThrow(() -> snsAdapter.publishMessage(videoMessage, VideoStatus.IN_PROGRESS, "zipKey", "videoUrl"));

        verify(snsClient, times(1)).publish(any(PublishRequest.class));
    }

    @Test
    void shouldHandlePublishFailure() {
        when(videoMessage.getId()).thenReturn(1L);
        when(videoMessage.getUser()).thenReturn("testUser");
        when(videoMessage.getEmail()).thenReturn("test@example.com");
        when(videoMessage.getVideoKeyS3()).thenReturn("videoKey");
        when(productEventsTopic.getTopicArn()).thenReturn("arn:aws:sns:region:123456789012:MyTopic");

        when(snsClient.publish(any(PublishRequest.class))).thenThrow(new RuntimeException("SNS publish failed"));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                snsAdapter.publishMessage(videoMessage, VideoStatus.IN_PROGRESS, "zipKey", "videoUrl")
        );

        assertEquals("Erro ao publicar mensagem no SNS", exception.getMessage());
        verify(snsClient, times(1)).publish(any(PublishRequest.class));
    }
}
