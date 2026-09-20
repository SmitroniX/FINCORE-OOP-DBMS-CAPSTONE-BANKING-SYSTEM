package com.fincore.repository;

import com.fincore.model.AuthUser;

import java.util.Optional;

/**
 * Data access contract for User authentication and credential management.
 */
public interface UserRepository extends CrudRepository<AuthUser, Long> {

    Optional<AuthUser> findByUsername(String username);

    Optional<AuthUser> authenticate(String username, String password);
}
