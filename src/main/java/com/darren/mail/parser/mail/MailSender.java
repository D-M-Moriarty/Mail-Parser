package com.darren.mail.parser.mail;

import com.darren.mail.parser.serverproperties.MailProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

@Component
@EnableConfigurationProperties(MailProperties.class)
public class MailSender {

  private static final Logger logger = LoggerFactory.getLogger("pbmAppender");

  @Autowired
  private MailProperties mailProperties;

  public void informInvalidSubjectLine(Message message) {

    Session session = Session.getInstance(mailProperties.getSMTPProperties(),
        new javax.mail.Authenticator() {
          @Override
          protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(mailProperties.getEmailAddress(), mailProperties.getPassword());
          }
        });

    try {
      Message mailMessage = new MimeMessage(session);
      mailMessage.setFrom(new InternetAddress(mailProperties.getEmailAddress()));
      mailMessage.setRecipients(Message.RecipientType.TO,
          InternetAddress.parse(message.getFrom()[0].toString()));
      mailMessage.setSubject("!Attention - Message Processing Failed");
      mailMessage.setText("The following mail subject failed to process. \n\n- " + message.getSubject() + "\n\n The customer numbers must be encased within the following pattern: [#12345678].");

      Transport.send(mailMessage);

    } catch (MessagingException e) {
      logger.warn("Invalid subject line E-mail couldn't be sent.", e);
      logger.error("Exception caught: {}", e.getMessage(), e);
    }
  }
}