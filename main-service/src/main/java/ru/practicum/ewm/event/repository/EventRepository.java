package ru.practicum.ewm.event.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.enums.Status;
import ru.practicum.ewm.event.model.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByInitiator_IdInAndStateInAndCategory_IdInAndEventDateAfterAndEventDateBefore(List<Long> users,
                                                                                                  List<Status> states,
                                                                                                  List<Long> categories,
                                                                                                  LocalDateTime rangeStart,
                                                                                                  LocalDateTime rangeEnd,
                                                                                                  PageRequest page);

    @Query("select e " +
            "from Event as e " +
            "where lower(e.annotation) like lower(concat('%', ?1,'%')) " +
            "or lower(e.description) like lower(concat('%', ?1,'%')) " +
            "and e.category.id in ?2 " +
            "and e.paid = ?3 " +
            "and e.eventDate >= ?4 " +
            "and e.eventDate <= ?5 " +
            "and e.participantLimit <> e.confirmedRequests " +
            "or e.participantLimit = 0 " +
            "and e.state = 'PUBLISHED'")
    List<Event> getEventsTrue(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                              LocalDateTime rangeEnd, PageRequest page);

    @Query("select e " +
            "from Event as e " +
            "where lower(e.annotation) like lower(concat('%', ?1,'%')) " +
            "or lower(e.description) like lower(concat('%', ?1,'%')) " +
            "and e.category.id in ?2 " +
            "and e.paid = ?3 " +
            "and e.eventDate >= ?4 " +
            "and e.eventDate <= ?5 " +
            "and e.state = 'PUBLISHED'")
    List<Event> getEventsFalse(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                               LocalDateTime rangeEnd, PageRequest page);

    Optional<Event> findByStateAndId(Status state, Long id);

    List<Event> findByInitiator_Id(Long userId, PageRequest page);

    Optional<Event> findByInitiator_IdAndId(Long userId, Long id);

    @Query("select e " +
            "from Event as e " +
            "where e.initiator.id = ?1 " +
            "and e.id = ?2 " +
            "and e.state <> 'PUBLISHED'")
    Optional<Event> getEvent(Long userId, Long eventId);
}
