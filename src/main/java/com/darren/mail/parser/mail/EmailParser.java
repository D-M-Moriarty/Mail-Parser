package com.darren.mail.parser.mail;

import com.darren.mail.parser.entity.CustomerDetails;
import com.darren.mail.parser.entity.MailAttachment;
import com.darren.mail.parser.entity.MailMessage;
import com.darren.mail.parser.exceptions.InvalidSubjectException;
import com.darren.mail.parser.pbmapi.PbmApi;
import com.darren.mail.parser.rabbitmq.MailMessageSender;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.mail.Flags.Flag;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import java.io.IOException;
import java.util.List;

@Service
public class EmailParser {

    private static final Logger logger = LoggerFactory.getLogger("pbmAppender");

  @Autowired
  private MailMessage mailMessage;
  @Autowired
  private MailMessageSender messageSender;
  @Autowired
  private MailSender mailSender;
  @Autowired
  private PbmApi pbmApi;

  /*
      Process each message being read and set attributes of MailMessage
   */
  public MailMessage processMail(Message message) throws MessagingException {

    try {
      List<String> customerMatches;
      List<String> customerNumbers;
      mailMessage = getMailMessage(message);

      if (MailUtils.isValidSubjectLine(message.getSubject())) {

        customerMatches = MailUtils.extractCustMatches(message.getSubject());
        customerNumbers = MailUtils.extractCustomerNumbers(customerMatches);

        if (pbmApi.isValidCustomerNumber(customerNumbers)) {
          mailMessage.setCustomerNumbers(customerNumbers);
          return mailMessage;
        } else {
          logger.warn("Warning, {} Is An Invalid Subject Line", message.getSubject());
          mailSender.informInvalidSubjectLine(message);
          return null;
        }

      } else if (MailUtils.isValidEmailAddress(mailMessage.getOriginalEmailAddress())) {

        List<CustomerDetails> customerDetails = pbmApi.getCustomerDetailsByEmail(mailMessage.getOriginalEmailAddress());
        List<String> customerNumbersList = pbmApi.getCustomerNumbers(customerDetails);
        mailMessage.setCustomerNumbers(customerNumbersList);
        return mailMessage;

      } else {
        throw new InvalidSubjectException(message.getSubject() + " has an Invalid Subject Line");
      }
    } catch (InvalidSubjectException e) {
      logger.warn("Mail: '{}' has an Invalid Subject Line", message.getSubject(), e);
      mailSender.informInvalidSubjectLine(message);
    } catch (MessagingException | IOException e) {
      logger.error("Exception caught: {}", e.getMessage(), e);
      // Reset Flags on Mail
      logger.debug("Resetting Flags for mail: {}", message.getSubject());
      message.setFlags(message.getFlags(), false);
    }

    return null;
  }

  private MailMessage getMailMessage(Message message) throws MessagingException, IOException {
    String forwardTime;
    String originalSendDate = null;
    String originalEmail = null;

    forwardTime = MailUtils.formatIso8601(message.getSentDate());
    processContent(message);
    mailMessage.setForwardDateTimeSent(forwardTime);
    mailMessage.setForwardEmailAddress(message.getFrom()[0].toString());
    mailMessage.setSubjectLine(message.getSubject());

    try {
      originalEmail = MailUtils.extractoriginalEmailAddress(mailMessage.getContent());
      mailMessage.setOriginalEmailAddress(originalEmail);

      originalSendDate = MailUtils.extractOriginalDateTime(mailMessage.getContent());
      mailMessage.setOriginalDateTimeSent(originalSendDate);

    } catch (NullPointerException e) {
      logger.info("No forwarded Email found.", e);
    }

    logger.info("Processed Mail - Subject: {}, dateSent: {} \n {}",
        mailMessage.getSubjectLine(), originalSendDate, mailMessage.getContent());

    return mailMessage;
  }

  public void sendMessage(Message message) throws MessagingException {
    if (messageSender != null) {
      message.setFlag(Flag.SEEN, true);

      try {
        messageSender.sendMessage(mailMessage);
        mailMessage.clearAttachments();
      } catch (AmqpException e) {
        logger.error("Mail couldn't be sent to rabbitMQ.", e);
        // Reset Flags on Mail
        message.setFlags(message.getFlags(), false);
      }
    }
  }

  // This method checks for content-type based on which, it processes and fetches the content of the message
  private void processContent(Part p) throws MessagingException, IOException {

    if (MailUtils.isAttachmentOrInline(p)) {
      saveAttachment(p);
    } else if (p.isMimeType("text/html")) {
      mailMessage.setHtml(p.getContent().toString());
      //check if the content has attachment
    } else if (p.isMimeType("text/plain")) {
      mailMessage.setContent(p.getContent().toString());
    } else if (p.isMimeType("multipart/*")) {
      Multipart mp = (Multipart) p.getContent();
      int count = mp.getCount();
      for (int i = 0; i < count; i++) {
        processContent(mp.getBodyPart(i));
      }
    } else {
      logger.info("This Is An Unknown Type\n{} \n{}", p.getDisposition(), p.getContent());
    }
  }

  // This method creates a mail attachment for the mail message
  private void saveAttachment(Part p) throws MessagingException, IOException {
    MailAttachment mailAttachment = new MailAttachment();
    mailAttachment.setFileName(p.getFileName());
    mailAttachment.setFileData(IOUtils.toByteArray(p.getInputStream()));
    mailMessage.addAttachment(mailAttachment);
  }
}
