package ru.practicum.ewm.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.compilation.mapper.CompilationMapper;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.repository.CompilationRepository;
import ru.practicum.ewm.enums.Status;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.event.repository.LocationRepository;
import ru.practicum.ewm.exceptions.FailBadException;
import ru.practicum.ewm.exceptions.FailConflictException;
import ru.practicum.ewm.exceptions.FailIdException;
import ru.practicum.ewm.service.publicity.PublicService;
import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final CategoryRepository categoryRepository;

    private final UserRepository userRepository;

    private final PublicService publicService;

    private final CompilationRepository compilationRepository;

    private final EventRepository eventRepository;

    private final LocationRepository locationRepository;

    @Override
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        Category category = categoryRepository.save(CategoryMapper.toCategory(newCategoryDto));
        log.info("Категория добавлена. {}", category);
        return CategoryMapper.toCategoryDto(category);
    }

    @Override
    public void deleteCategory(Long catId) {
        categoryRepository.deleteById(catId);
        log.info("Категория удалена.");
    }

    @Override
    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) {
        Category category = CategoryMapper.toCategory(publicService.getCategoryById(catId));
        category.setName(categoryDto.getName());
        log.info("Категория успешно обновлена. {}", category);
        return CategoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Override
    public List<EventFullDto> getEvents(List<Long> users, List<Status> states, List<Long> categories,
                                        LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {
        if (users.isEmpty() && states.isEmpty() && categories.isEmpty() && rangeStart == null && rangeEnd == null) {
            return eventRepository.findAll(PageRequest.of(from / size, size, Sort.by("id"))).stream()
                    .map(EventMapper::toEventFullDto)
                    .collect(Collectors.toList());
        }
        return eventRepository.findByInitiator_IdInAndStateInAndCategory_IdInAndEventDateAfterAndEventDateBefore(users,
                        states, categories, rangeStart, rangeEnd,
                        PageRequest.of(from / size, size, Sort.by("id"))).stream()
                .map(EventMapper::toEventFullDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        Optional<Event> eventOptional = eventRepository.findById(eventId);
        if (eventOptional.isEmpty()) {
            throw new FailIdException("Неверный id!");
        }
        Event event = eventOptional.get();
        if (updateEventAdminRequest.getStateAction() != null &&
                updateEventAdminRequest.getStateAction().equals(Status.PUBLISH_EVENT) &&
                !event.getState().equals(Status.PENDING)) {
            throw new FailConflictException("Неверный статус!");
        }
        if (updateEventAdminRequest.getStateAction() != null &&
                updateEventAdminRequest.getStateAction().equals(Status.REJECT_EVENT) &&
                event.getState().equals(Status.PUBLISHED)) {
            throw new FailConflictException("Нельзя отклонить опубликованное событие!");
        }
        if (updateEventAdminRequest.getEventDate() != null &&
                (updateEventAdminRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(1)))) {
            throw new FailBadException("Дата начала изменяемого события должна быть не ранее чем за час от даты" +
                    " публикации!");
        }
        if (updateEventAdminRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventAdminRequest.getAnnotation());
        }
        if (updateEventAdminRequest.getCategory() != null) {
            event.setCategory(CategoryMapper.toCategory(publicService
                    .getCategoryById(updateEventAdminRequest.getCategory())));
        }
        if (updateEventAdminRequest.getDescription() != null) {
            event.setDescription(updateEventAdminRequest.getDescription());
        }
        if (updateEventAdminRequest.getEventDate() != null) {
            event.setEventDate(updateEventAdminRequest.getEventDate());
        }
        if (updateEventAdminRequest.getLocation() != null) {
            event.setLocation(locationRepository.save(updateEventAdminRequest.getLocation()));
        }
        if (updateEventAdminRequest.getPaid() != null) {
            event.setPaid(updateEventAdminRequest.getPaid());
        }
        if (updateEventAdminRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventAdminRequest.getParticipantLimit());
        }
        if (updateEventAdminRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventAdminRequest.getRequestModeration());
        }
        if (updateEventAdminRequest.getStateAction() != null &&
                updateEventAdminRequest.getStateAction().equals(Status.PUBLISH_EVENT)) {
            event.setState(Status.PUBLISHED);
            event.setPublishedOn(LocalDateTime.now());
        }
        if (updateEventAdminRequest.getStateAction() != null &&
                updateEventAdminRequest.getStateAction().equals(Status.REJECT_EVENT)) {
            event.setState(Status.CANCELED);
        }
        if (updateEventAdminRequest.getTitle() != null) {
            event.setTitle(updateEventAdminRequest.getTitle());
        }
        log.info("Событие успешно обновлено. {}", event);
        return EventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        if (ids == null) {
            return userRepository.findAll(PageRequest.of(from / size, size, Sort.by("id")))
                    .get()
                    .map(UserMapper::toUserDto)
                    .collect(Collectors.toList());
        }
        return userRepository.findAllById(ids).stream().map(UserMapper::toUserDto).collect(Collectors.toList());
    }

    @Override
    public UserDto createUser(NewUserRequest newUserRequest) {
        User user = userRepository.save(UserMapper.toUser(newUserRequest));
        log.info("Пользователь создан. {}", user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
        log.info("Пользователь удалён.");
    }

    @Override
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        Compilation compilation = CompilationMapper.toCompilation(newCompilationDto);
        if (newCompilationDto.getEvents().isEmpty()) {
            return CompilationMapper.toCompilationDto(compilationRepository.save(compilation));
        }
        compilation.getEvents().addAll(eventRepository.findAllById(newCompilationDto.getEvents()));
        compilation = compilationRepository.save(compilation);
        log.info("Подборка добавлена. {}", compilation);
        return CompilationMapper.toCompilationDto(compilation);
    }

    @Override
    public void deleteCompilation(Long compId) {
        compilationRepository.deleteById(compId);
        log.info("Подборка удалена.");
    }

    @Override
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateCompilationRequest) {
        Optional<Compilation> compilationOptional = compilationRepository.findById(compId);
        if (compilationOptional.isEmpty()) {
            throw new FailIdException("Неверный id.");
        }
        Compilation compilation = compilationOptional.get();
        if (!updateCompilationRequest.getEvents().isEmpty()) {
            compilation.getEvents().addAll(eventRepository.findAllById(updateCompilationRequest.getEvents()));
        }
        if (updateCompilationRequest.getPinned() != null) {
            compilation.setPinned(updateCompilationRequest.getPinned());
        }
        if (updateCompilationRequest.getTitle() != null) {
            compilation.setTitle(updateCompilationRequest.getTitle());
        }
        log.info("Категория успешно обновлена. {}", compilation);
        return CompilationMapper.toCompilationDto(compilationRepository.save(compilation));
    }
}
