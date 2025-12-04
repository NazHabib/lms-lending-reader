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
 * SUT (System Under Test): FileStorageException
 * Layer: Exceptions/Domain support (independent from controllers/services)
 * Purpose: Convey storage-related failures with immutable message/cause, serializable.
 * Public API under test: two constructors (String), (String, Throwable) and Throwable getters
 * Isolation strategy:
 *  - Functional opaque-box tests assert observable behavior (message, cause).
 *  - Transparent-box tests use reflection to verify immutability cues (no setters) and serialVersionUID.
 * Test types mapped to requirements:
 *  - Functional opaque-box with SUT = class (black-box; no mocks needed).
 *  - Functional transparent-box with SUT = domain class (reflection, akin to stubs/spies).
 *  - Mutation-oriented checks: exact message equality, identity of cause, serialVersionUID presence.
 *  - Health metrics: minimal test-count guard (quantity) and assertions that kill common mutants (quality).
 * Isolation notes:
 *  - No external systems or foreign keys; null causes are acceptable and do not violate isolation.
 *  - Each “part” is its own class; DTO thinking is trivial here (exception holds immutable data).
 */

class FileStorageExceptionTest {

    @Nested
    @DisplayName("Functional opaque-box tests (public behavior)")
    class OpaqueBox {

        @Test
        @DisplayName("Constructor(message) preserves message, no cause")
        void ctor_withMessage_only_setsMessage_noCause() {
            String msg = "Failed to write file";
            FileStorageException ex = new FileStorageException(msg);

            assertEquals(msg, ex.getMessage(), "Message must be preserved exactly");
            assertNull(ex.getCause(), "Cause must be null when not provided");
        }

        @Test
        @DisplayName("Constructor(message, cause) preserves message and cause identity")
        void ctor_withMessageAndCause_setsMessage_andCause() {
            Throwable cause = new IllegalStateException("disk full");
            FileStorageException ex = new FileStorageException("store failed", cause);

            assertEquals("store failed", ex.getMessage());
            assertSame(cause, ex.getCause(), "Cause must be the provided instance");
        }
    }

    @Nested
    @DisplayName("Functional transparent-box tests (reflection/immutability)")
    class TransparentBox {

        @Test
        @DisplayName("serialVersionUID exists and is 1L")
        void serialVersionUID_present_andEquals1L() throws Exception {
            Field f = FileStorageException.class.getDeclaredField("serialVersionUID");
            f.setAccessible(true);
            assertTrue(java.lang.reflect.Modifier.isStatic(f.getModifiers()));
            assertTrue(java.lang.reflect.Modifier.isFinal(f.getModifiers()));
            assertEquals(1L, f.getLong(null));
        }

        @Test
        @DisplayName("No public setters (immutability)")
        void noPublicSetters_present() {
            List<Method> setters = Arrays.stream(FileStorageException.class.getMethods())
                    .filter(m -> m.getName().startsWith("set"))
                    .filter(m -> m.getDeclaringClass().equals(FileStorageException.class))
                    .collect(Collectors.toList());
            assertTrue(setters.isEmpty(), "Exception must not declare public setters");
        }
    }

    @Nested
    @DisplayName("Mutation-oriented checks")
    class MutationTests {

        @Test
        @DisplayName("Exact message match kills string-format mutants")
        void exactMessage_match_required() {
            FileStorageException ex = new FileStorageException("write failed");
            assertEquals("write failed", ex.getMessage());
        }

        @Test
        @DisplayName("Cause identity check kills dropped/wrapped-cause mutants")
        void cause_identity_required() {
            Throwable cause = new RuntimeException("io");
            FileStorageException ex = new FileStorageException("x", cause);
            assertSame(cause, ex.getCause());
        }
    }

    @Nested
    @DisplayName("Test health metrics")
    class HealthMetrics {

        @Test
        @DisplayName("Keep at least 6 tests (quantity guard)")
        void minimumTestCount_present() {
            long testCount = Arrays.stream(FileStorageExceptionTest.class.getDeclaredClasses())
                    .flatMap(c -> Arrays.stream(c.getDeclaredMethods()))
                    .filter(m -> m.isAnnotationPresent(Test.class))
                    .count();
            assertTrue(testCount >= 6, "Maintain ≥6 tests; found: " + testCount);
        }
    }
}
