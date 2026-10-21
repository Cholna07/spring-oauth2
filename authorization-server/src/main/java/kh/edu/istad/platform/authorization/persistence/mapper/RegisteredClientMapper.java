
package kh.edu.istad.platform.authorization.persistence.mapper;

import kh.edu.istad.platform.authorization.persistence.entity.RegisteredClientEntity;

import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Component
public class RegisteredClientMapper {

    private static final String REQUIRE_PROOF_KEY =
            "settings.client.require-proof-key";

    private static final String REQUIRE_AUTHORIZATION_CONSENT =
            "settings.client.require-authorization-consent";

    private final ObjectMapper objectMapper;

    public RegisteredClientMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public RegisteredClientEntity toEntity(RegisteredClient registeredClient) {
        RegisteredClientEntity entity = new RegisteredClientEntity();

        entity.setId(registeredClient.getId());
        entity.setClientId(registeredClient.getClientId());
        entity.setClientIdIssuedAt(registeredClient.getClientIdIssuedAt());
        entity.setClientSecret(registeredClient.getClientSecret());
        entity.setClientSecretExpiresAt(
                registeredClient.getClientSecretExpiresAt());
        entity.setClientName(registeredClient.getClientName());

        entity.setClientAuthenticationMethods(writeJson(
                registeredClient.getClientAuthenticationMethods()
                        .stream()
                        .map(ClientAuthenticationMethod::getValue)
                        .toList()));

        entity.setAuthorizationGrantTypes(writeJson(
                registeredClient.getAuthorizationGrantTypes()
                        .stream()
                        .map(AuthorizationGrantType::getValue)
                        .toList()));

        entity.setRedirectUris(writeJson(registeredClient.getRedirectUris()));
        entity.setPostLogoutRedirectUris(
                writeJson(registeredClient.getPostLogoutRedirectUris()));
        entity.setScopes(writeJson(registeredClient.getScopes()));

        entity.setClientSettings(
                writeJson(registeredClient.getClientSettings().getSettings()));

        entity.setTokenSettings(
                writeTokenSettings(registeredClient.getTokenSettings()));

        return entity;
    }

    public RegisteredClient toRegisteredClient(RegisteredClientEntity entity) {
        RegisteredClient.Builder builder =
                RegisteredClient.withId(entity.getId())
                        .clientId(entity.getClientId());

        if (entity.getClientIdIssuedAt() != null) {
            builder.clientIdIssuedAt(entity.getClientIdIssuedAt());
        }

        if (entity.getClientSecret() != null) {
            builder.clientSecret(entity.getClientSecret());
        }

        if (entity.getClientSecretExpiresAt() != null) {
            builder.clientSecretExpiresAt(entity.getClientSecretExpiresAt());
        }

        if (entity.getClientName() != null) {
            builder.clientName(entity.getClientName());
        }

        readStringSet(entity.getClientAuthenticationMethods())
                .forEach(value -> builder.clientAuthenticationMethod(
                        new ClientAuthenticationMethod(value)));

        readStringSet(entity.getAuthorizationGrantTypes())
                .forEach(value -> builder.authorizationGrantType(
                        new AuthorizationGrantType(value)));

        readStringSet(entity.getRedirectUris())
                .forEach(builder::redirectUri);

        readStringSet(entity.getPostLogoutRedirectUris())
                .forEach(builder::postLogoutRedirectUri);

        readStringSet(entity.getScopes()).forEach(builder::scope);

        builder.clientSettings(
                readClientSettings(entity.getClientSettings()));

        builder.tokenSettings(
                readTokenSettings(entity.getTokenSettings()));

        return builder.build();
    }

    private ClientSettings readClientSettings(String json) {
        Map<String, Object> settings = readJson(json);

        ClientSettings.Builder builder = ClientSettings.builder();

        Object requireProofKey = settings.get(REQUIRE_PROOF_KEY);
        if (requireProofKey instanceof Boolean value) {
            builder.requireProofKey(value);
        }

        Object requireAuthorizationConsent =
                settings.get(REQUIRE_AUTHORIZATION_CONSENT);

        if (requireAuthorizationConsent instanceof Boolean value) {
            builder.requireAuthorizationConsent(value);
        }

        return builder.build();
    }

    private String writeTokenSettings(TokenSettings tokenSettings) {
        Map<String, Object> settings = new LinkedHashMap<>();

        tokenSettings.getSettings().forEach((key, value) -> {
            if (value instanceof Duration duration) {
                settings.put(key, duration.toString());
            } else if (value instanceof OAuth2TokenFormat format) {
                settings.put(key, format.getValue());
            } else if (value instanceof Enum<?> enumValue) {
                settings.put(key, enumValue.name());
            } else {
                settings.put(key, value);
            }
        });

        return writeJson(settings);
    }


    private TokenSettings readTokenSettings(String json) {
        Map<String, Object> settings = readJson(json);

        TokenSettings.Builder builder = TokenSettings.builder();

        Object accessTokenFormat =
                settings.get("settings.token.access-token-format");

        if (accessTokenFormat instanceof String value) {
            builder.accessTokenFormat(new OAuth2TokenFormat(value));
        }

        Object accessTokenTtl =
                settings.get("settings.token.access-token-time-to-live");

        if (accessTokenTtl instanceof String value) {
            builder.accessTokenTimeToLive(Duration.parse(value));
        }

        Object refreshTokenTtl =
                settings.get("settings.token.refresh-token-time-to-live");

        if (refreshTokenTtl instanceof String value) {
            builder.refreshTokenTimeToLive(Duration.parse(value));
        }

        Object authorizationCodeTtl =
                settings.get("settings.token.authorization-code-time-to-live");

        if (authorizationCodeTtl instanceof String value) {
            builder.authorizationCodeTimeToLive(Duration.parse(value));
        }

        Object deviceCodeTtl =
                settings.get("settings.token.device-code-time-to-live");

        if (deviceCodeTtl instanceof String value) {
            builder.deviceCodeTimeToLive(Duration.parse(value));
        }

        Object reuseRefreshTokens =
                settings.get("settings.token.reuse-refresh-tokens");

        if (reuseRefreshTokens instanceof Boolean value) {
            builder.reuseRefreshTokens(value);
        }

        return builder.build();
    }




    private Set<String> readStringSet(String value) {
        Set<String> result = new LinkedHashSet<>();

        if (value == null || value.isBlank()) {
            return result;
        }

        String trimmed = value.trim();

        // JSON array: ["authorization_code", "refresh_token"]
        if (trimmed.startsWith("[")) {
            return new LinkedHashSet<>(
                    objectMapper.readValue(
                            trimmed,
                            new TypeReference<Set<String>>() {}
                    )
            );
        }

        // Legacy comma-separated values:
        // authorization_code,refresh_token
        if (trimmed.contains(",")) {
            for (String item : trimmed.split(",")) {
                if (!item.isBlank()) {
                    result.add(item.trim());
                }
            }
            return result;
        }

        // Single plain-text value
        result.add(trimmed);
        return result;
    }



    private Map<String, Object> readJson(String json) {
        if (json == null || json.isBlank()) {
            return new LinkedHashMap<>();
        }

        return objectMapper.readValue(
                json,
                new TypeReference<Map<String, Object>>() {
                });
    }

    private String writeJson(Object value) {
        return objectMapper.writeValueAsString(value);
    }
}
