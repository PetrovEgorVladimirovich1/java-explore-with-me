package ru.practicum.ewm.event.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.ewm.client.stats.StatsClient;
import ru.practicum.ewm.dto.stats.StatsDto;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EventClient extends StatsClient {

    @Autowired
    public EventClient(@Value("${stats-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> create(StatsDto statsDto) {
        return post("/hit", statsDto);
    }

    public ResponseEntity<Object> getStats(String start, String end, List<String> uris, Boolean unique) {
        Map<String, Object> parameters = Map.of(
                "start", start,
                "end", end,
                "unique", unique
        );
        StringBuilder path = new StringBuilder("/stats?start={start}&end={end}");
        for (String uri : uris) {
            path.append("&uris=").append(uri);
        }
        path.append("&unique={unique}");
        return get(path.toString(), parameters);
    }
}
