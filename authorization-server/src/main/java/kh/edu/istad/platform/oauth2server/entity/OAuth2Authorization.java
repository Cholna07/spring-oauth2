package kh.edu.istad.platform.oauth2server.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
        name = "oauth2_authorizations",
        indexes = {
                @Index(name = "idx_authorization_id", columnList = "authorizationId"),
                @Index(name = "idx_access_token", columnList = "accessTokenValue"),
                @Index(name = "idx_refresh_token", columnList = "refreshTokenValue"),
                @Index(name = "idx_auth_code", columnList = "authorizationCodeValue")
        }
)
public class OAuth2Authorization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String authorizationId;

    @Column(nullable = false)
    private String registeredClientId;

    @Column(nullable = false)
    private String principalName;

    @Column(nullable = false)
    private String authorizationGrantType;

    @Column(length = 2000)
    private String authorizedScopes;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String attributes;

    private String state;

    // Authorization code
    @Lob
    @Column(columnDefinition = "TEXT")
    private String authorizationCodeValue;

    private Instant authorizationCodeIssuedAt;
    private Instant authorizationCodeExpiresAt;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String authorizationCodeMetadata;

    // Access token
    @Lob
    @Column(columnDefinition = "TEXT")
    private String accessTokenValue;

    private Instant accessTokenIssuedAt;
    private Instant accessTokenExpiresAt;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String accessTokenMetadata;

    @Column(length = 1000)
    private String accessTokenType;

    @Column(length = 2000)
    private String accessTokenScopes;

    // Refresh token
    @Lob
    @Column(columnDefinition = "TEXT")
    private String refreshTokenValue;

    private Instant refreshTokenIssuedAt;
    private Instant refreshTokenExpiresAt;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String refreshTokenMetadata;

    public OAuth2Authorization() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAuthorizationId() { return authorizationId; }
    public void setAuthorizationId(String authorizationId) {
        this.authorizationId = authorizationId;
    }

    public String getRegisteredClientId() { return registeredClientId; }
    public void setRegisteredClientId(String registeredClientId) {
        this.registeredClientId = registeredClientId;
    }

    public String getPrincipalName() { return principalName; }
    public void setPrincipalName(String principalName) {
        this.principalName = principalName;
    }

    public String getAuthorizationGrantType() {
        return authorizationGrantType;
    }
    public void setAuthorizationGrantType(String authorizationGrantType) {
        this.authorizationGrantType = authorizationGrantType;
    }

    public String getAuthorizedScopes() { return authorizedScopes; }
    public void setAuthorizedScopes(String authorizedScopes) {
        this.authorizedScopes = authorizedScopes;
    }

    public String getAttributes() { return attributes; }
    public void setAttributes(String attributes) {
        this.attributes = attributes;
    }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getAuthorizationCodeValue() {
        return authorizationCodeValue;
    }
    public void setAuthorizationCodeValue(String value) {
        this.authorizationCodeValue = value;
    }

    public Instant getAuthorizationCodeIssuedAt() {
        return authorizationCodeIssuedAt;
    }
    public void setAuthorizationCodeIssuedAt(Instant value) {
        this.authorizationCodeIssuedAt = value;
    }

    public Instant getAuthorizationCodeExpiresAt() {
        return authorizationCodeExpiresAt;
    }
    public void setAuthorizationCodeExpiresAt(Instant value) {
        this.authorizationCodeExpiresAt = value;
    }

    public String getAuthorizationCodeMetadata() {
        return authorizationCodeMetadata;
    }
    public void setAuthorizationCodeMetadata(String value) {
        this.authorizationCodeMetadata = value;
    }

    public String getAccessTokenValue() { return accessTokenValue; }
    public void setAccessTokenValue(String value) {
        this.accessTokenValue = value;
    }

    public Instant getAccessTokenIssuedAt() { return accessTokenIssuedAt; }
    public void setAccessTokenIssuedAt(Instant value) {
        this.accessTokenIssuedAt = value;
    }

    public Instant getAccessTokenExpiresAt() {
        return accessTokenExpiresAt;
    }
    public void setAccessTokenExpiresAt(Instant value) {
        this.accessTokenExpiresAt = value;
    }

    public String getAccessTokenMetadata() { return accessTokenMetadata; }
    public void setAccessTokenMetadata(String value) {
        this.accessTokenMetadata = value;
    }

    public String getAccessTokenType() { return accessTokenType; }
    public void setAccessTokenType(String value) {
        this.accessTokenType = value;
    }

    public String getAccessTokenScopes() { return accessTokenScopes; }
    public void setAccessTokenScopes(String value) {
        this.accessTokenScopes = value;
    }

    public String getRefreshTokenValue() { return refreshTokenValue; }
    public void setRefreshTokenValue(String value) {
        this.refreshTokenValue = value;
    }

    public Instant getRefreshTokenIssuedAt() {
        return refreshTokenIssuedAt;
    }
    public void setRefreshTokenIssuedAt(Instant value) {
        this.refreshTokenIssuedAt = value;
    }

    public Instant getRefreshTokenExpiresAt() {
        return refreshTokenExpiresAt;
    }
    public void setRefreshTokenExpiresAt(Instant value) {
        this.refreshTokenExpiresAt = value;
    }

    public String getRefreshTokenMetadata() {
        return refreshTokenMetadata;
    }
    public void setRefreshTokenMetadata(String value) {
        this.refreshTokenMetadata = value;
    }
}