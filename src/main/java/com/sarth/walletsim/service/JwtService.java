package com.sarth.walletsim.service;

import com.sarth.walletsim.constants.UserRole;
import com.sarth.walletsim.entity.AppUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class JwtService {
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

    private final ObjectMapper objectMapper;
    private final String jwtSecret;
    private final long expirationSeconds;

    public JwtService(
            ObjectMapper objectMapper,
            @Value("${app.jwt.secret:wallet-simulator-local-dev-secret-key}") String jwtSecret,
            @Value("${app.jwt.expiration-seconds:86400}") long expirationSeconds
    ) {
        this.objectMapper = objectMapper;
        this.jwtSecret = jwtSecret;
        this.expirationSeconds = expirationSeconds;
    }

    public String generateToken(AppUser user) {
        Instant now = Instant.now();

        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", user.getEmail());
        payload.put("name", user.getFullName());
        payload.put("role", user.getRole().name());
        payload.put("iat", now.getEpochSecond());
        payload.put("exp", now.plusSeconds(expirationSeconds).getEpochSecond());

        String encodedHeader = encodeJson(header);
        String encodedPayload = encodeJson(payload);
        String unsignedToken = encodedHeader + "." + encodedPayload;

        return unsignedToken + "." + sign(unsignedToken);
    }

    public String extractEmail(String token) {
        return parseClaims(token).get("sub").toString();
    }

    public UserRole extractRole(String token) {
        return UserRole.valueOf(parseClaims(token).get("role").toString());
    }

    public boolean isTokenValid(String token) {
        Map<String, Object> claims = parseClaims(token);
        Number expiration = (Number) claims.get("exp");
        return expiration.longValue() > Instant.now().getEpochSecond();
    }

    private Map<String, Object> parseClaims(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid token format");
        }

        String unsignedToken = parts[0] + "." + parts[1];
        String expectedSignature = sign(unsignedToken);
        if (!constantTimeEquals(expectedSignature, parts[2])) {
            throw new IllegalArgumentException("Invalid token signature");
        }

        try {
            return objectMapper.readValue(URL_DECODER.decode(parts[1]), new TypeReference<>() {
            });
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid token payload");
        }
    }

    private String encodeJson(Map<String, Object> value) {
        try {
            return URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to encode token payload", ex);
        }
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return URL_ENCODER.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to sign token", ex);
        }
    }

    private boolean constantTimeEquals(String first, String second) {
        byte[] firstBytes = first.getBytes(StandardCharsets.UTF_8);
        byte[] secondBytes = second.getBytes(StandardCharsets.UTF_8);
        if (firstBytes.length != secondBytes.length) {
            return false;
        }

        int result = 0;
        for (int index = 0; index < firstBytes.length; index++) {
            result |= firstBytes[index] ^ secondBytes[index];
        }
        return result == 0;
    }
}
