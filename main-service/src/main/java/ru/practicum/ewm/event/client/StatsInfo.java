package ru.practicum.ewm.event.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.dto.stats.StatsDto;
import ru.practicum.ewm.dto.stats.StatsWithHitsDto;

import java.util.List;

@AllArgsConstructor
@Component
public class StatsInfo {
    private final ObjectMapper objectMapper;
    private final EventClient eventClient;

    public Long getStats(String start, String end, List<String> uris, Boolean unique) {
        ResponseEntity<Object> response = eventClient.getStats(start, end, uris, unique);
        List<StatsWithHitsDto> stats = objectMapper.convertValue(response.getBody(), new TypeReference<>() {
        });
        if (stats.isEmpty()) {
            return 0L;
        }
        return (long) stats.get(0).getHits();
    }

    public void create(StatsDto statsDto) {
        eventClient.create(statsDto);
    }
}
