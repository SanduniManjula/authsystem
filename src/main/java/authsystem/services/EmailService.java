package authsystem.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender emailSender;
    public void sendSummaryEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
      //  message.setFrom("sandunimanju.27@gmail.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
    }
     /*
    public void sendSummaryEmail(String to, String subject, String text, byte[] userReport, byte[] roleReport) {
        MimeMessage message = emailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text);


            helper.addAttachment("users_report.pdf", new ByteArrayResource(userReport));

            helper.addAttachment("roles_report.pdf", new ByteArrayResource(roleReport));

            emailSender.send(message);
            System.out.println("Email sent successfully with attachments.");

        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("Failed to send email with attachments.");
        }
    }
      */
}

