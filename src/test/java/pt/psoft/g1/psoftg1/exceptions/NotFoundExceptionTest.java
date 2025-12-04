package pt.psoft.g1.psoftg1.exceptions;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/*
 * ===== SUT DESCRIPTION =====
 * SUT (System Under Test): NotFoundException
 * Layer: Exceptions (consumed by controllers/services; annotated to map to HTTP 404)
 * Purpose: Immutable runtime exception with message/cause and formatted "not found" messages from Class + id.
 *
 * Public API under test: all constructors, Throwable getters; class-level @ResponseStatus metadata.
 *
 * Testing strategy:
 *  - Functional opaque-box (black box): assert observable behavior (message, cause).
 *  - Functional transparent-box (white box): reflectively assert annotation (code+reason), serialVersionUID,
 *    and absence of public setters (immutability).
 *  - Mutation-oriented checks: exact string/message assertions and identity checks for cause.
 *  - Health metrics: guard on minimum number of tests.
 *
 * Isolation: No external systems; no Spring context; no I/O. Nulls are acceptable where meaningful.
 * DTO thinking: the exception acts as a small immutable DTO conveying an error state.
 */

class NotFoundExceptionTest {

    private static final class DummyEntity { }

    @Nested
    @DisplayName("Functional opaque-box tests (public behavior)")
    class OpaqueBox {

        @Test
        @DisplayName("Constructor(message) preserves message and no cause")
        void ctor_message_only() {
            String msg = "Resource was not found";
            NotFoundException ex = new NotFoundException(msg);

            assertEquals(msg, ex.getMessage());
            assertNull(ex.getCause());
        }

        @Test
        @DisplayName("Constructor(message, cause) preserves message and cause identity")
        void ctor_message_and_cause() throws MalformedURLException {
            String msg = "Bad URL while locating resource";
            MalformedURLException cause = new MalformedURLException("bad://url");
            NotFoundException ex = new NotFoundException(msg, cause);

            assertEquals(msg, ex.getMessage());
            assertSame(cause, ex.getCause());
        }

        @Test
        @DisplayName("Constructor(Class,long) formats exact message")
        void ctor_class_long_formats() {
            NotFoundException ex = new NotFoundException(DummyEntity.class, 123L);
            assertEquals("Entity DummyEntity with id 123 not found", ex.getMessage());
        }

        @Test
        @DisplayName("Constructor(Class,String) formats exact message")
        void ctor_class_string_formats() {
            NotFoundException ex = new NotFoundException(DummyEntity.class, "ABC-123");
            assertEquals("Entity DummyEntity with id ABC-123 not found", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Functional transparent-box tests (reflection/annotations)")
    class TransparentBox {

        @Test
        @DisplayName("@ResponseStatus must be NOT_FOUND with reason 'Object Not Found'")
        void responseStatus_annotation_values() {
            ResponseStatus ann = NotFoundException.class.getAnnotation(ResponseStatus.class);
            assertNotNull(ann, "@ResponseStatus must be present");
            assertEquals(HttpStatus.NOT_FOUND, ann.code(), "HTTP status must be 404 NOT_FOUND");
            assertEquals("Object Not Found", ann.reason(), "Reason must match exactly");
        }

        @Test
        @DisplayName("serialVersionUID exists and equals 1L (immutability/serialization contract)")
        void serialVersionUID_present_and_1L() throws Exception {
            Field f = NotFoundException.class.getDeclaredField("serialVersionUID");
            f.setAccessible(true);
            int mod = f.getModifiers();
            assertTrue(java.lang.reflect.Modifier.isStatic(mod));
            assertTrue(java.lang.reflect.Modifier.isFinal(mod));
            assertEquals(1L, f.getLong(null));
        }

        @Test
        @DisplayName("Class must not declare public setters (immutability)")
        void no_public_setters() {
            List<Method> setters = Arrays.stream(NotFoundException.class.getMethods())
                    .filter(m -> m.getName().startsWith("set"))
                    .filter(m -> m.getDeclaringClass().equals(NotFoundException.class))
                    .collect(Collectors.toList());
            assertTrue(setters.isEmpty(), "No public setters should be declared");
        }
    }

    @Nested
    @DisplayName("Mutation-oriented robustness checks")
    class Mutation {

        @Test
        @DisplayName("Exact message comparison kills formatting mutants (long id)")
        void message_exact_match_long() {
            NotFoundException ex = new NotFoundException(DummyEntity.class, 7L);
            assertEquals("Entity DummyEntity with id 7 not found", ex.getMessage());
        }

        @Test
        @DisplayName("Cause identity check kills dropped/wrapped-cause mutants")
        void cause_identity() throws MalformedURLException {
            MalformedURLException cause = new MalformedURLException("x");
            NotFoundException ex = new NotFoundException("y", cause);
            assertSame(cause, ex.getCause());
        }
    }

    @Test
    @DisplayName("Health: keep a minimum number of tests")
    void health_minimum_tests() {
        long testCount = Arrays.stream(NotFoundExceptionTest.class.getDeclaredClasses())
                .flatMap(c -> Arrays.stream(c.getDeclaredMethods()))
                .filter(m -> m.isAnnotationPresent(Test.class))
                .count();
        assertTrue(testCount >= 8, "Maintain ≥8 tests; found: " + testCount);
    }
}
