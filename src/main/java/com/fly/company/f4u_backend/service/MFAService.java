package com.fly.company.f4u_backend.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.fly.company.f4u_backend.model.MFACode;

@Service
public class MFAService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@wheelsu.com}")
    private String fromEmail;

    // Almacenamiento en memoria de códigos MFA
    private final Map<String, MFACode> mfaCodes = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    /**
     * Genera un código de 6 dígitos
     */
    private String generateCode() {
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    /**
     * Envía código MFA por email
     */
    public void sendMFACode(String email) {
        // Generar código
        String code = generateCode();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);

        // Guardar en memoria
        MFACode mfaCode = new MFACode(email, code, expiresAt);
        mfaCodes.put(email, mfaCode);

        // Enviar email
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("WheelsU - Código de verificación");
        message.setText(
            "Tu código de verificación es: " + code + "\n\n" +
            "Este código expira en 5 minutos.\n\n" +
            "Si no solicitaste este código, ignora este mensaje."
        );

        mailSender.send(message);
    }

    /**
     * Verifica el código MFA
     */
    public boolean verifyMFACode(String email, String code) {
        MFACode mfaCode = mfaCodes.get(email);

        if (mfaCode == null) {
            return false;
        }

        if (mfaCode.isExpired()) {
            mfaCodes.remove(email);
            return false;
        }

        if (!mfaCode.getCode().equals(code)) {
            return false;
        }

        // Marcar como verificado
        mfaCode.setVerified(true);
        return true;
    }

    /**
     * Verifica si el usuario ya completó MFA
     */
    public boolean isVerified(String email) {
        MFACode mfaCode = mfaCodes.get(email);
        return mfaCode != null && mfaCode.isVerified() && !mfaCode.isExpired();
    }

    /**
     * Limpia el código MFA del usuario
     */
    public void clearMFACode(String email) {
        mfaCodes.remove(email);
    }
}
