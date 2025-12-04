package pt.psoft.g1.psoftg1.lendingmanagement.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SUT: LendingsAverageDurationView (simple DTO)
 */
class LendingsAverageDurationViewTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
        validator = vf.getValidator();
    }

    @Test
    @DisplayName("Lombok getters/setters work")
    void gettersSetters() {
        var v = new LendingsAverageDurationView();
        v.setLendingsAverageDuration(3.7);
        assertEquals(3.7, v.getLendingsAverageDuration());
    }

    @Test
    @DisplayName("@Schema present on class and @NotNull present on field")
    void annotationsPresent() throws Exception {
        assertNotNull(LendingsAverageDurationView.class.getAnnotation(Schema.class),
                "@Schema must be present on class");

        Field f = LendingsAverageDurationView.class.getDeclaredField("lendingsAverageDuration");
        assertNotNull(f.getAnnotation(NotNull.class),
                "@NotNull must be present on lendingsAverageDuration");
    }

    @Test
    @DisplayName("Bean Validation: null violates @NotNull; non-null passes")
    void beanValidation() {
        var invalid = new LendingsAverageDurationView();
        invalid.setLendingsAverageDuration(null);
        assertFalse(validator.validate(invalid).isEmpty(), "Null must violate @NotNull");

        var ok = new LendingsAverageDurationView();
        ok.setLendingsAverageDuration(1.0);
        assertTrue(validator.validate(ok).isEmpty(), "Non-null should pass validation");
    }

    @Test
    @DisplayName("equals/hashCode: equal when same value; different when value differs")
    void equalsHashCode() {
        var a = new LendingsAverageDurationView();
        a.setLendingsAverageDuration(2.5);

        var b = new LendingsAverageDurationView();
        b.setLendingsAverageDuration(2.5);

        var c = new LendingsAverageDurationView();
        c.setLendingsAverageDuration(3.1);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }

    @Test
    @DisplayName("toString contains field value")
    void toStringContainsValue() {
        var v = new LendingsAverageDurationView();
        v.setLendingsAverageDuration(4.2);
        String s = v.toString();
        assertTrue(s.contains("4.2"));
    }

    @Test
    @DisplayName("Reflexive, null, and different-class equality checks")
    void equalsBasicContracts() {
        var v = new LendingsAverageDurationView();
        v.setLendingsAverageDuration(1.1);

        assertEquals(v, v);              // reflexive
        assertNotEquals(v, null);        // null
        assertNotEquals(v, "string");    // different class
    }
}
