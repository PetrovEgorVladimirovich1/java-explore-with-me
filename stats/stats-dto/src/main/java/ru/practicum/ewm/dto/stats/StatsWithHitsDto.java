package ru.practicum.ewm.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatsWithHitsDto {
    private String app;

    private String uri;

    private int hits;
}
