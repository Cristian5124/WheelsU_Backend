package com.fly.company.f4u_backend.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/**
 * Configuración personalizada del decodificador JWT para modo multi-tenant
 * Valida audience, issuer y timestamp del token
 */
@Configuration
public class JwtDecoderConfig {
    
    @Value("${spring.security.oauth2.resourceserver.jwt.audiences}")
    private String expectedAudience;
    
    @Value("${azure.activedirectory.client-id}")
    private String clientId;
    
    @Value("${azure.activedirectory.tenant-id}")
    private String tenantId;

    @Bean
    public JwtDecoder jwtDecoder() {
        // Usar el tenant específico en lugar de /common/ para evitar problemas de validación
        String jwkSetUri = String.format(
            "https://login.microsoftonline.com/%s/discovery/v2.0/keys",
            tenantId
        );
        
        System.out.println("🔑 JwtDecoder configurado con JWK URI: " + jwkSetUri);
        
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        
        // Validar timestamp
        OAuth2TokenValidator<Jwt> withTimestamp = new JwtTimestampValidator();
        
        // Validar audience - acepta tanto el formato api:// como el Client ID solo
        OAuth2TokenValidator<Jwt> withAudience = new JwtClaimValidator<List<String>>(
            JwtClaimNames.AUD,
            aud -> aud != null && (
                aud.contains(expectedAudience) || 
                aud.contains(clientId)
            )
        );
        
        // Validar que el issuer sea de Microsoft (cualquier tenant)
        // Acepta AMBOS formatos: con y sin slash final, y ambos dominios (sts.windows.net y login.microsoftonline.com)
        OAuth2TokenValidator<Jwt> withIssuer = new JwtClaimValidator<String>(
            JwtClaimNames.ISS,
            iss -> {
                if (iss == null) return false;
                // Remover slash final para comparación
                String normalizedIss = iss.endsWith("/") ? iss.substring(0, iss.length() - 1) : iss;
                return normalizedIss.startsWith("https://login.microsoftonline.com/") ||
                       normalizedIss.startsWith("https://sts.windows.net/");
            }
        );
        
        jwtDecoder.setJwtValidator(
            new DelegatingOAuth2TokenValidator<>(withTimestamp, withAudience, withIssuer)
        );
        
        return jwtDecoder;
    }
}
