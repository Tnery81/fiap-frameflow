package com.fiap.video.infrastructure.adapters;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sns.model.Topic;
import com.fiap.video.config.SNSConfig;
import com.fiap.video.core.application.enums.VideoStatus;
import com.fiap.video.core.domain.VideoMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SNSAdapterTest {

    @Mock
    private SNSConfig snsConfig;

    @Mock
    private AmazonSNS amazonSNS;

    @Mock
    private Topic productEventsTopic;

    @InjectMocks
    private SNSAdapter snsAdapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(snsConfig.snsClient()).thenReturn(amazonSNS);
        when(productEventsTopic.getTopicArn()).thenReturn("arn:aws:sns:us-east-1:123456789012:my-topic");
    }


    @Test
    void testPublishMessage_shouldSendMessageToSNS() {
        // Arrange
        VideoMessage videoMessage = new VideoMessage();
        videoMessage.setId("123");
        videoMessage.setUser("testUser");
        videoMessage.setEmail("test@example.com");
        videoMessage.setVideoKeyS3("testVideo.mp4");

        String zipKeyS3 = "output.zip";
        String videoUrlS3 = "https://s3.amazonaws.com/bucket/video.mp4";
        VideoStatus status = VideoStatus.COMPLETED;

        PublishResult publishResult = new PublishResult().withMessageId("test-message-id");
        when(amazonSNS.publish(any(PublishRequest.class))).thenReturn(publishResult);

        // Act
        snsAdapter.publishMessage(videoMessage, status, zipKeyS3, videoUrlS3);

        // Assert
        verify(amazonSNS, times(1)).publish(any(PublishRequest.class));
    }

    @Test
    void testPublishMessage_shouldHandleException() {
        // Arrange
        VideoMessage videoMessage = new VideoMessage();
        videoMessage.setId("123");
        videoMessage.setUser("testUser");
        videoMessage.setEmail("test@example.com");
        videoMessage.setVideoKeyS3("testVideo.mp4");

        String zipKeyS3 = "output.zip";
        String videoUrlS3 = "https://s3.amazonaws.com/bucket/video.mp4";
        VideoStatus status = VideoStatus.COMPLETED;

        when(amazonSNS.publish(any(PublishRequest.class))).thenThrow(new RuntimeException("SNS error"));

        // Act & Assert
        try {
            snsAdapter.publishMessage(videoMessage, status, zipKeyS3, videoUrlS3);
        } catch (RuntimeException e) {
            // Esperado
        }

        verify(amazonSNS, times(1)).publish(any(PublishRequest.class));
    }
}
