package com.fiap.video.infrastructure.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.video.core.application.usecases.ProcessVideoUseCase;
import com.fiap.video.core.domain.SnsMessageWrapper;
import com.fiap.video.core.domain.VideoMessage;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SqsConsumer {

    private final ObjectMapper objectMapper;

    private final ProcessVideoUseCase processVideoUseCase;

    @Autowired
    public SqsConsumer(ObjectMapper objectMapper, ProcessVideoUseCase processVideoUseCase) {
        this.objectMapper = objectMapper;
        this.processVideoUseCase = processVideoUseCase;
    }
    @SqsListener("${spring.cloud.aws.sqs.queue-name}")
    public void receiveMessage(SnsMessageWrapper snsMessageWrapper) {
        try {
            if (snsMessageWrapper != null && snsMessageWrapper.getMessage() != null) {
                VideoMessage videoMessage = objectMapper.readValue(snsMessageWrapper.getMessage(), VideoMessage.class);

                log.info("Mensagem recebida: " + videoMessage.toString());

                processVideoUseCase.process(videoMessage);
            } else {
                log.error("Mensagem SNS está vazia ou nula");
            }
        } catch (Exception e) {
            log.error("Erro ao processar a mensagem", e);
        }
    }
}

