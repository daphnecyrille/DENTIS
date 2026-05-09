package com.dentis.DENTIS.repository;

import com.dentis.DENTIS.model.AccountStatus;
import com.dentis.DENTIS.model.Role;
import com.dentis.DENTIS.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByRoleAndStatusOrderByLastNameAsc(Role role, AccountStatus status);
}
