package kh.edu.istad.platform.authorization.persistence.repository;

import kh.edu.istad.platform.authorization.persistence.entity.RegisteredClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface RegisteredClientJpaRepository extends JpaRepository<RegisteredClientEntity, String> {

    Optional<RegisteredClientEntity> findByClientId(String clientId);
}