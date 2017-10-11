package com.evolveback.health.repository;

import com.evolveback.health.domain.Points;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;


import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA repository for the Points entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PointsRepository extends JpaRepository<Points, Long> {

    @Query("select points from Points points where points.user.login = ?#{principal.username} order by points.date desc")
    Page<Points> findByUserIsCurrentUser(Pageable pageable);
    Page<Points> findAllByOrderByDateDesc(Pageable pageable);
    List<Points> findAllByDateBetween(LocalDate firstDate, LocalDate secondDate);
    List<Points> findAllByDateBetweenAndUserLogin(LocalDate startOfWeek, LocalDate endOfWeek, String currentUserLogin);

}
