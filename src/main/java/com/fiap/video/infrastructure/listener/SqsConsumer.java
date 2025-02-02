package com.fiap.video.infrastructure.listener;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SqsConsumer {

    @SqsListener("${spring.cloud.aws.sqs.queue-name}")
    public void recieveMessage(String content) {

        System.out.println("Mensagem recebida: " + content);

    }
}

