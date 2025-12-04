package pt.psoft.g1.psoftg1.readermanagement.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.*;

import java.lang.annotation.Annotation;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/*
* ReaderCountView (DTO, package pt.psoft.g1.psoftg1.readermanagement.api)

Type: Pure DTO (no Spring / DB).
Black-box: Lombok–generated behavior (getters/setters, equals/hashCode/toString).
White-box: Field–level validation and presence of OpenAPI annotations.

We verify:

@Schema present on class.

@NotNull present on readerView and enforced by Bean Validation.

Lombok @Data – getters/setters work; equals/hashCode correctness; toString is safe.

Null/edge handling (e.g., readerView null vs non-null).
* */

@DisplayName("ReaderCountView – DTO contract, Lombok data, and validation")
class ReaderCountViewTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
        validator = vf.getValidator();
    }

    private static ReaderView newReaderView() {
        // ReaderView is another DTO; no assumptions on its fields are required here.
        return new ReaderView();
    }

    @Test
    @DisplayName("@Schema is present on the class")
    void schemaAnnotation_present() {
        Annotation ann = ReaderCountView.class.getAnnotation(Schema.class);
        assertNotNull(ann, "@Schema must be present on ReaderCountView");
    }

    @Test
    @DisplayName("@NotNull is present on readerView field")
    void notNullAnnotation_present_on_readerView() throws Exception {
        var field = ReaderCountView.class.getDeclaredField("readerView");
        assertNotNull(field.getAnnotation(NotNull.class),
                "readerView must be annotated with @NotNull");
    }

    @Test
    @DisplayName("Bean Validation: readerView=null violates @NotNull")
    void beanValidation_readerView_null_fails() {
        var dto = new ReaderCountView();
        dto.setReaderView(null);     // violates @NotNull
        dto.setLendingCount(5L);

        Set<?> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Expected @NotNull violation on readerView");
    }

    @Test
    @DisplayName("Bean Validation: readerView set -> passes")
    void beanValidation_readerView_set_passes() {
        var dto = new ReaderCountView();
        dto.setReaderView(newReaderView());
        dto.setLendingCount(7L);

        var violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "DTO should be valid when readerView is set");
    }

    @Test
    @DisplayName("Lombok @Data: getters/setters work")
    void lombok_getters_setters() {
        var dto = new ReaderCountView();

        var rv = newReaderView();
        dto.setReaderView(rv);
        dto.setLendingCount(10L);

        assertSame(rv, dto.getReaderView());
        assertEquals(10L, dto.getLendingCount());
    }

    @Test
    @DisplayName("equals/hashCode: identical state -> equal and same hash")
    void equals_hashCode_equalObjects() {
        var a = new ReaderCountView();
        a.setReaderView(newReaderView());
        a.setLendingCount(3L);

        var b = new ReaderCountView();
        b.setReaderView(newReaderView()); // another ReaderView with same default state
        b.setLendingCount(3L);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("equals: differs by lendingCount or readerView -> not equal")
    void equals_notEqual_when_fieldsDiffer() {
        var base = new ReaderCountView();
        base.setReaderView(newReaderView());
        base.setLendingCount(5L);

        var diffCount = new ReaderCountView();
        diffCount.setReaderView(newReaderView());
        diffCount.setLendingCount(6L);

        var diffReader = new ReaderCountView();
        diffReader.setReaderView(null);  // different from non-null
        diffReader.setLendingCount(5L);

        assertNotEquals(base, diffCount);
        assertNotEquals(base, diffReader);
    }

    @Test
    @DisplayName("equals: reflexive, null, different-class checks")
    void equals_reflexive_and_null_and_diffClass() {
        var dto = new ReaderCountView();
        dto.setReaderView(newReaderView());
        dto.setLendingCount(1L);

        assertEquals(dto, dto);          // reflexive
        assertNotEquals(dto, null);      // null
        assertNotEquals(dto, "string");  // different type
    }

    @Test
    @DisplayName("toString is safe and includes fields")
    void toString_includesFields() {
        var dto = new ReaderCountView();
        dto.setReaderView(newReaderView());
        dto.setLendingCount(42L);

        String s = dto.toString();
        assertNotNull(s);
        assertTrue(s.contains("lendingCount=42"));
        // readerView toString may vary; just ensure it doesn't crash.
    }

    @AfterAll
    static void tearDown() {
        validator = null;
    }
}
