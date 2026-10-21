package kh.edu.istad.platform.oauth2server.repository;

import kh.edu.istad.platform.oauth2server.entity.OAuth2Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OAuth2ClientRepository
        extends JpaRepository<OAuth2Client, Long> {

    Optional<OAuth2Client> findByClientId(String clientId);

    Optional<OAuth2Client> findByRegisteredClientId(String registeredClientId);
}