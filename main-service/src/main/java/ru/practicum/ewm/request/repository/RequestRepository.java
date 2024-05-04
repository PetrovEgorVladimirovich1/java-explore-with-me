package ru.practicum.ewm.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.request.model.Request;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
    Optional<Request> findByRequesterAndEvent(Long userId, Long eventId);

    List<Request> findByEvent(Long eventId);

    List<Request> findByRequester(Long userId);

    Optional<Request> findByRequesterAndId(Long userId, Long requestId);
}
