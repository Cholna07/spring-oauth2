
package kh.edu.istad.platform.authorization.persistence.service;

import kh.edu.istad.platform.authorization.persistence.entity.OAuth2AuthorizationEntity;
import kh.edu.istad.platform.authorization.persistence.mapper.OAuth2AuthorizationMapper;
import kh.edu.istad.platform.authorization.persistence.repository.OAuth2AuthorizationJpaRepository;


import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class JpaOAuth2AuthorizationService
        implements OAuth2AuthorizationService {

    private final OAuth2AuthorizationMapper authorizationMapper;
    private final OAuth2AuthorizationJpaRepository authorizationRepository;
    private final RegisteredClientRepository registeredClientRepository;

    public JpaOAuth2AuthorizationService(
            OAuth2AuthorizationMapper authorizationMapper,
            OAuth2AuthorizationJpaRepository authorizationRepository,
            RegisteredClientRepository registeredClientRepository) {

        this.authorizationMapper = authorizationMapper;
        this.authorizationRepository = authorizationRepository;
        this.registeredClientRepository = registeredClientRepository;
    }

    @Override
    public void save(OAuth2Authorization authorization) {
        OAuth2AuthorizationEntity entity =
                authorizationMapper.toEntity(authorization);

        authorizationRepository.save(entity);
    }

    @Override
    public void remove(OAuth2Authorization authorization) {
        authorizationRepository.deleteById(authorization.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public OAuth2Authorization findById(String id) {
        return authorizationRepository.findById(id)
                .map(this::toAuthorization)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public OAuth2Authorization findByToken(
            String token,
            OAuth2TokenType tokenType) {

        OAuth2AuthorizationEntity entity = null;

        if (tokenType == null) {
            // Search all supported token values when the type is unknown.
            entity = authorizationRepository.findByState(token)
                    .orElseGet(() -> authorizationRepository
                            .findByAuthorizationCodeValue(token)
                            .orElseGet(() -> authorizationRepository
                                    .findByAccessTokenValue(token)
                                    .orElseGet(() -> authorizationRepository
                                            .findByRefreshTokenValue(token)
                                            .orElseGet(() -> authorizationRepository
                                                    .findByOidcIdTokenValue(token)
                                                    .orElseGet(() -> authorizationRepository
                                                            .findByUserCodeValue(token)
                                                            .orElseGet(() -> authorizationRepository
                                                                    .findByDeviceCodeValue(token)
                                                                    .orElse(null)))))));

        } else if (OAuth2TokenType.ACCESS_TOKEN.equals(tokenType)) {
            entity = authorizationRepository
                    .findByAccessTokenValue(token)
                    .orElse(null);

        } else if (OAuth2TokenType.REFRESH_TOKEN.equals(tokenType)) {
            entity = authorizationRepository
                    .findByRefreshTokenValue(token)
                    .orElse(null);

        } else if (new OAuth2TokenType(
            OAuth2ParameterNames.CODE
            ).equals(tokenType)) {
        entity = authorizationRepository
                .findByAuthorizationCodeValue(token)
                .orElse(null);

        } else if (OAuth2ParameterNames.STATE.equals(tokenType.getValue())) {
            entity = authorizationRepository
                    .findByState(token)
                    .orElse(null);
        }

        return entity == null ? null : toAuthorization(entity);
    }

    private OAuth2Authorization toAuthorization(
            OAuth2AuthorizationEntity entity) {

        RegisteredClient registeredClient =
                registeredClientRepository.findById(
                        entity.getRegisteredClientId());

        if (registeredClient == null) {
            throw new IllegalStateException(
                    "Registered client not found: "
                            + entity.getRegisteredClientId());
        }

        return authorizationMapper.toAuthorization(
                entity,
                registeredClient);
    }
}
