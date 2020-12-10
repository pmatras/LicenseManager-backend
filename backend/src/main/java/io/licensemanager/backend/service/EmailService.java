package io.licensemanager.backend.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(UserAccountService.class);

    @Value("${mail.no-reply.address:no-reply@license-manager.io}")
    private String NO_REPLY_EMAIL_ADDRESS;
    private final JavaMailSender mailSender;

    public void sendEmailMessage(String subject, String recipient, String content) {
        SimpleMailMessage emailMessage = new SimpleMailMessage();
        emailMessage.setFrom(NO_REPLY_EMAIL_ADDRESS);
        emailMessage.setTo(recipient);
        emailMessage.setSubject(subject);
        emailMessage.setText(content);

        mailSender.send(emailMessage);
    }

    public void sendEmailMessageToMultipleRecipients(final String subject, final List<String> recipients, final String content) {
        SimpleMailMessage emailMessage = new SimpleMailMessage();
        emailMessage.setFrom(NO_REPLY_EMAIL_ADDRESS);
        emailMessage.setTo(recipients.toArray(new String[recipients.size()]));
        emailMessage.setSubject(subject);
        emailMessage.setCc();
        emailMessage.setText(content);

        mailSender.send(emailMessage);
    }

    public boolean sendHtmlEmailMessage(String subject, String recipient, String content) {
        MimeMessage emailMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(emailMessage, true);
            helper.setFrom(NO_REPLY_EMAIL_ADDRESS);
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(content, true);

            mailSender.send(emailMessage);
        } catch (MessagingException e) {
            logger.error("Failed to send e-mail, reason: {}", e.getMessage());

            return false;
        }

        return true;
    }
}
