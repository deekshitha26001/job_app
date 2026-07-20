package backend.demo.service;

import backend.demo.entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.text.SimpleDateFormat;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    /**
     * Sends an email using a Thymeleaf template.
     */
    private void sendHtmlEmail(String to, String subject, String templateName, Context context) {
        if (fromEmail == null || fromEmail.isBlank() || fromEmail.startsWith("$")) {
            log.warn("Email sending is disabled because spring.mail.username is not configured.");
            log.warn("--- MOCK EMAIL ---");
            log.warn("To: {}", to);
            log.warn("Subject: {}", subject);
            if (context.containsVariable("resetUrl")) {
                log.warn("Reset Link: {}", context.getVariable("resetUrl"));
            }
            log.warn("------------------");
            return;
        }

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String htmlContent = templateEngine.process(templateName, context);

            helper.setFrom(fromEmail, "Job Portal Team");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            javaMailSender.send(message);
            log.info("Sent email '{}' to {}", subject, to);
        } catch (Exception e) {
            log.error("Failed to send email to {}", to, e);
        }
    }

    /**
     * Sends a login notification email.
     */
    public void sendLoginNotification(User user, String loginMethod, String ipAddress, String device) {
        Context context = new Context();
        context.setVariable("name", user.getName());
        context.setVariable("loginMethod", loginMethod);
        context.setVariable("dateTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        context.setVariable("ipAddress", ipAddress != null && !ipAddress.isEmpty() ? ipAddress : "Unknown IP");
        context.setVariable("device", device != null && !device.isEmpty() ? device : "Unknown Device");

        sendHtmlEmail(
                user.getEmail(),
                "New Login to Your Job Portal Account",
                "login-notification",
                context
        );
    }

    /**
     * Sends a password reset email.
     */
    public void sendPasswordResetEmail(User user, String resetUrl) {
        Context context = new Context();
        context.setVariable("name", user.getName());
        context.setVariable("resetUrl", resetUrl);

        sendHtmlEmail(
                user.getEmail(),
                "Reset Your Job Portal Password",
                "password-reset",
                context
        );
    }

    /**
     * Sends a confirmation email that the password has been changed.
     */
    public void sendPasswordChangedSuccessfullyEmail(User user) {
        Context context = new Context();
        context.setVariable("name", user.getName());

        sendHtmlEmail(
                user.getEmail(),
                "Your Password Has Been Reset",
                "password-changed",
                context
        );
    }
}
