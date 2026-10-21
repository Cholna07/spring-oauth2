package kh.edu.istad.platform.oauth2server.repository;

import kh.edu.istad.platform.oauth2server.entity.OAuth2Consent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConsentRepository
        extends JpaRepository<OAuth2Consent, Long> {

    Optional<OAuth2Consent>
    findByRegisteredClientIdAndPrincipalName(
            String registeredClientId,
            String principalName);
}