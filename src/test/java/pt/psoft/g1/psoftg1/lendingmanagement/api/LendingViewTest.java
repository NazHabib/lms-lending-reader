package pt.psoft.g1.psoftg1.lendingmanagement.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * SUT: LendingView (DTO)
 * Tipo: Unitário e isolado.
 *
 * Campos obrigatórios (com @NotNull):
 *  - lendingNumber, bookTitle, startDate, limitDate
 *
 * Campos opcionais:
 *  - returnedDate, daysUntilReturn, daysOverdue, fineValueInCents, _links
 */
class LendingViewTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
        validator = vf.getValidator();
    }

    private static LendingView valid() {
        LendingView v = new LendingView();
        v.setLendingNumber("2025/42");
        v.setBookTitle("Clean Code");
        v.setStartDate(LocalDate.of(2025, 1, 10));
        v.setLimitDate(LocalDate.of(2025, 1, 25));
        return v;
    }

    @Test
    @DisplayName("Lombok getters/setters work (including optional fields and _links)")
    void gettersSetters() {
        LendingView v = new LendingView();
        v.setLendingNumber("2025/7");
        v.setBookTitle("Domain-Driven Design");
        v.setStartDate(LocalDate.of(2025, 2, 1));
        v.setLimitDate(LocalDate.of(2025, 2, 15));
        v.setReturnedDate(LocalDate.of(2025, 2, 12));
        v.setDaysUntilReturn(3);
        v.setDaysOverdue(0);
        v.setFineValueInCents(0);

        LendingLinksView links = new LendingLinksView();
        v.set_links(links);

        assertEquals("2025/7", v.getLendingNumber());
        assertEquals("Domain-Driven Design", v.getBookTitle());
        assertEquals(LocalDate.of(2025, 2, 1), v.getStartDate());
        assertEquals(LocalDate.of(2025, 2, 15), v.getLimitDate());
        assertEquals(LocalDate.of(2025, 2, 12), v.getReturnedDate());
        assertEquals(3, v.getDaysUntilReturn());
        assertEquals(0, v.getDaysOverdue());
        assertEquals(0, v.getFineValueInCents());
        assertSame(links, v.get_links());
    }

    @Test
    @DisplayName("@Schema present on class and @NotNull present on required fields")
    void annotationsPresent() throws Exception {
        assertNotNull(LendingView.class.getAnnotation(Schema.class),
                "@Schema must be present on class");

        for (String fieldName : new String[]{"lendingNumber", "bookTitle", "startDate", "limitDate"}) {
            Field f = LendingView.class.getDeclaredField(fieldName);
            assertNotNull(f.getAnnotation(NotNull.class),
                    () -> "@NotNull must be present on field: " + fieldName);
        }
    }

    @Test
    @DisplayName("Bean Validation: valid object passes, null in required fields fails")
    void beanValidation() {
        var ok = valid();
        assertTrue(validator.validate(ok).isEmpty(), "Valid DTO should pass Bean Validation");

        // lendingNumber null
        var bad1 = valid();
        bad1.setLendingNumber(null);
        assertFalse(validator.validate(bad1).isEmpty(), "lendingNumber null must violate @NotNull");

        // bookTitle null
        var bad2 = valid();
        bad2.setBookTitle(null);
        assertFalse(validator.validate(bad2).isEmpty(), "bookTitle null must violate @NotNull");

        // startDate null
        var bad3 = valid();
        bad3.setStartDate(null);
        assertFalse(validator.validate(bad3).isEmpty(), "startDate null must violate @NotNull");

        // limitDate null
        var bad4 = valid();
        bad4.setLimitDate(null);
        assertFalse(validator.validate(bad4).isEmpty(), "limitDate null must violate @NotNull");
    }

    @Test
    @DisplayName("Optional fields can be null without violations")
    void optionalFieldsNullable_ok() {
        var v = valid();
        v.setReturnedDate(null);
        v.setDaysUntilReturn(null);
        v.setDaysOverdue(null);
        v.setFineValueInCents(null);

        Set<?> violations = validator.validate(v);
        assertTrue(violations.isEmpty(), "Optional fields being null should not violate constraints");
    }

    @Test
    @DisplayName("equals/hashCode: equal with same data; different when one field differs")
    void equalsHashCode() {
        var a = valid();
        a.setReturnedDate(LocalDate.of(2025, 1, 20));
        a.setDaysUntilReturn(1);
        a.setDaysOverdue(0);
        a.setFineValueInCents(100);

        var b = valid();
        b.setReturnedDate(LocalDate.of(2025, 1, 20));
        b.setDaysUntilReturn(1);
        b.setDaysOverdue(0);
        b.setFineValueInCents(100);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());

        var c = valid();
        c.setBookTitle("Different");
        assertNotEquals(a, c);
    }

    @Test
    @DisplayName("toString contains key fields")
    void toStringContainsKeyFields() {
        var v = valid();
        String s = v.toString();
        assertTrue(s.contains("2025/42"));
        assertTrue(s.contains("Clean Code"));
        assertTrue(s.contains("startDate"));
        assertTrue(s.contains("limitDate"));
    }

    @Test
    @DisplayName("Mockito: can inject a mocked LendingLinksView into _links")
    void mockitoForLinks() {
        LendingLinksView mockedLinks = mock(LendingLinksView.class);
        LendingView v = valid();
        v.set_links(mockedLinks);

        assertSame(mockedLinks, v.get_links());
        Mockito.verifyNoInteractions(mockedLinks);
    }

    @Test
    @DisplayName("Basic equals contracts: reflexive, null, different class")
    void equalsContracts() {
        var v = valid();
        assertEquals(v, v);          // reflexive
        assertNotEquals(v, null);    // null
        assertNotEquals(v, "text");  // different class
    }
}
