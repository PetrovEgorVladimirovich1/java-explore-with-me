package ru.practicum.ewm.stats.service;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.stats.StatsDto;
import ru.practicum.ewm.dto.stats.StatsWithHitsDto;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StatsController {
    private final StatsService service;

    @PostMapping("/hit")
    public void create(@RequestBody StatsDto statsDto) {
        service.create(statsDto);
    }

    @GetMapping("/stats")
    public List<StatsWithHitsDto> getStats(@RequestParam(name = "start") String start,
                                           @RequestParam(name = "end") String end,
                                           @RequestParam(name = "uris", required = false) List<String> uris,
                                           @RequestParam(name = "unique", defaultValue = "false") Boolean unique) {
        return service.getStats(start, end, uris, unique);
    }
}
