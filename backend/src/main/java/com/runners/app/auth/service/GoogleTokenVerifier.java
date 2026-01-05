package com.runners.app.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;

@Service
public class GoogleTokenVerifier {

    private static final Logger log = LoggerFactory.getLogger(GoogleTokenVerifier.class);

    private final GoogleIdTokenVerifier verifier;
    private final String expectedAudience;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GoogleTokenVerifier(@Value("${google.oauth.web-client-id}") String webClientId) {
        this.expectedAudience = webClientId;
        this.verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                JacksonFactory.getDefaultInstance()
        )
        .setAudience(Collections.singletonList(webClientId))
        .build();
    }

    public GoogleIdToken.Payload verify(String idTokenString) {
        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                String message = buildInvalidTokenMessage(idTokenString);
                log.warn(message);
                throw new IllegalArgumentException(message);
            }
            return idToken.getPayload();
        } catch (Exception e) {
            String message = "Google token verification failed";
            String details = safeExtractUnverifiedDetails(idTokenString);
            if (details != null) {
                message += " (" + details + ")";
            }
            log.warn(message, e);
            throw new IllegalArgumentException(message, e);
        }
    }

    private String buildInvalidTokenMessage(String token) {
        String details = safeExtractUnverifiedDetails(token);
        if (details == null) {
            return "Invalid Google ID Token";
        }
        return "Invalid Google ID Token (" + details + ")";
    }

    private String safeExtractUnverifiedDetails(String token) {
        try {
            String[] parts = token == null ? new String[0] : token.split("\\.");
            if (parts.length < 2) {
                return null;
            }
            byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
            String payloadJson = new String(decoded, StandardCharsets.UTF_8);
            JsonNode payload = objectMapper.readTree(payloadJson);

            String aud = readStringOrFirstArrayItem(payload.path("aud"));
            String azp = payload.path("azp").asText(null);
            String iss = payload.path("iss").asText(null);
            String exp = payload.path("exp").asText(null);

            StringBuilder sb = new StringBuilder();
            if (aud != null) sb.append("tokenAud=").append(aud).append(", ");
            if (azp != null) sb.append("azp=").append(azp).append(", ");
            if (iss != null) sb.append("iss=").append(iss).append(", ");
            if (exp != null) sb.append("exp=").append(exp).append(", ");
            sb.append("expectedAud=").append(expectedAudience);
            return sb.toString();
        } catch (Exception ignored) {
            return null;
        }
    }

    private String readStringOrFirstArrayItem(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        if (node.isArray() && node.size() > 0) {
            JsonNode first = node.get(0);
            return first == null || first.isNull() ? null : first.asText(null);
        }
        return node.asText(null);
    }
}
