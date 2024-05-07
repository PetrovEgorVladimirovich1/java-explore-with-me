package ru.practicum.ewm.stats.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dto.stats.StatsDto;
import ru.practicum.ewm.dto.stats.StatsWithHitsDto;
import ru.practicum.ewm.stats.exceptions.FailBadException;
import ru.practicum.ewm.stats.mapper.StatsMapper;
import ru.practicum.ewm.stats.model.Stats;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {
    private final StatsRepository repository;

    @Override
    public void create(StatsDto statsDto) {
        statsDto.setTimes(LocalDateTime.now());
        Stats stats = StatsMapper.toStats(statsDto);
        repository.save(stats);
        log.info("Информация сохранена. {}", stats);
    }

    @Override
    public List<StatsWithHitsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (start.isAfter(end)) {
            throw new FailBadException("Неверное время!");
        }
        List<StatsWithHitsDto> statsWithHitsDtos = new ArrayList<>();
        if (uris == null) {
            for (Stats stats : repository.getStatsWithoutUri(start, end)) {
                statsWithHitsDtos.add(new StatsWithHitsDto(stats.getApp(), stats.getUri(),
                        repository.getCountStats(start, end, stats.getApp(), stats.getUri())));
            }
            return statsWithHitsDtos.stream()
                    .sorted(Comparator.comparingInt(StatsWithHitsDto::getHits).reversed())
                    .collect(Collectors.toList());
        }
        if (unique) {
            for (String uri : uris) {
                List<Stats> statsList = repository.getStatsUnique(start, end, uri);
                if (!statsList.isEmpty()) {
                    Stats stats = statsList.get(0);
                    statsWithHitsDtos.add(new StatsWithHitsDto(stats.getApp(), stats.getUri(), statsList.size()));
                }
            }
        } else {
            for (String uri : uris) {
                List<Stats> statsList = repository.getStats(start, end, uri);
                if (!statsList.isEmpty()) {
                    Stats stats = statsList.get(0);
                    statsWithHitsDtos.add(new StatsWithHitsDto(stats.getApp(), stats.getUri(), statsList.size()));
                }
            }
        }
        return statsWithHitsDtos.stream()
                .sorted(Comparator.comparingInt(StatsWithHitsDto::getHits).reversed())
                .collect(Collectors.toList());
    }
}
