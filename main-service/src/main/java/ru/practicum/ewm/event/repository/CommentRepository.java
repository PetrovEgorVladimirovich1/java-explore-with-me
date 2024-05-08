package ru.practicum.ewm.event.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.event.model.Comment;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Optional<Comment> findByAuthor_IdAndEvent_IdAndId(Long userId, Long eventId, Long commentId);

    List<Comment> findByAuthor_IdAndEvent_Id(Long userId, Long eventId, PageRequest pageRequest);

    List<Comment> findByAuthor_Id(Long userId, PageRequest pageRequest);

    List<Comment> findByEvent_Id(Long eventId, PageRequest pageRequest);
}
