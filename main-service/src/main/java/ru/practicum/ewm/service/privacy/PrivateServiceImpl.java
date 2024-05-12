package ru.practicum.ewm.service.privacy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.enums.Status;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Comment;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.CommentRepository;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.event.repository.LocationRepository;
import ru.practicum.ewm.exceptions.FailConflictException;
import ru.practicum.ewm.exceptions.FailIdException;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.mapper.RequestMapper;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.service.publicity.PublicService;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PrivateServiceImpl implements PrivateService {
    private final EventRepository eventRepository;

    private final PublicService publicService;

    private final UserRepository userRepository;

    private final LocationRepository locationRepository;

    private final RequestRepository requestRepository;

    private final CommentRepository commentRepository;

    private User getUserById(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new FailIdException("Неверный id!");
        }
        return user.get();
    }

    @Override
    public List<EventShortDto> getEventsUser(Long userId, int from, int size) {
        return eventRepository.findByInitiator_Id(userId,
                        PageRequest.of(from / size, size, Sort.by("id"))).stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2)) ||
                newEventDto.getEventDate().equals(LocalDateTime.now().plusHours(2))) {
            throw new FailConflictException("Неверная дата!");
        }
        newEventDto.setLocation(locationRepository.save(newEventDto.getLocation()));
        Event event = EventMapper.toEvent(newEventDto);
        event.setCategory(CategoryMapper.toCategory(publicService.getCategoryById(newEventDto.getCategory())));
        event.setInitiator(getUserById(userId));
        event.setState(Status.PENDING);
        event = eventRepository.save(event);
        log.info("Событие добавлено. {}", event);
        return EventMapper.toEventFullDto(event);
    }

    @Override
    public EventFullDto getEventFullUser(Long userId, Long eventId) {
        Optional<Event> event = eventRepository.findByInitiator_IdAndId(userId, eventId);
        if (event.isEmpty()) {
            throw new FailIdException("Неверный id!");
        }
        return EventMapper.toEventFullDto(event.get());
    }

    @Override
    public EventFullDto updateEventUser(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        if (updateEventUserRequest.getEventDate() != null &&
                updateEventUserRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(2)) ||
                Objects.equals(updateEventUserRequest.getEventDate(), LocalDateTime.now().plusHours(2))) {
            throw new FailConflictException("Неверная дата!");
        }
        Optional<Event> eventOptional = eventRepository.getEvent(userId, eventId);
        if (eventOptional.isEmpty()) {
            throw new FailConflictException("Неверный id или статус!");
        }
        Event event = eventOptional.get();
        if (updateEventUserRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventUserRequest.getAnnotation());
        }
        if (updateEventUserRequest.getCategory() != null) {
            event.setCategory(CategoryMapper.toCategory(publicService
                    .getCategoryById(updateEventUserRequest.getCategory())));
        }
        if (updateEventUserRequest.getDescription() != null) {
            event.setDescription(updateEventUserRequest.getDescription());
        }
        if (updateEventUserRequest.getEventDate() != null) {
            event.setEventDate(updateEventUserRequest.getEventDate());
        }
        if (updateEventUserRequest.getLocation() != null) {
            event.setLocation(locationRepository.save(updateEventUserRequest.getLocation()));
        }
        if (updateEventUserRequest.getPaid() != null) {
            event.setPaid(updateEventUserRequest.getPaid());
        }
        if (updateEventUserRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventUserRequest.getParticipantLimit());
        }
        if (updateEventUserRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventUserRequest.getRequestModeration());
        }
        if (updateEventUserRequest.getStateAction() != null &&
                updateEventUserRequest.getStateAction().equals(Status.SEND_TO_REVIEW)) {
            event.setState(Status.PENDING);
        }
        if (updateEventUserRequest.getStateAction() != null &&
                updateEventUserRequest.getStateAction().equals(Status.CANCEL_REVIEW)) {
            event.setState(Status.CANCELED);
        }
        if (updateEventUserRequest.getTitle() != null) {
            event.setTitle(updateEventUserRequest.getTitle());
        }
        log.info("Событие успешно обновлено. {}", event);
        return EventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public List<ParticipationRequestDto> getRequests(Long userId, Long eventId) {
        getEventFullUser(userId, eventId);
        return requestRepository.findByEvent(eventId).stream()
                .map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventRequestStatusUpdateResult updateRequest(Long userId, Long eventId,
                                                        EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        Optional<Event> eventOptional = eventRepository.findByInitiator_IdAndId(userId, eventId);
        if (eventOptional.isEmpty()) {
            throw new FailIdException("Неверный id!");
        }
        Event event = eventOptional.get();
        if (event.getParticipantLimit() != 0 &&
                event.getParticipantLimit().equals(event.getConfirmedRequests())) {
            throw new FailConflictException("Лимит исчерпан!");
        }
        List<Request> requests = requestRepository.findAllById(eventRequestStatusUpdateRequest.getRequestIds());
        for (Request request : requests) {
            if (request.getStatus() == Status.CONFIRMED) {
                throw new FailConflictException("Заявка уже принята!");
            }
            request.setStatus(eventRequestStatusUpdateRequest.getStatus());
            requestRepository.save(request);
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        }
        EventRequestStatusUpdateResult eventRequestStatusUpdateResult = new EventRequestStatusUpdateResult();
        eventRequestStatusUpdateResult.getConfirmedRequests().addAll(requests.stream()
                .filter(request -> request.getStatus() == Status.CONFIRMED)
                .map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList()));
        eventRequestStatusUpdateResult.getRejectedRequests().addAll(requests.stream()
                .filter(request -> request.getStatus() == Status.REJECTED)
                .map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList()));
        return eventRequestStatusUpdateResult;
    }

    @Override
    public List<ParticipationRequestDto> getRequestEvents(Long userId) {
        return requestRepository.findByRequester(userId).stream()
                .map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        getUserById(userId);
        if (requestRepository.findByRequesterAndEvent(userId, eventId).isPresent()) {
            throw new FailConflictException("Нельзя добавить повторный запрос!");
        }
        Optional<Event> eventOptional = eventRepository.findById(eventId);
        if (eventOptional.isEmpty()) {
            throw new FailIdException("Неверный id события!");
        }
        Event event = eventOptional.get();
        if (event.getInitiator().getId().equals(userId)) {
            throw new FailConflictException("Нельзя добавить запрос к своему событию!");
        }
        if (!event.getState().equals(Status.PUBLISHED)) {
            throw new FailConflictException("Событие неопубликовано!");
        }
        if (event.getParticipantLimit() != 0 && event.getParticipantLimit().equals(event.getConfirmedRequests())) {
            throw new FailConflictException("Лимит исчерпан!");
        }
        Request request = new Request(null, LocalDateTime.now(), eventId, userId, Status.PENDING);
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(Status.CONFIRMED);
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        }
        request = requestRepository.save(request);
        log.info("Запрос добавлен. {}", request);
        return RequestMapper.toParticipationRequestDto(request);
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        Optional<Request> requestOptional = requestRepository.findByRequesterAndId(userId, requestId);
        if (requestOptional.isEmpty()) {
            throw new FailIdException("Неверный id!");
        }
        Request request = requestOptional.get();
        request.setStatus(Status.CANCELED);
        return RequestMapper.toParticipationRequestDto(requestRepository.save(request));
    }

    @Override
    public CommentDto createComment(Long userId, Long eventId, NewCommentDto newCommentDto) {
        User user = getUserById(userId);
        Optional<Event> eventOptional = eventRepository.findById(eventId);
        if (eventOptional.isEmpty()) {
            throw new FailIdException("Неверный id события!");
        }
        Event event = eventOptional.get();
        Comment comment = EventMapper.toComment(newCommentDto);
        comment.setEvent(event);
        comment.setAuthor(user);
        comment = commentRepository.save(comment);
        log.info("Комментарий добавлен. {}", comment);
        return EventMapper.toCommentDto(comment);
    }

    @Override
    public CommentDto updateComment(Long userId, Long eventId, Long commentId, NewCommentDto newCommentDto) {
        Optional<Comment> commentOptional = commentRepository.findByAuthor_IdAndEvent_IdAndId(userId, eventId, commentId);
        if (commentOptional.isEmpty()) {
            throw new FailIdException("Неверный id!");
        }
        Comment comment = commentOptional.get();
        comment.setText(newCommentDto.getText());
        log.info("Комментарий обновлён. {}", comment);
        return EventMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    public void deleteComment(Long userId, Long eventId, Long commentId) {
        if (commentRepository.findByAuthor_IdAndEvent_IdAndId(userId, eventId, commentId).isEmpty()) {
            throw new FailIdException("Неверный id!");
        }
        commentRepository.deleteById(commentId);
        log.info("Комментарий удалён.");
    }

    @Override
    public List<CommentDto> getComments(Long userId, Long eventId, int from, int size) {
        return commentRepository.findByAuthor_IdAndEvent_Id(userId, eventId,
                        PageRequest.of(from / size, size, Sort.by("id"))).stream()
                .map(EventMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentDto> getCommentsUser(Long userId, int from, int size) {
        return commentRepository.findByAuthor_Id(userId,
                        PageRequest.of(from / size, size, Sort.by("id"))).stream()
                .map(EventMapper::toCommentDto)
                .collect(Collectors.toList());
    }
}
