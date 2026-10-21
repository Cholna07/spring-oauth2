
package kh.edu.istad.platform.oauth2server.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "oauth2_consents",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"registered_client_id", "principal_name"}
        )
)
public class OAuth2Consent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "registered_client_id", nullable = false)
    private String registeredClientId;

    @Column(name = "principal_name", nullable = false)
    private String principalName;

    @Column(nullable = false)
    private String authorities;

    public OAuth2Consent() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRegisteredClientId() {
        return registeredClientId;
    }

    public void setRegisteredClientId(String registeredClientId) {
        this.registeredClientId = registeredClientId;
    }

    public String getPrincipalName() {
        return principalName;
    }

    public void setPrincipalName(String principalName) {
        this.principalName = principalName;
    }

    public String getAuthorities() {
        return authorities;
    }

    public void setAuthorities(String authorities) {
        this.authorities = authorities;
    }
}
