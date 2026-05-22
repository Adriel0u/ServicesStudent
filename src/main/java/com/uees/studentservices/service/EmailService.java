package com.uees.studentservices.service;

import com.uees.studentservices.config.AppProperties;
import com.uees.studentservices.model.Certificate;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.UnsupportedEncodingException;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final AppProperties props;

    public EmailService(JavaMailSender mailSender,
                        SpringTemplateEngine templateEngine,
                        AppProperties props) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.props = props;
    }

    @Async
    public void sendWelcomeEmail(String email, String fullName) {
        try {
            Context ctx = new Context(new Locale("es", "ES"));
            ctx.setVariable("fullName", fullName);
            ctx.setVariable("platformName", props.getMail().getFromName());
            String html = templateEngine.process("welcome-email", ctx);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(buildFrom());
            helper.setTo(email);
            helper.setSubject("Bienvenido/a a " + props.getMail().getFromName());
            helper.setText(html, true);

            mailSender.send(message);
            log.info("[email] welcome enviado a {}", email);
        } catch (Exception ex) {
            log.warn("[email] no se pudo enviar welcome a {}: {}", email, ex.getMessage());
        }
    }

    @Async
    public void sendEnrollmentWelcome(String email, String fullName, String courseName) {
        try {
            Context ctx = new Context(new Locale("es", "ES"));
            ctx.setVariable("fullName", fullName);
            ctx.setVariable("courseName", courseName);
            ctx.setVariable("platformName", props.getMail().getFromName());
            String html = templateEngine.process("enrollment-welcome", ctx);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(buildFrom());
            helper.setTo(email);
            helper.setSubject("Inscripcion activada: " + courseName);
            helper.setText(html, true);

            mailSender.send(message);
            log.info("[email] enrollment welcome enviado a {} (curso={})", email, courseName);
        } catch (Exception ex) {
            log.warn("[email] no se pudo enviar enrollment-welcome a {}: {}", email, ex.getMessage());
        }
    }

    /**
     * Envia el certificado HTML al estudiante usando Thymeleaf.
     * Retorna true si el envio fue exitoso.
     */
    public boolean sendCertificateEmail(Certificate certificate) {
        try {
            Context ctx = new Context(new Locale("es", "ES"));
            ctx.setVariable("studentName", certificate.getStudentName());
            ctx.setVariable("courseName", certificate.getCourseName());
            ctx.setVariable("issuedAt", certificate.getIssuedAt().format(DATE_FMT));
            ctx.setVariable("certificateCode", certificate.getCertificateCode());
            ctx.setVariable("platformName", props.getMail().getFromName());
            String html = templateEngine.process("certificate", ctx);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(buildFrom());
            helper.setTo(certificate.getStudentEmail());
            helper.setSubject("Tu certificado: " + certificate.getCourseName());
            helper.setText(html, true);

            mailSender.send(message);
            log.info("[email] certificado {} enviado a {}", certificate.getCertificateCode(), certificate.getStudentEmail());
            return true;
        } catch (MessagingException | UnsupportedEncodingException ex) {
            log.error("[email] error enviando certificado a {}: {}",
                    certificate.getStudentEmail(), ex.getMessage(), ex);
            return false;
        } catch (Exception ex) {
            log.error("[email] error inesperado enviando certificado: {}", ex.getMessage(), ex);
            return false;
        }
    }

    private InternetAddress buildFrom() throws UnsupportedEncodingException {
        return new InternetAddress(props.getMail().getFrom(), props.getMail().getFromName(), "UTF-8");
    }
}
