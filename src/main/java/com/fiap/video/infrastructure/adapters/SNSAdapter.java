package com.fiap.video.infrastructure.adapters;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sns.model.Topic;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class SNSAdapter {

    private final AmazonSNS snsClient;
    private final Topic productEventsTopic;
    private final ObjectMapper objectMapper;
    private static final String messageGroupId = "meu-message-group-id";

    public SNSAdapter(@Qualifier("productEventsTopic") Topic productEventsTopic, AmazonSNS snsClient) {
        this.snsClient = snsClient;
        this.objectMapper = new ObjectMapper();
        this.productEventsTopic = productEventsTopic;
    }

    public void publishMessage(String idSolicitacao, String usuario, String status, String email, String urlVideoCortesS3) {
        try {
            Map<String, String> message = new HashMap<>();
            message.put("idSolicitacao", idSolicitacao);
            message.put("usuario", usuario);
            message.put("status", status);
            message.put("email", email);
            message.put("urlVideoCortesS3", urlVideoCortesS3);

            String jsonMessage = objectMapper.writeValueAsString(message);

            PublishRequest publishRequest = new PublishRequest(productEventsTopic.getTopicArn(), jsonMessage)
                    .withMessageGroupId(messageGroupId)
                    .withMessageDeduplicationId(UUID.randomUUID().toString());

            PublishResult publishResult = snsClient.publish(publishRequest);

            String messageId = publishResult.getMessageId();
            System.out.println("Mensagem publicada com sucesso! MessageId: " + messageId);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao publicar mensagem no SNS", e);
        }
    }
}