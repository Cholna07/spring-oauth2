package kh.edu.istad.platform.oauth2server.repository;

import kh.edu.istad.platform.oauth2server.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    boolean existsByName(String name);
}