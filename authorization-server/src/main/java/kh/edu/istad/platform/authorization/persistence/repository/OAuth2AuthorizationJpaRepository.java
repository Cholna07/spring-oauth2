
package kh.edu.istad.platform.authorization.persistence.repository;

import kh.edu.istad.platform.authorization.persistence.entity.OAuth2AuthorizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OAuth2AuthorizationJpaRepository extends JpaRepository<OAuth2AuthorizationEntity, String> {

    Optional<OAuth2AuthorizationEntity> findByState(String state);

    Optional<OAuth2AuthorizationEntity> findByAuthorizationCodeValue(String value);

    Optional<OAuth2AuthorizationEntity> findByAccessTokenValue(String value);

    Optional<OAuth2AuthorizationEntity> findByRefreshTokenValue(String value);

    Optional<OAuth2AuthorizationEntity> findByOidcIdTokenValue(String value);

    Optional<OAuth2AuthorizationEntity> findByUserCodeValue(String value);

    Optional<OAuth2AuthorizationEntity> findByDeviceCodeValue(String value);
}
