package com.dentis.DENTIS.repository;

import com.dentis.DENTIS.model.AccountStatus;
import com.dentis.DENTIS.model.Role;
import com.dentis.DENTIS.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByRoleAndStatusOrderByLastNameAsc(Role role, AccountStatus status);
    List<User> findByRoleAndSectionAndStatusOrderByLastNameAsc(Role role, String section, AccountStatus status);

    @Query("SELECT u FROM User u WHERE u.role = :role AND UPPER(u.section) = UPPER(:section) AND u.status = :status ORDER BY u.lastName")
    List<User> findByRoleAndSectionIgnoreCaseAndStatus(@Param("role") Role role, @Param("section") String section, @Param("status") AccountStatus status);
}
