/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.common.exception.misc;

import eu.arrowhead.common.exception.ArrowheadException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ArrowheadExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(ArrowheadException.class)
  protected final ResponseEntity<ErrorMessage> handleArrowheadException(ArrowheadException ex, WebRequest request) {
    ErrorMessage error = new ErrorMessage(LocalDateTime.now(), ex.getErrorCode(), ex.getMessage(), request.getDescription(false),
                                          ex.getExceptionType());
    return new ResponseEntity<ErrorMessage>(error, new HttpHeaders(), HttpStatus.valueOf(ex.getErrorCode()));
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status,
                                                                WebRequest request) {
    final List<String> errors = new ArrayList<String>();
    for (final FieldError error : ex.getBindingResult().getFieldErrors()) {
      errors.add(error.getField() + ": " + error.getDefaultMessage());
    }
    for (final ObjectError error : ex.getBindingResult().getGlobalErrors()) {
      errors.add(error.getObjectName() + ": " + error.getDefaultMessage());
    }
    String errorMessage = String.join(", ", errors);

    ErrorMessage error = new ErrorMessage(LocalDateTime.now(), 400, errorMessage, request.getDescription(false), ExceptionType.VALIDATION);
    return new ResponseEntity<Object>(error, new HttpHeaders(), HttpStatus.BAD_REQUEST);
  }
}
