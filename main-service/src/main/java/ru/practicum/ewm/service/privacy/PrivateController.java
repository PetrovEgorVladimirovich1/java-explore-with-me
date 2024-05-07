package ru.practicum.ewm.service.privacy;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users/{userId}")
@Validated
public class PrivateController {
    private final PrivateService service;

    @GetMapping("/events")
    public List<EventShortDto> getEventsUser(@Positive @NotNull @PathVariable Long userId,
                                             @RequestParam(name = "from", defaultValue = "0") int from,
                                             @RequestParam(name = "size", defaultValue = "10") int size) {
        return service.getEventsUser(userId, from, size);
    }

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(@Positive @NotNull @PathVariable Long userId,
                                    @Valid @RequestBody NewEventDto newEventDto) {
        return service.createEvent(userId, newEventDto);
    }

    @GetMapping("/events/{eventId}")
    public EventFullDto getEventFullUser(@Positive @NotNull @PathVariable Long userId,
                                         @Positive @NotNull @PathVariable Long eventId) {
        return service.getEventFullUser(userId, eventId);
    }

    @PatchMapping("/events/{eventId}")
    public EventFullDto updateEventUser(@Positive @NotNull @PathVariable Long userId,
                                        @Positive @NotNull @PathVariable Long eventId,
                                        @Valid @RequestBody UpdateEventUserRequest updateEventUserRequest) {
        return service.updateEventUser(userId, eventId, updateEventUserRequest);
    }

    @GetMapping("/events/{eventId}/requests")
    public List<ParticipationRequestDto> getRequests(@Positive @NotNull @PathVariable Long userId,
                                                     @Positive @NotNull @PathVariable Long eventId) {
        return service.getRequests(userId, eventId);
    }

    @PatchMapping("/events/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequest(@Positive @NotNull @PathVariable Long userId,
                                                        @Positive @NotNull @PathVariable Long eventId,
                                                        @Valid @RequestBody EventRequestStatusUpdateRequest
                                                                eventRequestStatusUpdateRequest) {
        return service.updateRequest(userId, eventId, eventRequestStatusUpdateRequest);
    }

    @GetMapping("/requests")
    public List<ParticipationRequestDto> getRequestEvents(@Positive @NotNull @PathVariable Long userId) {
        return service.getRequestEvents(userId);
    }

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(@Positive @NotNull @PathVariable Long userId,
                                                 @Positive @RequestParam(name = "eventId") Long eventId) {
        return service.createRequest(userId, eventId);
    }

    @PatchMapping("/requests/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@Positive @NotNull @PathVariable Long userId,
                                                 @Positive @NotNull @PathVariable Long requestId) {
        return service.cancelRequest(userId, requestId);
    }
}
