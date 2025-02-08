package com.fiap.video.config;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    @Setter
    @Value("${aws.accessKeyId}")
    private String accessKeyId;

    @Setter
    @Value("${aws.secretAccessKey}")
    private String secretAccessKey;

    @Setter
    @Value("${aws.token}")
    private String token;

    private final String regionName = Region.US_EAST_1.toString();

    @Bean
    public S3Client getS3Client() {
        AwsSessionCredentials credentials = AwsSessionCredentials.create(accessKeyId, secretAccessKey, token);
        AwsCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);

        return S3Client.builder()
                .region(Region.of(regionName))
                .credentialsProvider(credentialsProvider)
                .build();
    }
}