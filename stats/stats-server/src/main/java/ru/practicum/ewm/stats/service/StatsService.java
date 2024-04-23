package ru.practicum.ewm.stats.service;

import ru.practicum.ewm.dto.stats.StatsDto;
import ru.practicum.ewm.dto.stats.StatsWithHitsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {
    void create(StatsDto statsDto);

    List<StatsWithHitsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique);
}
