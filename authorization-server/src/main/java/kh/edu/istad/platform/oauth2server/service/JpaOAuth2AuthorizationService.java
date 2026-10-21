package kh.edu.istad.platform.oauth2server.service;

import kh.edu.istad.platform.oauth2server.repository.AuthorizationRepository;
import org.springframework.stereotype.Service;

@Service
public class JpaOAuth2AuthorizationService {

    private final AuthorizationRepository authorizationRepository;

    public JpaOAuth2AuthorizationService(
            AuthorizationRepository authorizationRepository) {
        this.authorizationRepository = authorizationRepository;
    }

    public void deleteByAuthorizationId(String authorizationId) {
        authorizationRepository.findByAuthorizationId(authorizationId)
                .ifPresent(authorizationRepository::delete);
    }
}