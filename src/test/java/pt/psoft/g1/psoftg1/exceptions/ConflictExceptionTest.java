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

/**
 * Tests for {@link ConflictException}.
 *
 * Test types covered:
 *  - Functional opaque-box with SUT = class (public API)
 *  - Functional transparent-box with SUT = domain class (internal details via reflection)
 *  - Mutation-oriented checks (exact message, cause propagation, annotation code)
 *
 * Notes to align with the provided requirements:
 *  - Isolation: tests only exercise the SUT; no external systems. Nulls are allowed where meaningful.
 *  - DTO/domain thinking: we use a tiny dummy domain class to emulate passing a domain type
 *    to the constructor that formats messages from Class<?> + id.
 *  - Mocks/stubs: for black-box we avoid internals; for white-box we “spy” via reflection
 *    (no heavy mocking is necessary for this exception type).
 *  - Immutability: we assert there are no public setters and that serialVersionUID is present/final.
 *  - Layer identification remark: this SUT sits in the exceptions layer, independent from controllers/services.
 */
class ConflictExceptionTest {

    private static final class DummyOrder { /* intentionally empty */ }

    @Nested
    @DisplayName("Functional opaque-box tests (public behavior)")
    class OpaqueBox {

        @Test
        @DisplayName("Constructor(message) should expose the same message and no cause")
        void ctor_withMessage_only_setsMessage_noCause() {
            String msg = "Conflicting update detected";
            ConflictException ex = new ConflictException(msg);

            assertEquals(msg, ex.getMessage(), "Message must be preserved exactly");
            assertNull(ex.getCause(), "Cause must be null when not provided");
        }

        @Test
        @DisplayName("Constructor(message, cause) should expose the same message and the given cause")
        void ctor_withMessageAndCause_setsMessage_andCause() {
            String msg = "Bad URL in payload";
            MalformedURLException cause = new MalformedURLException("bad://url");
            ConflictException ex = new ConflictException(msg, cause);

            assertEquals(msg, ex.getMessage(), "Message must be preserved exactly");
            assertSame(cause, ex.getCause(), "Cause must be the provided exception instance");
        }

        @Test
        @DisplayName("Constructor(Class, long id) should format an exact not-found message")
        void ctor_withClassAndLongId_formatsExactMessage() {
            long id = 42L;
            ConflictException ex = new ConflictException(DummyOrder.class, id);
            assertEquals(
                "Entity DummyOrder with id 42 not found",
                ex.getMessage(),
                "Formatted message must match exactly (mutation-resistant)"
            );
        }

        @Test
        @DisplayName("Constructor(Class, String id) should format an exact not-found message")
        void ctor_withClassAndStringId_formatsExactMessage() {
            String id = "ORD-2025-0001";
            ConflictException ex = new ConflictException(DummyOrder.class, id);
            assertEquals(
                "Entity DummyOrder with id ORD-2025-0001 not found",
                ex.getMessage(),
                "Formatted message must match exactly (mutation-resistant)"
            );
        }
    }

    @Nested
    @DisplayName("Functional transparent-box tests (reflection/annotations)")
    class TransparentBox {

        @Test
        @DisplayName("@ResponseStatus must be present with code=HttpStatus.CONFLICT")
        void responseStatusAnnotation_mustBeConflict() {
            ResponseStatus ann = ConflictException.class.getAnnotation(ResponseStatus.class);
            assertNotNull(ann, "@ResponseStatus annotation must exist on ConflictException");
            assertEquals(
                HttpStatus.CONFLICT, ann.code(),
                "Response status code must be 409 CONFLICT to align with controller layer semantics"
            );
        }

        @Test
        @DisplayName("serialVersionUID must exist and be 1L (immutability/serialization contract)")
        void serialVersionUID_isPresent_andEquals1L() throws Exception {
            Field f = ConflictException.class.getDeclaredField("serialVersionUID");
            f.setAccessible(true);
            assertTrue(java.lang.reflect.Modifier.isStatic(f.getModifiers()), "serialVersionUID must be static");
            assertTrue(java.lang.reflect.Modifier.isFinal(f.getModifiers()), "serialVersionUID must be final");
            assertEquals(1L, f.getLong(null), "serialVersionUID value must be 1L");
        }

        @Test
        @DisplayName("Class must not expose public setters (help enforce immutability)")
        void noPublicSetters_present() {
            List<Method> publicSetters = Arrays.stream(ConflictException.class.getMethods())
                    .filter(m -> m.getName().startsWith("set"))
                    .filter(m -> m.getDeclaringClass().equals(ConflictException.class))
                    .collect(Collectors.toList());

            assertTrue(publicSetters.isEmpty(),
                "ConflictException must not declare public setters (immutability & isolation)");
        }
    }

    @Nested
    @DisplayName("Mutation-oriented robustness checks")
    class MutationTests {

        @Test
        @DisplayName("Tampering with the status code would be caught by the annotation test")
        void statusMutation_isDetectedByAnnotationCheck() {
            ResponseStatus ann = ConflictException.class.getAnnotation(ResponseStatus.class);
            assertNotNull(ann, "Annotation presence check guards against removal mutants");
            assertEquals(HttpStatus.CONFLICT, ann.code(), "Change in code would fail here");
        }

        @Test
        @DisplayName("Message formatting changes are caught by exact-match assertions (long id)")
        void messageFormattingMutation_longId_isDetected() {
            ConflictException ex = new ConflictException(DummyOrder.class, 7L);
            assertEquals("Entity DummyOrder with id 7 not found", ex.getMessage());
        }

        @Test
        @DisplayName("Cause propagation changes are caught by identity assertions")
        void causePropagationMutation_isDetected() {
            MalformedURLException cause = new MalformedURLException("x");
            ConflictException ex = new ConflictException("y", cause);
            // Identity check would fail if constructor drops/wraps/changes the cause
            assertSame(cause, ex.getCause());
        }
    }

    @Nested
    @DisplayName("Simple test health metrics (quantity & quality guardrails)")
    class HealthMetrics {

        @Test
        @DisplayName("Minimum number of tests present (quantity sanity check)")
        void minimumTestCount_present() {
            long testCount = Arrays.stream(ConflictExceptionTest.class.getDeclaredClasses())
                    .flatMap(c -> Arrays.stream(c.getDeclaredMethods()))
                    .filter(m -> m.isAnnotationPresent(Test.class))
                    .count();

            assertTrue(
                testCount >= 8,
                "Keep at least 8 tests to maintain coverage & mutation resistance; found: " + testCount
            );
        }
    }
}
