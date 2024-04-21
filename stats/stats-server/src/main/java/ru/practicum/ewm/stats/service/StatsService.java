package ru.practicum.ewm.stats.service;

import ru.practicum.ewm.dto.stats.StatsDto;
import ru.practicum.ewm.dto.stats.StatsWithHitsDto;

import java.util.List;

public interface StatsService {
    void create(StatsDto statsDto);

    List<StatsWithHitsDto> getStats(String start, String end, List<String> uris, Boolean unique);
}
