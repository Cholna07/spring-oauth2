package kh.edu.istad.platform.oauth2server.service;

import kh.edu.istad.platform.oauth2server.entity.OAuth2Consent;
import kh.edu.istad.platform.oauth2server.repository.ConsentRepository;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JpaOAuth2AuthorizationConsentService
        implements OAuth2AuthorizationConsentService {

    private final ConsentRepository consentRepository;

    public JpaOAuth2AuthorizationConsentService(
            ConsentRepository consentRepository) {
        this.consentRepository = consentRepository;
    }

    @Override
    @Transactional
    public void save(OAuth2AuthorizationConsent authorizationConsent) {
        OAuth2Consent consent = consentRepository
                .findByRegisteredClientIdAndPrincipalName(
                        authorizationConsent.getRegisteredClientId(),
                        authorizationConsent.getPrincipalName())
                .orElseGet(OAuth2Consent::new);

        consent.setRegisteredClientId(
                authorizationConsent.getRegisteredClientId());
        consent.setPrincipalName(
                authorizationConsent.getPrincipalName());

        String authorities = authorizationConsent.getAuthorities()
                .stream()
                .map(authority -> authority.getAuthority())
                .collect(java.util.stream.Collectors.joining(","));

        consent.setAuthorities(authorities);
        consentRepository.save(consent);
    }

    @Override
    @Transactional
    public void remove(OAuth2AuthorizationConsent authorizationConsent) {
        consentRepository
                .findByRegisteredClientIdAndPrincipalName(
                        authorizationConsent.getRegisteredClientId(),
                        authorizationConsent.getPrincipalName())
                .ifPresent(consentRepository::delete);
    }

    @Override
    @Transactional(readOnly = true)
    public OAuth2AuthorizationConsent findById(
            String registeredClientId, String principalName) {

        return consentRepository
                .findByRegisteredClientIdAndPrincipalName(
                        registeredClientId, principalName)
                .map(consent -> {
                    OAuth2AuthorizationConsent.Builder builder =
                            OAuth2AuthorizationConsent.withId(
                                    consent.getRegisteredClientId(),
                                    consent.getPrincipalName());

                    if (consent.getAuthorities() != null
                            && !consent.getAuthorities().isBlank()) {
                        for (String authority :
                                consent.getAuthorities().split(",")) {
                            builder.authority(
                                    new SimpleGrantedAuthority(authority.trim()));
                        }
                    }

                    return builder.build();
                })
                .orElse(null);
    }
}