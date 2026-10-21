
package kh.edu.istad.platform.oauth2server.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;


import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

@Configuration
public class AuthorizationServerConfig {

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer("http://localhost:9090")
                .build();
    }
    @Bean
    CommandLineRunner generateClientSecret(PasswordEncoder passwordEncoder) {
        return args -> System.out.println(
                "Client secret: " +
                        passwordEncoder.encode("postman-secret")
        );
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        RSAKey rsaKey = generateRsaKey();
        JWKSet jwkSet = new JWKSet(rsaKey);

        return (jwkSelector, securityContext) ->
                jwkSelector.select(jwkSet);
    }

    private RSAKey generateRsaKey() {
        try {
            KeyPairGenerator generator =
                    KeyPairGenerator.getInstance("RSA");

            generator.initialize(2048);

            KeyPair keyPair = generator.generateKeyPair();

            RSAPublicKey publicKey =
                    (RSAPublicKey) keyPair.getPublic();

            RSAPrivateKey privateKey =
                    (RSAPrivateKey) keyPair.getPrivate();

            return new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyID(UUID.randomUUID().toString())
                    .build();

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Failed to generate RSA keys",
                    exception
            );
        }
    }
}
