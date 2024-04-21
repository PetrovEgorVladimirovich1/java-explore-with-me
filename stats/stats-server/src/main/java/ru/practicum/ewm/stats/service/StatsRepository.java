package ru.practicum.ewm.stats.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.stats.model.Stats;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatsRepository extends JpaRepository<Stats, Long> {
    @Query(value = "SELECT * " +
            "FROM stats " +
            "WHERE times >= ?1 " +
            "AND times <= ?2 " +
            "AND uri = ?3 ", nativeQuery = true)
    List<Stats> getStats(LocalDateTime start, LocalDateTime end, String uri);

    @Query(value = "SELECT DISTINCT ON (ip) * " +
            "FROM stats " +
            "WHERE times >= ?1 " +
            "AND times <= ?2 " +
            "AND uri = ?3 ", nativeQuery = true)
    List<Stats> getStatsUnique(LocalDateTime start, LocalDateTime end, String uri);

    @Query(value = "SELECT DISTINCT ON (app,uri) * " +
            "FROM stats " +
            "WHERE times >= ?1 " +
            "AND times <= ?2 ", nativeQuery = true)
    List<Stats> getStatsWithoutUri(LocalDateTime start, LocalDateTime end);

    @Query(value = "SELECT DISTINCT ON (app,uri) " +
            "COUNT(uri) " +
            "FROM stats " +
            "WHERE times >= ?1 " +
            "AND times <= ?2 " +
            "AND app = ?3 " +
            "AND uri = ?4 " +
            "GROUP BY app, uri;", nativeQuery = true)
    Integer getCountStats(LocalDateTime start, LocalDateTime end, String app, String uri);
}
