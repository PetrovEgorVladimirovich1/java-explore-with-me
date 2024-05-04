package ru.practicum.ewm.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.ewm.exceptions.FailBadException;
import ru.practicum.ewm.exceptions.FailConflictException;
import ru.practicum.ewm.exceptions.FailIdException;

import javax.validation.ValidationException;
import java.sql.SQLException;
import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        log.info(e.getMessage());
        return new ApiError(e.getMessage(), e.getObjectName(), HttpStatus.BAD_REQUEST.name(),
                LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleSQLException(final SQLException e) {
        log.info(e.getMessage());
        return new ApiError(e.getMessage(), e.getLocalizedMessage(), HttpStatus.CONFLICT.name(),
                LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleFailIdException(final FailIdException e) {
        log.info(e.getMessage());
        return new ApiError(e.getMessage(), e.getLocalizedMessage(), HttpStatus.NOT_FOUND.name(),
                LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleFailConflictException(final FailConflictException e) {
        log.info(e.getMessage());
        return new ApiError(e.getMessage(), e.getLocalizedMessage(), HttpStatus.CONFLICT.name(),
                LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleFailBadException(final FailBadException e) {
        log.info(e.getMessage());
        return new ApiError(e.getMessage(), e.getLocalizedMessage(), HttpStatus.BAD_REQUEST.name(),
                LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(final ValidationException e) {
        log.info(e.getMessage());
        return new ApiError(e.getMessage(), e.getLocalizedMessage(), HttpStatus.BAD_REQUEST.name(),
                LocalDateTime.now());
    }
}
