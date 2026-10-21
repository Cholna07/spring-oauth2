package kh.edu.istad.platform.authorization.config;

import kh.edu.istad.platform.authorization.persistence.service.JpaRegisteredClientRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.util.UUID;

@Configuration
public class RegisteredClientInitializer {

    @Bean
    public CommandLineRunner initializeRegisteredClient(
            JpaRegisteredClientRepository repository) {

        return args -> {
            RegisteredClient existingClient =
                    repository.findByClientId("postman-client");

            if (existingClient == null) {

                RegisteredClient client = RegisteredClient
                        .withId(UUID.randomUUID().toString())
                        .clientId("postman-client")
                        .clientSecret("{noop}secret")
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
                        .tokenSettings(TokenSettings.builder().build())
                        .build();

                repository.save(client);

                System.out.println(
                        "Registered OAuth2 client saved to PostgreSQL.");

            } else {
                System.out.println(
                        "OAuth2 client already exists in PostgreSQL.");
            }
        };
    }
}
