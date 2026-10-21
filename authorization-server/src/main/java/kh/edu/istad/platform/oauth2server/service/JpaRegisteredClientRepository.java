
package kh.edu.istad.platform.oauth2server.service;

import kh.edu.istad.platform.oauth2server.entity.OAuth2Client;
import kh.edu.istad.platform.oauth2server.repository.OAuth2ClientRepository;

import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.stereotype.Service;

@Service
public class JpaRegisteredClientRepository
        implements RegisteredClientRepository {

    private final OAuth2ClientRepository clientRepository;

    public JpaRegisteredClientRepository(
            OAuth2ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public void save(RegisteredClient registeredClient) {
        OAuth2Client client = clientRepository
                .findByClientId(registeredClient.getClientId())
                .orElseGet(OAuth2Client::new);

        if (client.getRegisteredClientId() == null) {
            client.setRegisteredClientId(registeredClient.getId());
        }

        client.setClientId(registeredClient.getClientId());
        client.setClientSecret(registeredClient.getClientSecret());
        client.setName(registeredClient.getClientName());

        clientRepository.save(client);
    }

    @Override
    public RegisteredClient findById(String id) {
        return clientRepository.findByRegisteredClientId(id)
                .map(this::toRegisteredClient)
                .orElse(null);
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        return clientRepository.findByClientId(clientId)
                .map(this::toRegisteredClient)
                .orElse(null);
    }

    private RegisteredClient toRegisteredClient(OAuth2Client client) {
        return RegisteredClient.withId(client.getRegisteredClientId())
                .clientId(client.getClientId())
                .clientSecret(client.getClientSecret())
                .clientName(client.getName())

                .clientAuthenticationMethod(
                        ClientAuthenticationMethod.CLIENT_SECRET_BASIC)

                .authorizationGrantType(
                        AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(
                        AuthorizationGrantType.REFRESH_TOKEN)

                .redirectUri(
                        "https://oauth.pstmn.io/v1/callback")

                .scope("openid")
                .scope("profile")
                .scope("read")

                .clientSettings(ClientSettings.builder()
                        .requireProofKey(true)
                        .requireAuthorizationConsent(false)
                        .build())

                .build();
    }
}
