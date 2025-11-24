package com.exception;
import com.exception.AccountNotFoundException;
import com.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.junit.jupiter.api.Assertions;
import java.util.List;
import java.util.Map;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFound_returns404() {
        ResponseEntity<?> r = handler.handleNotFound(new AccountNotFoundException("no"));
        assertEquals(404, r.getStatusCodeValue());
        Map<?,?> body = (Map<?,?>) r.getBody();
        assertEquals("NOT_FOUND", body.get("code"));
    }

    @Test
    void handleBadRequest_forInvalidAmount() {
        ResponseEntity<?> r = handler.handleBadRequest(new RuntimeException("bad"));
        assertEquals(400, r.getStatusCodeValue());
    }

    @Test
    void handleValidation_returns_message_from_binding() {

        // Create mock BindingResult
        BindingResult bindingResult = Mockito.mock(BindingResult.class);

        FieldError fieldError = new FieldError("objectName", "holderName", "Name required");

        Mockito.when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(null, bindingResult);

        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<?> response = handler.handleValidation(ex);

        Map<String, Object> body = (Map<String, Object>) response.getBody();

        Assertions.assertEquals("VALIDATION_ERROR", body.get("code"));
        Assertions.assertEquals("Name required", body.get("message"));
        Assertions.assertEquals(400, response.getStatusCode().value());
    }
}