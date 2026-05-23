package ejb;

import jakarta.ejb.Stateless;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

/**
 * EmailService is a stateless EJB that sends emails through the fake SMTP
 * server (FakeSMTP / CentreMail) running on localhost port 2525.
 *
 * I created this service as a reusable EJB because both the registration flow
 * (verification code) and the account recovery flow (recovery code) need to
 * send emails. Keeping the mail logic here avoids duplication across EJBs and
 * makes it easy to test with the FakeSMTP tool.
 *
 * To use FakeSMTP: download the jar from http://nilhcem.com/FakeSMTP/download.html
 * and run it with: java -jar fakeSMTP.jar -s -p 2525
 * The application sends from CENTRE@glassfish.com to the user email address.
 */
@Stateless
public class EmailService {

    private static final Logger LOGGER = Logger.getLogger(EmailService.class.getName());

    // FakeSMTP server listens on localhost port 2525 by default.
    private static final String SMTP_HOST = "localhost";
    private static final String SMTP_PORT = "2525";

    // Sender address shown in the FakeSMTP mail list.
    private static final String FROM_ADDRESS = "CENTRE@glassfish.com";

    /**
     * Sends the email verification code to the user during registration.
     * The assignment requires the system to send a verification code to the
     * user's email address so they can complete the registration form.
     *
     * @param toEmail   the recipient email address entered by the user
     * @param code      the randomly generated verification code
     * @return true if the email was sent successfully, false otherwise
     */
    public boolean sendVerificationCode(String toEmail, String code) {
        String subject = "The Verification Code";
        String body = "The Verification Code: " + code;
        return sendEmail(toEmail, subject, body);
    }

    /**
     * Sends the account recovery code to the user when they forget their
     * username or password. The user uses this code to reset their password.
     *
     * @param toEmail   the registered email address entered on the recovery page
     * @param code      the randomly generated recovery code
     * @return true if the email was sent successfully, false otherwise
     */
    public boolean sendRecoveryCode(String toEmail, String code) {
        String subject = "The Recovery Code";
        String body = "The Recovery Code: " + code;
        return sendEmail(toEmail, subject, body);
    }

    /**
     * Core email sending method. Connects to the FakeSMTP server and sends
     * a plain text email. This avoids the Terms and Conditions issues that
     * arise when using real SMTP servers like Gmail.
     *
     * @param toEmail   recipient address
     * @param subject   email subject line
     * @param body      plain text body of the email
     * @return true on success, false on any MessagingException
     */
    private boolean sendEmail(String toEmail, String subject, String body) {
        // Configure JavaMail to connect to the fake SMTP server.
        Properties props = new Properties();
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.auth", "false");
        props.put("mail.smtp.starttls.enable", "false");

        try {
            Session session = Session.getInstance(props, null);
            session.setDebug(false);

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_ADDRESS));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            LOGGER.log(Level.INFO, "Email sent to {0} with subject: {1}", new Object[]{toEmail, subject});
            return true;

        } catch (MessagingException ex) {
            LOGGER.log(Level.SEVERE, "Failed to send email to " + toEmail, ex);
            return false;
        }
    }
}
