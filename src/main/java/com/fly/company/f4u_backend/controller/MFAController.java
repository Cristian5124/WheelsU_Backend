package com.fly.company.f4u_backend.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fly.company.f4u_backend.service.MFAService;

@RestController
@RequestMapping("/api/mfa")
public class MFAController {

    @Autowired
    private MFAService mfaService;

    @PostMapping("/send")
    public ResponseEntity<?> sendMFACode(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Email es requerido"
                ));
            }

            mfaService.sendMFACode(email);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Código enviado exitosamente"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Error al enviar código: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyMFACode(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String code = request.get("code");
            
            if (email == null || code == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Email y código son requeridos"
                ));
            }

            boolean verified = mfaService.verifyMFACode(email, code);
            
            if (verified) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Código verificado exitosamente"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Código inválido o expirado"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Error al verificar código: " + e.getMessage()
            ));
        }
    }
}
