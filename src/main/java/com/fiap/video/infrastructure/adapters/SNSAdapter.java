package com.fiap.video.infrastructure.adapters;

import com.amazonaws.services.sns.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.video.config.SNSConfig;
import com.fiap.video.core.application.enums.VideoStatus;
import com.fiap.video.core.domain.VideoMessage;
import com.fiap.video.infrastructure.exception.SnsPublishingException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class SNSAdapter {

    private final SNSConfig snsConfig;
    private final Topic productEventsTopic;
    private final ObjectMapper objectMapper;
    private static final String MEU_MESSAGE_GROUP_ID = "meu-message-group-id";

    public SNSAdapter(@Qualifier("productEventsTopic") Topic productEventsTopic, SNSConfig snsConfig) {
        this.snsConfig = snsConfig;
        this.objectMapper = new ObjectMapper();
        this.productEventsTopic = productEventsTopic;
    }

    public void publishMessage(VideoMessage videoMessage, VideoStatus status,  String zipKeyS3, String videoUrlS3) {
        try {

            Map<String, String> message = new HashMap<>();
            message.put("id", videoMessage.getId());
            message.put("user", videoMessage.getUser());
            message.put("status", status.toString());
            message.put("email",  videoMessage.getEmail());
            message.put("videoKeyS3", videoMessage.getVideoKeyS3());
            message.put("zipKeyS3", zipKeyS3);
            message.put("videoUrlS3", videoUrlS3);

            String jsonMessage = objectMapper.writeValueAsString(message);

            PublishRequest publishRequest = new PublishRequest(productEventsTopic.getTopicArn(), jsonMessage)
                    .withMessageGroupId(MEU_MESSAGE_GROUP_ID)
                    .withMessageDeduplicationId(UUID.randomUUID().toString());

            snsConfig.snsClient().publish(publishRequest);

        } catch (Exception e) {
            throw new SnsPublishingException("Erro ao publicar mensagem no SNS", e);
        }
    }
}