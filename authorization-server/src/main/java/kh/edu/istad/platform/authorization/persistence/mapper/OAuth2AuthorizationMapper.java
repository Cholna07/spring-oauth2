package kh.edu.istad.platform.authorization.persistence.mapper;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import kh.edu.istad.platform.authorization.persistence.entity.OAuth2AuthorizationEntity;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class OAuth2AuthorizationMapper {

    private static final String AUTHORIZATION_REQUEST_ATTRIBUTE =
            OAuth2AuthorizationRequest.class.getName();

    private static final String PRINCIPAL_ATTRIBUTE =
            Principal.class.getName();

    private final ObjectMapper objectMapper;
    private final UserDetailsService userDetailsService;

    public OAuth2AuthorizationMapper(
            ObjectMapper objectMapper,
            UserDetailsService userDetailsService) {
        this.objectMapper = objectMapper;
        this.userDetailsService = userDetailsService;
    }

    public OAuth2AuthorizationEntity toEntity(
            OAuth2Authorization authorization) {

        OAuth2AuthorizationEntity entity =
                new OAuth2AuthorizationEntity();

        entity.setId(authorization.getId());
        entity.setRegisteredClientId(
                authorization.getRegisteredClientId());
        entity.setPrincipalName(authorization.getPrincipalName());
        entity.setAuthorizationGrantType(
                authorization.getAuthorizationGrantType().getValue());
        entity.setAuthorizedScopes(
                String.join(",", authorization.getAuthorizedScopes()));

        // Store OAuth2AuthorizationRequest separately so it can be restored
        // as its original Java type.
        Map<String, Object> attributes =
                new HashMap<>(authorization.getAttributes());

        OAuth2AuthorizationRequest authorizationRequest =
                authorization.getAttribute(
                        AUTHORIZATION_REQUEST_ATTRIBUTE);

        if (authorizationRequest != null) {
            entity.setAuthorizationRequest(
                    writeAuthorizationRequest(authorizationRequest));

            attributes.remove(AUTHORIZATION_REQUEST_ATTRIBUTE);
        }

        // Do not serialize Authentication as JSON. Jackson would restore
        // it as a LinkedHashMap, causing a ClassCastException during
        // authorization-code token exchange.
        attributes.remove(PRINCIPAL_ATTRIBUTE);

        entity.setAttributes(writeJson(attributes));

        entity.setState(
                authorization.getAttribute(OAuth2ParameterNames.STATE));

        // Authorization code
        OAuth2Authorization.Token<OAuth2AuthorizationCode> code =
                authorization.getToken(OAuth2AuthorizationCode.class);

        if (code != null) {
            entity.setAuthorizationCodeValue(
                    code.getToken().getTokenValue());
            entity.setAuthorizationCodeIssuedAt(
                    code.getToken().getIssuedAt());
            entity.setAuthorizationCodeExpiresAt(
                    code.getToken().getExpiresAt());
            entity.setAuthorizationCodeMetadata(
                    writeJson(code.getMetadata()));
        }

        // Access token
        OAuth2Authorization.Token<OAuth2AccessToken> access =
                authorization.getToken(OAuth2AccessToken.class);

        if (access != null) {
            OAuth2AccessToken token = access.getToken();

            entity.setAccessTokenValue(token.getTokenValue());
            entity.setAccessTokenIssuedAt(token.getIssuedAt());
            entity.setAccessTokenExpiresAt(token.getExpiresAt());
            entity.setAccessTokenType(token.getTokenType().getValue());
            entity.setAccessTokenScopes(
                    String.join(",", token.getScopes()));
            entity.setAccessTokenMetadata(
                    writeJson(access.getMetadata()));
        }

        // Refresh token
        OAuth2Authorization.Token<OAuth2RefreshToken> refresh =
                authorization.getToken(OAuth2RefreshToken.class);

        if (refresh != null) {
            OAuth2RefreshToken token = refresh.getToken();

            entity.setRefreshTokenValue(token.getTokenValue());
            entity.setRefreshTokenIssuedAt(token.getIssuedAt());
            entity.setRefreshTokenExpiresAt(token.getExpiresAt());
            entity.setRefreshTokenMetadata(
                    writeJson(refresh.getMetadata()));
        }

        // OIDC ID token
        OAuth2Authorization.Token<OidcIdToken> idToken =
                authorization.getToken(OidcIdToken.class);

        if (idToken != null) {
            OidcIdToken token = idToken.getToken();

            entity.setOidcIdTokenValue(token.getTokenValue());
            entity.setOidcIdTokenIssuedAt(token.getIssuedAt());
            entity.setOidcIdTokenExpiresAt(token.getExpiresAt());
            entity.setOidcIdTokenMetadata(
                    writeJson(idToken.getMetadata()));
            entity.setOidcIdTokenClaims(
                    writeJson(token.getClaims()));
        }

        return entity;
    }

    public OAuth2Authorization toAuthorization(
            OAuth2AuthorizationEntity entity,
            RegisteredClient registeredClient) {

        OAuth2Authorization.Builder builder =
                OAuth2Authorization.withRegisteredClient(registeredClient)
                        .id(entity.getId())
                        .principalName(entity.getPrincipalName())
                        .authorizationGrantType(
                                new AuthorizationGrantType(
                                        entity.getAuthorizationGrantType()))
                        .authorizedScopes(
                                split(entity.getAuthorizedScopes()))
                        .attributes(attributes ->
                                attributes.putAll(
                                        readJson(entity.getAttributes())));

        // Restore the authorization request with its correct Java type.
        OAuth2AuthorizationRequest authorizationRequest =
                readAuthorizationRequest(
                        entity.getAuthorizationRequest());

        if (authorizationRequest != null) {
            builder.attribute(
                    AUTHORIZATION_REQUEST_ATTRIBUTE,
                    authorizationRequest);
        }

        // Rebuild Authentication from the user details stored in the
        // configured UserDetailsService instead of deserializing it from JSON.
        //
        // This is needed for the authorization-code token exchange, where
        // Spring Authorization Server expects an Authentication attribute.
        if (AuthorizationGrantType.AUTHORIZATION_CODE.getValue()
                .equals(entity.getAuthorizationGrantType())) {

            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(
                            entity.getPrincipalName());

            Authentication authentication =
                    UsernamePasswordAuthenticationToken.authenticated(
                            userDetails,
                            null,
                            userDetails.getAuthorities());

            builder.attribute(PRINCIPAL_ATTRIBUTE, authentication);
        }

        // Restore state
        if (entity.getState() != null) {
            builder.attribute(
                    OAuth2ParameterNames.STATE,
                    entity.getState());
        }

        // Restore authorization code
        if (entity.getAuthorizationCodeValue() != null) {
            OAuth2AuthorizationCode code =
                    new OAuth2AuthorizationCode(
                            entity.getAuthorizationCodeValue(),
                            entity.getAuthorizationCodeIssuedAt(),
                            entity.getAuthorizationCodeExpiresAt());

            builder.token(code, metadata ->
                    metadata.putAll(
                            readJson(
                                    entity.getAuthorizationCodeMetadata())));
        }

        // Restore access token
        if (entity.getAccessTokenValue() != null) {
            OAuth2AccessToken accessToken =
                    new OAuth2AccessToken(
                            OAuth2AccessToken.TokenType.BEARER,
                            entity.getAccessTokenValue(),
                            entity.getAccessTokenIssuedAt(),
                            entity.getAccessTokenExpiresAt(),
                            split(entity.getAccessTokenScopes()));

            builder.token(accessToken, metadata ->
                    metadata.putAll(
                            readJson(
                                    entity.getAccessTokenMetadata())));
        }

        // Restore refresh token
        if (entity.getRefreshTokenValue() != null) {
            OAuth2RefreshToken refreshToken =
                    new OAuth2RefreshToken(
                            entity.getRefreshTokenValue(),
                            entity.getRefreshTokenIssuedAt(),
                            entity.getRefreshTokenExpiresAt());

            builder.token(refreshToken, metadata ->
                    metadata.putAll(
                            readJson(
                                    entity.getRefreshTokenMetadata())));
        }

        // Restore OIDC ID token
        if (entity.getOidcIdTokenValue() != null) {
            Map<String, Object> claims =
                    readJson(entity.getOidcIdTokenClaims());

            OidcIdToken idToken =
                    new OidcIdToken(
                            entity.getOidcIdTokenValue(),
                            entity.getOidcIdTokenIssuedAt(),
                            entity.getOidcIdTokenExpiresAt(),
                            claims);

            builder.token(idToken, metadata ->
                    metadata.putAll(
                            readJson(
                                    entity.getOidcIdTokenMetadata())));
        }

        return builder.build();
    }

    private String writeAuthorizationRequest(
            OAuth2AuthorizationRequest request) {

        Map<String, Object> data = new LinkedHashMap<>();

        data.put("authorizationUri", request.getAuthorizationUri());
        data.put("clientId", request.getClientId());
        data.put("redirectUri", request.getRedirectUri());
        data.put("scopes", request.getScopes());
        data.put("state", request.getState());
        data.put(
                "additionalParameters",
                request.getAdditionalParameters());
        data.put("attributes", request.getAttributes());

        return writeJson(data);
    }

    private OAuth2AuthorizationRequest readAuthorizationRequest(
            String json) {

        if (json == null || json.isBlank()) {
            return null;
        }

        Map<String, Object> data = readJson(json);
        Set<String> scopes = new LinkedHashSet<>();

        Object scopesObject = data.get("scopes");

        if (scopesObject instanceof Iterable<?> iterable) {
            for (Object scope : iterable) {
                if (scope instanceof String value) {
                    scopes.add(value);
                }
            }
        }

        Map<String, Object> additionalParameters =
                readObjectMap(data.get("additionalParameters"));

        Map<String, Object> attributes =
                readObjectMap(data.get("attributes"));

        OAuth2AuthorizationRequest.Builder builder =
                OAuth2AuthorizationRequest.authorizationCode();

        builder.authorizationUri(
                (String) data.get("authorizationUri"));
        builder.clientId((String) data.get("clientId"));
        builder.redirectUri((String) data.get("redirectUri"));
        builder.scopes(scopes);
        builder.state((String) data.get("state"));
        builder.additionalParameters(
                parameters -> parameters.putAll(additionalParameters));
        builder.attributes(
                requestAttributes -> requestAttributes.putAll(attributes));

        return builder.build();
    }

    private Map<String, Object> readObjectMap(Object value) {
        if (!(value instanceof Map<?, ?> map)) {
            return Collections.emptyMap();
        }

        Map<String, Object> result = new LinkedHashMap<>();

        map.forEach((key, item) -> {
            if (key instanceof String stringKey) {
                result.put(stringKey, item);
            }
        });

        return result;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    "Could not serialize authorization data",
                    ex);
        }
    }

    private Map<String, Object> readJson(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }

        try {
            return objectMapper.readValue(
                    json,
                    new TypeReference<Map<String, Object>>() {});
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    "Could not deserialize authorization data",
                    ex);
        }
    }

    private Set<String> split(String values) {
        if (values == null || values.isBlank()) {
            return Collections.emptySet();
        }

        return Arrays.stream(values.split(","))
                .filter(value -> !value.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
