package com.calendar.server;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

public class Mail {
    private static final Logger LOG = Logger.getLogger(Mail.class);

    public static boolean send(String to, String subject, String text) {
        ApplicationContext context = new ClassPathXmlApplicationContext("mail.xml");

        JavaMailSender mailSender = context.getBean("mailSender", JavaMailSender.class);
        SimpleMailMessage templateMessage = context.getBean("templateMessage", SimpleMailMessage.class);

        SimpleMailMessage mailMessage = new SimpleMailMessage(templateMessage);

        mailMessage.setTo(to);
        mailMessage.setSubject(subject);
        mailMessage.setText(text);
        try {
            LOG.info(String.format("Send message to %s [%s]...", to, subject));
            mailSender.send(mailMessage);
        } catch (MailException mailException) {
            LOG.error("Mail sending failed!", mailException);
            return false;
        }
        return true;
    }
}
