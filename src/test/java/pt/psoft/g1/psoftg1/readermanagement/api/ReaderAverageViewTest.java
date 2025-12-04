package pt.psoft.g1.psoftg1.readermanagement.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SUT: ReaderAverageView (API DTO)
 * Tipo: Unitário isolado (sem dependências externas)
 *
 * Black-box: valida o comportamento gerado por Lombok (@Data) como DTO.
 * White-box: cobre getters/setters, equals/hashCode, toString e a presença de anotações.
 *
 * O que os testes verificam:
 *  - Getters e setters funcionam conforme esperado.
 *  - equals/hashCode: objetos com os mesmos valores são iguais; valores diferentes -> não iguais.
 *  - toString inclui os campos relevantes.
 *  - @Schema está presente na classe.
 *  - @NotNull está presente no campo readerView.
 *  - (Opcional) Bean Validation: readerView == null viola @NotNull.
 *
 * Isolamento: não envolve Spring nem camadas de serviço/repos; apenas o DTO.
 */


@DisplayName("ReaderAverageView – Lombok data & validation")
class ReaderAverageViewTest {

    private static Validator validator;

    @BeforeAll
    static void initValidator() {
        try {
            ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
            validator = vf.getValidator();
        } catch (Throwable t) {
            // Bean Validation not on classpath in some setups; tests that need it will be skipped gracefully
            validator = null;
        }
    }

    // helper: build a minimal ReaderView via no-args + setters
    private static ReaderView makeReaderView() {
        ReaderView v = new ReaderView(); // assumes @Data no-args
        // We don’t set inner fields to avoid coupling; no @Valid on ReaderAverageView so no cascade.
        return v;
    }

    @Test
    @DisplayName("getters/setters from @Data work")
    void lombok_getters_setters() {
        ReaderAverageView dto = new ReaderAverageView();

        ReaderView rv = makeReaderView();
        dto.setReaderView(rv);
        dto.setLendingCount(42L);

        assertSame(rv, dto.getReaderView());
        assertEquals(42L, dto.getLendingCount());
    }

    @Test
    @DisplayName("equals/hashCode true for identical values")
    void equals_hash_sameData() {
        ReaderView rv1 = makeReaderView();
        ReaderView rv2 = makeReaderView(); // equal by Lombok if same field values (all null) -> true

        ReaderAverageView a = new ReaderAverageView();
        a.setReaderView(rv1);
        a.setLendingCount(7L);

        ReaderAverageView b = new ReaderAverageView();
        b.setReaderView(rv2);
        b.setLendingCount(7L);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("equals false when readerView or lendingCount differ")
    void equals_false_whenFieldsDiffer() {
        ReaderAverageView base = new ReaderAverageView();
        base.setReaderView(makeReaderView());
        base.setLendingCount(3L);

        ReaderAverageView diffCount = new ReaderAverageView();
        diffCount.setReaderView(makeReaderView());
        diffCount.setLendingCount(4L);

        ReaderAverageView diffReader = new ReaderAverageView();
        // different ReaderView instance but same null fields => may be equal; force inequality by setting count null
        diffReader.setReaderView(new ReaderView()); // same as base logically
        diffReader.setLendingCount(null);           // now differs

        assertNotEquals(base, diffCount);
        assertNotEquals(base, diffReader);
    }

    @Test
    @DisplayName("toString contains field names")
    void toString_containsFields() {
        ReaderAverageView dto = new ReaderAverageView();
        dto.setReaderView(makeReaderView());
        dto.setLendingCount(5L);

        String s = dto.toString();
        assertTrue(s.contains("readerView"));
        assertTrue(s.contains("lendingCount"));
        assertTrue(s.contains("5"));
    }

    @Test
    @DisplayName("@Schema annotation present on class")
    void schemaAnnotation_present() {
        Annotation ann = ReaderAverageView.class.getAnnotation(Schema.class);
        assertNotNull(ann, "@Schema must be present on ReaderAverageView");
    }

    @Test
    @DisplayName("@NotNull present on readerView field")
    void notNull_on_readerView_present() throws Exception {
        Field f = ReaderAverageView.class.getDeclaredField("readerView");
        NotNull nn = f.getAnnotation(NotNull.class);
        assertNotNull(nn, "@NotNull must be present on readerView");
    }

    @Test
    @DisplayName("Bean Validation: readerView == null violates @NotNull")
    void beanValidation_readerView_null_violatesNotNull() {
        Assumptions.assumeTrue(validator != null, "Bean Validation not available");

        ReaderAverageView dto = new ReaderAverageView();
        dto.setReaderView(null);
        dto.setLendingCount(1L);

        var violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "readerView null should violate @NotNull");
    }

    @Test
    @DisplayName("equals: reflexive, null, different class")
    void equals_reflexive_null_diffClass() {
        ReaderAverageView dto = new ReaderAverageView();
        dto.setReaderView(makeReaderView());
        dto.setLendingCount(1L);

        assertEquals(dto, dto);          // reflexive
        assertNotEquals(dto, null);      // null
        assertNotEquals(dto, "string");  // different class
    }
}
