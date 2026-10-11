package kz.zankz.server.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import kz.zankz.server.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByToken(String token);

    boolean existsByEmail(String email);
}