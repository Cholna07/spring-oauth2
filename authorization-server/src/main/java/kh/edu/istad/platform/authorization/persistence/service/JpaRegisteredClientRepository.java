
package kh.edu.istad.platform.authorization.persistence.service;

import kh.edu.istad.platform.authorization.persistence.entity.RegisteredClientEntity;
import kh.edu.istad.platform.authorization.persistence.mapper.RegisteredClientMapper;
import kh.edu.istad.platform.authorization.persistence.repository.RegisteredClientJpaRepository;

import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Repository
@Transactional
public class JpaRegisteredClientRepository
        implements RegisteredClientRepository {

    private final RegisteredClientJpaRepository jpaRepository;
    private final RegisteredClientMapper mapper;

    public JpaRegisteredClientRepository(
            RegisteredClientJpaRepository jpaRepository,
            RegisteredClientMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public void save(RegisteredClient registeredClient) {
        RegisteredClientEntity entity = mapper.toEntity(registeredClient);

        if (entity.getClientIdIssuedAt() == null) {
            entity.setClientIdIssuedAt(Instant.now());
        }

        jpaRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public RegisteredClient findById(String id) {
        return jpaRepository.findById(id)
                .map(mapper::toRegisteredClient)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public RegisteredClient findByClientId(String clientId) {
        return jpaRepository.findByClientId(clientId)
                .map(mapper::toRegisteredClient)
                .orElse(null);
    }
}
