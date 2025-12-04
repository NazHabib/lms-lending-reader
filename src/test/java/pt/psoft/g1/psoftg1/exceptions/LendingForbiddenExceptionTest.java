package pt.psoft.g1.psoftg1.exceptions;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/*
 * ===== SUT DESCRIPTION =====
 * SUT (System Under Test): LendingForbiddenException
 * Layer: Exceptions (used by controllers/services to signal HTTP 403 forbidden actions)
 * Purpose: Immutable runtime exception that carries a message and optional cause.
 *
 * Public API under test:
 *   - Constructors: (String), (String, Throwable)
 *   - Inherited methods: getMessage(), getCause()
 *
 * Testing strategy:
 *   - Functional opaque-box: verify externally visible behavior (messages, causes)
 *   - Functional transparent-box: reflection to verify immutability cues (no setters)
 *   - Mutation-oriented: check exact message propagation and cause identity
 *   - Health metrics: ensure test quantity and quality (≥ 5 tests)
 *
 * Isolation: No dependencies, no I/O, no Spring context.
 * DTO thinking: this exception itself is a simple immutable DTO for error signaling.
 */

class LendingForbiddenExceptionTest {

    @Nested
    @DisplayName("Functional opaque-box tests")
    class OpaqueBox {

        @Test
        @DisplayName("Constructor(message) preserves message and null cause")
        void ctor_withMessage_setsMessage_noCause() {
            String msg = "Lending operation is forbidden";
            LendingForbiddenException ex = new LendingForbiddenException(msg);

            assertEquals(msg, ex.getMessage(), "Message must be preserved exactly");
            assertNull(ex.getCause(), "Cause must be null when not provided");
        }

        @Test
        @DisplayName("Constructor(message, cause) preserves message and cause identity")
        void ctor_withMessageAndCause_setsMessage_andCause() {
            Throwable cause = new IllegalStateException("User lacks permission");
            LendingForbiddenException ex = new LendingForbiddenException("Lending blocked", cause);

            assertEquals("Lending blocked", ex.getMessage());
            assertSame(cause, ex.getCause(), "Cause must match provided instance");
        }
    }

    @Nested
    @DisplayName("Functional transparent-box tests (reflection)")
    class TransparentBox {

        @Test
        @DisplayName("No public setters present (immutability)")
        void noPublicSetters_present() {
            List<Method> setters = Arrays.stream(LendingForbiddenException.class.getMethods())
                    .filter(m -> m.getName().startsWith("set"))
                    .filter(m -> m.getDeclaringClass().equals(LendingForbiddenException.class))
                    .collect(Collectors.toList());

            assertTrue(setters.isEmpty(), "Class must not declare public setters");
        }

        @Test
        @DisplayName("Class extends RuntimeException (hierarchy check)")
        void extendsRuntimeException() {
            assertTrue(RuntimeException.class.isAssignableFrom(LendingForbiddenException.class),
                    "LendingForbiddenException must extend RuntimeException");
        }

        @Test
        @DisplayName("serialVersionUID is not declared (still valid serializable runtime exception)")
        void serialVersionUID_absent_isFine() {
            // Ensure no declared serialVersionUID field exists (legal absence)
            boolean hasField = Arrays.stream(LendingForbiddenException.class.getDeclaredFields())
                    .map(Field::getName)
                    .anyMatch("serialVersionUID"::equals);
            assertFalse(hasField, "serialVersionUID field is optional and not declared here");
        }
    }

    @Nested
    @DisplayName("Mutation-oriented robustness tests")
    class MutationTests {

        @Test
        @DisplayName("Exact message match kills string-format mutants")
        void messageExactMatch_preventsMutants() {
            LendingForbiddenException ex = new LendingForbiddenException("Forbidden X");
            assertEquals("Forbidden X", ex.getMessage());
        }

        @Test
        @DisplayName("Cause identity check kills dropped/wrapped-cause mutants")
        void causeIdentity_preventsMutants() {
            Throwable cause = new RuntimeException("Y");
            LendingForbiddenException ex = new LendingForbiddenException("msg", cause);
            assertSame(cause, ex.getCause());
        }
    }

    @Test
    @DisplayName("Health: ensure minimum test coverage count")
    void health_testCount() {
        long count = Arrays.stream(LendingForbiddenExceptionTest.class.getDeclaredClasses())
                .flatMap(c -> Arrays.stream(c.getDeclaredMethods()))
                .filter(m -> m.isAnnotationPresent(Test.class))
                .count();
        assertTrue(count >= 5, "Maintain ≥5 tests; found: " + count);
    }
}
