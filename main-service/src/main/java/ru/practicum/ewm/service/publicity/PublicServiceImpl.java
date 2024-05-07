package ru.practicum.ewm.service.publicity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.mapper.CompilationMapper;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.repository.CompilationRepository;
import ru.practicum.ewm.dto.stats.StatsDto;
import ru.practicum.ewm.enums.Status;
import ru.practicum.ewm.event.client.StatsInfo;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exceptions.FailBadException;
import ru.practicum.ewm.exceptions.FailIdException;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PublicServiceImpl implements PublicService {
    private final CategoryRepository categoryRepository;

    private final CompilationRepository compilationRepository;

    private final EventRepository eventRepository;

    private final StatsInfo statsInfo;

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        if (pinned != null) {
            return compilationRepository.findByPinned(pinned,
                            PageRequest.of(from / size, size, Sort.by("id"))).stream()
                    .map(CompilationMapper::toCompilationDto)
                    .collect(Collectors.toList());
        }
        return compilationRepository.findAll(PageRequest.of(from / size, size, Sort.by("id"))).stream()
                .map(CompilationMapper::toCompilationDto)
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        Optional<Compilation> compilation = compilationRepository.findById(compId);
        if (compilation.isEmpty()) {
            throw new FailIdException("Неверный id.");
        }
        return CompilationMapper.toCompilationDto(compilation.get());
    }

    @Override
    public List<CategoryDto> getCategories(int from, int size) {
        return categoryRepository.findAll(PageRequest.of(from / size, size, Sort.by("id")))
                .get()
                .map(CategoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategoryById(Long catId) {
        Optional<Category> category = categoryRepository.findById(catId);
        if (category.isEmpty()) {
            throw new FailIdException("Неверный id.");
        }
        return CategoryMapper.toCategoryDto(category.get());
    }

    @Override
    public List<EventShortDto> getEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                         LocalDateTime rangeEnd, Boolean onlyAvailable,
                                         String sort, int from, int size, HttpServletRequest request) {
        if (rangeStart == null && rangeEnd == null) {
            rangeStart = LocalDateTime.now();
        }
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new FailBadException("Неверное время!");
        }
        if (onlyAvailable) {
            if (sort != null) {
                if (sort.equals("EVENT_DATE")) {
                    statsInfo.create(new StatsDto(null, "main-service", request.getRequestURI(), request.getRemoteAddr(),
                            LocalDateTime.now()));
                    return eventRepository.getEventsTrue(text, categories, paid, rangeStart, rangeEnd,
                                    PageRequest.of(from / size, size, Sort.by("id", "eventDate"))).stream()
                            .map(EventMapper::toEventShortDto)
                            .collect(Collectors.toList());
                }
                if (sort.equals("VIEWS")) {
                    statsInfo.create(new StatsDto(null, "main-service", request.getRequestURI(), request.getRemoteAddr(),
                            LocalDateTime.now()));
                    return eventRepository.getEventsTrue(text, categories, paid, rangeStart, rangeEnd,
                                    PageRequest.of(from / size, size, Sort.by("id", "views"))).stream()
                            .map(EventMapper::toEventShortDto)
                            .collect(Collectors.toList());
                }
            }
            statsInfo.create(new StatsDto(null, "main-service", request.getRequestURI(), request.getRemoteAddr(),
                    LocalDateTime.now()));
            return eventRepository.getEventsTrue(text, categories, paid, rangeStart, rangeEnd,
                            PageRequest.of(from / size, size, Sort.by("id"))).stream()
                    .map(EventMapper::toEventShortDto)
                    .collect(Collectors.toList());
        }
        if (sort != null) {
            if (sort.equals("EVENT_DATE")) {
                statsInfo.create(new StatsDto(null, "main-service", request.getRequestURI(), request.getRemoteAddr(),
                        LocalDateTime.now()));
                return eventRepository.getEventsFalse(text, categories, paid, rangeStart, rangeEnd,
                                PageRequest.of(from / size, size, Sort.by("id", "eventDate"))).stream()
                        .map(EventMapper::toEventShortDto)
                        .collect(Collectors.toList());
            }
            if (sort.equals("VIEWS")) {
                statsInfo.create(new StatsDto(null, "main-service", request.getRequestURI(), request.getRemoteAddr(),
                        LocalDateTime.now()));
                return eventRepository.getEventsFalse(text, categories, paid, rangeStart, rangeEnd,
                                PageRequest.of(from / size, size, Sort.by("id", "views"))).stream()
                        .map(EventMapper::toEventShortDto)
                        .collect(Collectors.toList());
            }
        }
        statsInfo.create(new StatsDto(null, "main-service", request.getRequestURI(), request.getRemoteAddr(),
                LocalDateTime.now()));
        return eventRepository.getEventsFalse(text, categories, paid, rangeStart, rangeEnd,
                        PageRequest.of(from / size, size, Sort.by("id"))).stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getEventById(Long id, HttpServletRequest request) {
        Optional<Event> eventOptional = eventRepository.findByStateAndId(Status.PUBLISHED, id);
        if (eventOptional.isEmpty()) {
            throw new FailIdException("Неверный id.");
        }
        Event event = eventOptional.get();
        event.setViews(statsInfo.getStats(event.getPublishedOn().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                event.getEventDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                List.of(request.getRequestURI()), true));
        statsInfo.create(new StatsDto(null, "main-service", request.getRequestURI(), request.getRemoteAddr(),
                LocalDateTime.now()));
        eventRepository.save(event);
        return EventMapper.toEventFullDto(eventOptional.get());
    }
}
