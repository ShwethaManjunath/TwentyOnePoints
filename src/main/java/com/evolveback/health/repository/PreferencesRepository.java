package com.evolveback.health.repository;

import com.evolveback.health.domain.Preferences;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.util.Optional;


/**
 * Spring Data JPA repository for the Preferences entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PreferencesRepository extends JpaRepository<Preferences, Long> {
    Optional<Preferences> findOneByUserLogin(String login);

    @Query("select preferences from Preferences preferences where preferences.user.login = ?#{principal.username} ")
    Page<Preferences> findByUserIsCurrentUser(Pageable pageable);

    Page<Preferences> findAll(Pageable pageable);
}
