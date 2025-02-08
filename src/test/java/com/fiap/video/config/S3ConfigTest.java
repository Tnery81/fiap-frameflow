package com.fiap.video.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.services.s3.S3Client;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

class S3ConfigTest {

    @InjectMocks
    private S3Config s3Config;

    @Mock
    private AwsCredentialsProvider awsCredentialsProvider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        s3Config = new S3Config();
        s3Config.setAccessKeyId("mock-access-key");
        s3Config.setSecretAccessKey("mock-secret-key");
        s3Config.setToken("mock-token");
        s3Config.setAccessKeyId("mock-acess");
    }

    @Test
    void testGetS3Client() {
        AwsSessionCredentials mockCredentials = AwsSessionCredentials.create("mock-access-key", "mock-secret-key", "mock-token");
        when(awsCredentialsProvider.resolveCredentials()).thenReturn(mockCredentials);

        S3Client s3Client = s3Config.getS3Client();

        assertNotNull(s3Client, "S3Client foi criado");
    }
}