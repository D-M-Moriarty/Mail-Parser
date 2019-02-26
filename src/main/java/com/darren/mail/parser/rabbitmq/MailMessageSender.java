package com.darren.mail.parser.rabbitmq;

import com.darren.mail.parser.entity.MailMessage;
import com.google.gson.Gson;
import net.gpedro.integrations.slack.SlackApi;
import net.gpedro.integrations.slack.SlackMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

@Service
@EnableConfigurationProperties(RabbitMqConfig.class)
public class MailMessageSender {

  private static final Logger logger = LoggerFactory.getLogger(MailMessageSender.class);

  private final RabbitTemplate rabbitTemplate;

  private SlackApi api = new SlackApi(
      "https://hooks.slack.com/services/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");


  @Autowired
  private RabbitMqConfig rabbitMqConfig;

  @Autowired
  public MailMessageSender(final RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  // Send's Message to Console, Splunk and RabbitMQ
  public void sendMessage(MailMessage mailMessage) throws AmqpException {
      logger.info("{}", "Mail Processed - Sending Message Now\n" + new Gson().toJson(mailMessage));
      rabbitTemplate
          .convertAndSend(rabbitMqConfig.exchangeName, rabbitMqConfig.routingKey, mailMessage);
      api.call(new SlackMessage("Processed Mail: " + new Gson().toJson(mailMessage)));
  }
}