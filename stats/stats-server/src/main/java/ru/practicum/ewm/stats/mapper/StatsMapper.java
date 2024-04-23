package ru.practicum.ewm.stats.mapper;

import ru.practicum.ewm.dto.stats.StatsDto;
import ru.practicum.ewm.stats.model.Stats;

public class StatsMapper {
    public static Stats toStats(StatsDto statsDto) {
        return new Stats(statsDto.getId(),
                statsDto.getApp(),
                statsDto.getUri(),
                statsDto.getIp(),
                statsDto.getTimes());
    }

    public static StatsDto toStatsDto(Stats stats) {
        return new StatsDto(stats.getId(),
                stats.getApp(),
                stats.getUri(),
                stats.getIp(),
                stats.getTimes());
    }
}
