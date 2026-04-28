package com.LHZ.SETSS2026.repository;

import com.LHZ.SETSS2026.dto.Assign.UserSimpleDTO;
import com.LHZ.SETSS2026.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findById(Integer id);

    List<User> findByName(String name);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.role WHERE u.name = :name")
    Optional<User> findByNameWithRole(String name);

    Optional<User> findByPhone(String phone);

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.role WHERE u.role.name = :roleName")
    List<User> findByRoleName(String roleName);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.role")
    List<User> findAllWithRole();
}


