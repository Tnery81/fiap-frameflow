package com.fiap.video.config;

import io.awspring.cloud.autoconfigure.sqs.SqsProperties;
import io.awspring.cloud.sqs.listener.QueueNotFoundStrategy;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import java.net.URI;

@Configuration
public class SQSConfig {

    @Setter
    @Value("${aws.accessKeyId}")
    private String accessKeyId;

    @Setter
    @Value("${aws.secretAccessKey}")
    private String secretAccessKey;

    @Setter
    @Value("${aws.token}")
    private String token;

    @Value("${aws.queueUrl}")
    private String queueUrl;

    private final String regionName = Region.US_EAST_1.toString();


    @Bean
    public SqsTemplate sqsTemplate(SqsAsyncClient sqsAsyncClient) {
        return SqsTemplate.builder()
                .sqsAsyncClient(sqsAsyncClient)
                .configure(o -> o.queueNotFoundStrategy(QueueNotFoundStrategy.FAIL))
                .build();
    }

    @Bean
    SqsAsyncClient sqsAsyncClient(SqsProperties properties) {
        AwsSessionCredentials credentials = AwsSessionCredentials.create(accessKeyId, secretAccessKey, token);
        
        return SqsAsyncClient.builder()
                .credentialsProvider( StaticCredentialsProvider.create(credentials))
                .region(Region.of(regionName))
                .endpointOverride(URI.create(queueUrl))
                .build();
    }
}
