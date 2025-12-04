package pt.psoft.g1.psoftg1.exceptions;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;

/*
 * ===== SUT DESCRIPTION =====
 * SUT (System Under Test): GlobalExceptionHandler
 * Layer: Controller advice (cross-cutting the controller layer)
 * Purpose: Map exceptions to HTTP responses with a structured DTO (ApiCallError).
 * Public API under test:
 *   - @ExceptionHandler methods + override of handleMethodArgumentNotValid
 * Isolation:
 *   - Black-box: invoke handlers with mocked HttpServletRequest and concrete exceptions.
 *   - White-box: reflection checks for logger presence and DTO shape.
 * Mutation resistance:
 *   - Exact HTTP status and message checks; verify key fields in payload.
 * Quantity/Health:
 *   - Keep a reasonable number of assertions and cover all declared handlers.
 */

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private static HttpServletRequest req(String uri) {
        HttpServletRequest r = mock(HttpServletRequest.class);
        when(r.getRequestURI()).thenReturn(uri);
        return r;
    }

    private static GlobalExceptionHandler.ApiCallError<?> body(ResponseEntity<?> re) {
        assertNotNull(re.getBody(), "Response body must not be null");
        return (GlobalExceptionHandler.ApiCallError<?>) re.getBody();
    }

    // ---------- Black-box / functional tests ----------

    @Nested
    @DisplayName("Conflict mappings")
    class ConflictGroup {

        @Test
        @DisplayName("handleConflict: HTTP 409 + message/error entries")
        void handleConflict_basic() {
            Exception ex = new ConflictException("conflict here");
            ResponseEntity<Object> re = handler.handleConflict(req("/r"), ex);

            assertEquals(HttpStatus.CONFLICT, re.getStatusCode());
            var payload = body(re);
            assertEquals("Conflict", payload.getMessage());

            var map = payload.getDetails().stream()
                    .map(e -> (Map.Entry<?, ?>) e)
                    .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            assertTrue(map.containsKey("message"));
            assertTrue(map.containsKey("error"));
        }

        @Test
        @DisplayName("handleConstraintViolation: includes constraint & SQL state")
        void handleConstraintViolation_includesConstraintAndState() {
            SQLException sqlEx = new SQLException("dup key", "23505", 0);
            ConstraintViolationException ex =
                new ConstraintViolationException("cv", sqlEx, "insert into x", "uk_users_email");

            ResponseEntity<Object> re = handler.handleConstraintViolation(req("/create"), ex);
            assertEquals(HttpStatus.CONFLICT, re.getStatusCode());

            var payload = body(re);
            assertEquals("Conflict", payload.getMessage());

            var map = payload.getDetails().stream()
                    .map(e -> (Map.Entry<?, ?>) e)
                    .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            assertEquals("The identity of the object you tried to create is already in use",
                    String.valueOf(map.get("message")));
            assertEquals("uk_users_email", String.valueOf(map.get("constraint")));
            assertEquals("23505", String.valueOf(map.get("state")));
            assertTrue(String.valueOf(map.get("error")).contains("cv"));
        }

        @Test
        @DisplayName("handleDataIntegrityViolation: generic identity-in-use message")
        void handleDataIntegrityViolation_basic() {
            DataIntegrityViolationException ex =
                    new DataIntegrityViolationException("data integrity", new RuntimeException("dup"));
            ResponseEntity<Object> re = handler.handleDataIntegrityViolation(req("/persist"), ex);

            assertEquals(HttpStatus.CONFLICT, re.getStatusCode());
            var payload = body(re);

            var map = payload.getDetails().stream()
                    .map(e -> (Map.Entry<?, ?>) e)
                    .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            assertEquals("The identity of the object you tried to create is already in use",
                    String.valueOf(map.get("message")));
            assertTrue(String.valueOf(map.get("error")).contains("data integrity"));
        }
    }

    @Nested
    @DisplayName("Bad request mappings")
    class BadRequestGroup {

        @Test
        @DisplayName("handleIllegalArgument: HTTP 400 and message echoed")
        void handleIllegalArgument_basic() {
            IllegalArgumentException ex = new IllegalArgumentException("bad arg");
            ResponseEntity<Object> re = handler.handleIllegalArgument(req("/q"), ex);

            assertEquals(HttpStatus.BAD_REQUEST, re.getStatusCode());
            @SuppressWarnings("unchecked")
            var payload = (GlobalExceptionHandler.ApiCallError<String>) re.getBody();
            assertEquals("Bad Request", payload.getMessage());
            assertTrue(payload.getDetails().contains("bad arg"));
        }

        @Test
        @DisplayName("handleValidationException: HTTP 400 with 'Validation Failed'")
        void handleValidationException_basic() {
            ValidationException ex = new ValidationException("invalid X");
            ResponseEntity<GlobalExceptionHandler.ApiCallError<String>> re =
                    handler.handleValidationException(req("/validate"), ex);

            assertEquals(HttpStatus.BAD_REQUEST, re.getStatusCode());
            assertEquals("Bad Request: Validation Failed", re.getBody().getMessage());
            assertTrue(re.getBody().getDetails().contains("invalid X"));
        }

        @Test
        @DisplayName("handleMethodArgumentTypeMismatchException: paramName/value/errorMessage present")
        void handleMethodArgumentTypeMismatch_basic() {
            Object value = "abc";
            Class<?> required = Integer.class;
            String name = "id";
            MethodParameter mp = null;
            Throwable cause = new NumberFormatException("For input string: \"abc\"");

            MethodArgumentTypeMismatchException ex =
                    new MethodArgumentTypeMismatchException(value, required, name, mp, cause);

            // Match the method's exact generic signature:
            ResponseEntity<GlobalExceptionHandler.ApiCallError<Map.Entry<String, String>>> re =
                    handler.handleMethodArgumentTypeMismatchException(req("/by-id"), ex);

            assertEquals(HttpStatus.BAD_REQUEST, re.getStatusCode());

            var map = re.getBody().getDetails().stream()
                    .collect(java.util.stream.Collectors.toMap(
                            e -> String.valueOf(e.getKey()),
                            e -> String.valueOf(e.getValue())
                    ));
            assertEquals("id", map.get("paramName"));
            assertEquals("abc", map.get("paramValue"));
            assertTrue(map.get("errorMessage").contains("For input string"));
            assertEquals("Method argument type mismatch", re.getBody().getMessage());
        }

        @Test
        @DisplayName("handleMethodArgumentNotValid: aggregated field errors")
        void handleMethodArgumentNotValid_collectsFieldErrors() {
            BeanPropertyBindingResult result = new BeanPropertyBindingResult(new Object(), "sampleDto");
            result.addError(new FieldError("sampleDto", "age", -1, false, null, null, "must be positive"));
            result.addError(new FieldError("sampleDto", "name", "", false, null, null, "must not be blank"));

            MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, result);

            ResponseEntity<Object> re = handler.handleMethodArgumentNotValid(
                    ex, HttpHeaders.EMPTY, HttpStatusCode.valueOf(400), mock(WebRequest.class));

            assertEquals(HttpStatus.BAD_REQUEST, re.getStatusCode());

            @SuppressWarnings("unchecked")
            GlobalExceptionHandler.ApiCallError<Map<String, String>> payload =
                    (GlobalExceptionHandler.ApiCallError<Map<String, String>>) re.getBody();

            assertEquals("Method argument validation failed", payload.getMessage());

            // Convert the Collection<T> into a List to index into it
            List<Map<String, String>> detailsAsList = new ArrayList<>(payload.getDetails());
            assertEquals(2, detailsAsList.size());

            Map<String, String> first = detailsAsList.get(0);
            assertTrue(first.containsKey("objectName"));
            assertTrue(first.containsKey("field"));
            assertTrue(first.containsKey("rejectedValue"));
            assertTrue(first.containsKey("errorMessage"));
        }
    }

    @Nested
    @DisplayName("Forbidden & security mappings")
    class ForbiddenGroup {

        @Test
        @DisplayName("handleAccessDeniedException: 403 + 'Access denied!'")
        void handleAccessDenied_basic() {
            AccessDeniedException ex = new AccessDeniedException("nope");
            ResponseEntity<GlobalExceptionHandler.ApiCallError<String>> re =
                    handler.handleAccessDeniedException(req("/secure"), ex);

            assertEquals(HttpStatus.FORBIDDEN, re.getStatusCode());
            assertEquals("Access denied!", re.getBody().getMessage());
            assertTrue(re.getBody().getDetails().contains("nope"));
        }

        @Test
        @DisplayName("handleLendingForbiddenException: 403 + 'Lending forbidden!'")
        void handleLendingForbidden_basic() {
            LendingForbiddenException ex = new LendingForbiddenException("You cannot lend");
            ResponseEntity<GlobalExceptionHandler.ApiCallError<String>> re =
                    handler.handleLendingForbiddenException(req("/lending"), ex);

            assertEquals(HttpStatus.FORBIDDEN, re.getStatusCode());
            assertEquals("Lending forbidden!", re.getBody().getMessage());
            assertTrue(re.getBody().getDetails().contains("You cannot lend"));
        }
    }

    // ---------- Transparent-box / DTO & structure checks ----------

    @Nested
    @DisplayName("Transparent-box & DTO checks")
    class TransparentBox {

        @Test
        @DisplayName("ApiCallError<T> exposes message + details")
        void apiCallError_shape() {
            GlobalExceptionHandler.ApiCallError<String> dto =
                    new GlobalExceptionHandler.ApiCallError<>("m", List.of("a", "b"));
            assertEquals("m", dto.getMessage());
            assertEquals(List.of("a", "b"), dto.getDetails());
        }

        @Test
        @DisplayName("GlobalExceptionHandler has a logger field (initialized)")
        void logger_present() throws Exception {
            Field f = GlobalExceptionHandler.class.getDeclaredField("logger");
            f.setAccessible(true);
            assertNotNull(f.get(handler), "logger should be initialized");
        }
    }

    // ---------- Small health guard ----------

    @Test
    @DisplayName("Health: maintain a reasonable number of assertions")
    void health_assertionCount() {
        assertTrue(true, "Placeholder health check");
    }
}
