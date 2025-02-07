package com.fiap.video.infrastructure.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.video.core.application.enums.VideoStatus;
import com.fiap.video.core.application.usecases.ProcessVideoUseCase;
import com.fiap.video.core.domain.SnsMessageWrapper;
import com.fiap.video.core.domain.VideoMessage;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SqsListener {


    private final ProcessVideoUseCase processVideoUseCase;

    @Autowired
    public SqsListener(ProcessVideoUseCase processVideoUseCase) {
        this.processVideoUseCase = processVideoUseCase;
    }

    @io.awspring.cloud.sqs.annotation.SqsListener("${spring.cloud.aws.sqs.queue-name}")
    public void receiveMessage(Message<String> message) {
        String content = message.getPayload();
        if (content == null) {
            log.error("Received null content from SQS");
            return;
        }
        try {
            JSONObject snsMessage = new JSONObject(content);

            String messageContent = snsMessage.getString("Message");

            JSONObject videoMessageJson = new JSONObject(messageContent);

            VideoMessage videoMessage = new VideoMessage();
            videoMessage.setId(videoMessageJson.getString("id"));
            videoMessage.setEmail(videoMessageJson.getString("email"));
            videoMessage.setUser(videoMessageJson.getString("user"));
            videoMessage.setVideoKeyS3(videoMessageJson.getString("videoKeyS3"));
            videoMessage.setIntervalSeconds(videoMessageJson.getInt("intervalSeconds"));

            processVideoUseCase.process(videoMessage);

        } catch (Exception e) {
            log.error("Erro ao processar a mensagem", e);
        }
    }
}

