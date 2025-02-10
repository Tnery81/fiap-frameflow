package com.fiap.video.infrastructure.subscriber;

import com.fiap.video.core.application.usecases.ProcessVideoUseCase;
import com.fiap.video.core.domain.VideoMessage;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SQSSubscriber {

    private final ProcessVideoUseCase processVideoUseCase;

    @Autowired
    public SQSSubscriber(ProcessVideoUseCase processVideoUseCase) {
        this.processVideoUseCase = processVideoUseCase;
    }

    @SqsListener("video-carregado-subscriber-queue.fifo")
    public void receiveMessage(Message<String> message) {
        String content = message.getPayload();

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

