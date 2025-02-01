package com.fiap.video.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.GetTopicAttributesRequest;
import com.amazonaws.services.sns.model.GetTopicAttributesResult;
import com.amazonaws.services.sns.model.SetTopicAttributesRequest;
import com.amazonaws.services.sns.model.Topic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;

@Configuration
public class SNSConfig {

    @Value("${aws.sns.topicArn}")
    private String topicArn;

    @Value("${aws.accessKeyId}")
    private String accessKeyId;

    @Value("${aws.secretAccessKey}")
    private String secretAccessKey;

    @Value("${aws.token}")
    private String token;

    private final String regionName = Region.US_EAST_1.toString();

    @Bean
    public AmazonSNS snsClient() {
        BasicSessionCredentials credentials = new BasicSessionCredentials(accessKeyId, secretAccessKey, token);

        AmazonSNS snsClient = AmazonSNSClientBuilder.standard()
                .withRegion(regionName)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();

        SetTopicAttributesRequest setTopicAttributesRequest = new SetTopicAttributesRequest()
                .withTopicArn(topicArn)
                .withAttributeName("ContentBasedDeduplication")
                .withAttributeValue("true");

        snsClient.setTopicAttributes(setTopicAttributesRequest);

        return snsClient;
    }



    @Bean(name = "productEventsTopic")
    public Topic snsProductEventsTopic() {

        GetTopicAttributesRequest getTopicAttributesRequest = new GetTopicAttributesRequest()
                .withTopicArn(topicArn);
        GetTopicAttributesResult getTopicAttributesResult = snsClient().getTopicAttributes(getTopicAttributesRequest);

        String topicArn = getTopicAttributesResult.getAttributes().get("TopicArn");


        return new Topic().withTopicArn(topicArn);
    }

}
