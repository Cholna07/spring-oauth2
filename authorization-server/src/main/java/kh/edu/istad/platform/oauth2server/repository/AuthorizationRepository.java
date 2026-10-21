package kh.edu.istad.platform.oauth2server.repository;

import kh.edu.istad.platform.oauth2server.entity.OAuth2Authorization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthorizationRepository
        extends JpaRepository<OAuth2Authorization, Long> {

    Optional<OAuth2Authorization> findByAuthorizationId(String authorizationId);

    Optional<OAuth2Authorization> findByState(String state);

    Optional<OAuth2Authorization> findByAuthorizationCodeValue(String value);

    Optional<OAuth2Authorization> findByAccessTokenValue(String value);

    Optional<OAuth2Authorization> findByRefreshTokenValue(String value);
}