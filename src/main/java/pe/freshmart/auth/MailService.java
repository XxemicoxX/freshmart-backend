package pe.freshmart.auth;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {
    private final ObjectProvider<JavaMailSender> sender;
    private final boolean enabled;
    private final String from;

    public MailService(ObjectProvider<JavaMailSender> sender,
                       @Value("${app.mail.enabled:false}") boolean enabled,
                       @Value("${app.mail.from:noreply@freshmart.local}") String from) {
        this.sender = sender;
        this.enabled = enabled;
        this.from = from;
    }

    public void enviar(String destino, String asunto, String contenido) {
        if (!enabled) return;
        JavaMailSender mailSender = sender.getIfAvailable();
        if (mailSender == null) throw new IllegalStateException("El envío está habilitado, pero no hay servidor SMTP configurado.");
        var mensaje = new SimpleMailMessage();
        mensaje.setFrom(from);
        mensaje.setTo(destino);
        mensaje.setSubject(asunto);
        mensaje.setText(contenido);
        mailSender.send(mensaje);
    }
}
