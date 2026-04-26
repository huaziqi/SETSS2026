package com.LHZ.SETSS2026.repository;

import com.LHZ.SETSS2026.entity.ConferencePage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConferencePageRepository extends JpaRepository<ConferencePage, Long> {

    Optional<ConferencePage> findByPageKey(String pageKey);
}