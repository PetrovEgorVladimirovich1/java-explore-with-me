package ru.practicum.ewm.stats.service;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.stats.StatsDto;
import ru.practicum.ewm.dto.stats.StatsWithHitsDto;

import javax.validation.Valid;
import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
public class StatsController {
    private final StatsService service;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@Valid @RequestBody StatsDto statsDto) {
        service.create(statsDto);
    }

    @GetMapping("/stats")
    public List<StatsWithHitsDto> getStats(@NotNull @FutureOrPresent @RequestParam(name = "start")
                                           @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                           @NotNull @Future @RequestParam(name = "end")
                                           @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                                           @RequestParam(name = "uris", required = false) List<String> uris,
                                           @RequestParam(name = "unique", defaultValue = "false") Boolean unique) {
        return service.getStats(start, end, uris, unique);
    }
}
