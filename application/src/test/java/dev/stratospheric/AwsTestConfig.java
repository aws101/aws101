package dev.stratospheric;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static dev.stratospheric.TodoApplicationTests.localStack;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SNS;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS;

@TestConfiguration
public class AwsTestConfig {

  @Bean
  @Primary
  public AmazonSQSAsync amazonSQS() {
    return AmazonSQSAsyncClientBuilder.standard()
      .withCredentials(localStack.getDefaultCredentialsProvider())
      .withEndpointConfiguration(localStack.getEndpointConfiguration(SQS))
      .build();
  }

  @Bean
  @Primary
  public AmazonSNS amazonSNS() {
    return AmazonSNSClientBuilder
      .standard()
      .withCredentials(localStack.getDefaultCredentialsProvider())
      .withEndpointConfiguration(localStack.getEndpointConfiguration(SNS))
      .build();
  }
}
