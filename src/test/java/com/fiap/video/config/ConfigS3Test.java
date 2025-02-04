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

class ConfigS3Test {

    @InjectMocks
    private ConfigS3 configS3;

    @Mock
    private AwsCredentialsProvider awsCredentialsProvider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        configS3 = new ConfigS3();
        configS3.setAccessKeyId("mock-access-key");
        configS3.setSecretAccessKey("mock-secret-key");
        configS3.setToken("mock-token");
        configS3.setAccessKeyId("mock-acess");
    }

    @Test
    void testGetS3Client() {
        AwsSessionCredentials mockCredentials = AwsSessionCredentials.create("mock-access-key", "mock-secret-key", "mock-token");
        when(awsCredentialsProvider.resolveCredentials()).thenReturn(mockCredentials);

        S3Client s3Client = configS3.getS3Client();

        assertNotNull(s3Client, "S3Client foi criado");
    }
}