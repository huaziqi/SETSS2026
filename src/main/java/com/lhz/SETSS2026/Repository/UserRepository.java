package com.LHZ.SETSS2026.Repository;

import com.LHZ.SETSS2026.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findById(Integer id);

    List<User> findByName(String name);

    Optional<User> findByPhone(String phone);

    Optional<User> findByEmail(String email);
}

