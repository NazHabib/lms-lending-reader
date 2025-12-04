package pt.psoft.g1.psoftg1.readermanagement.services;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * SUT: ReaderAverageDto (DTO)
 * Tipo: Unitário isolado (sem dependências externas)
 *
 * O que cobrimos:
 *  - Bean Validation: @NotNull em readerView (que é do tipo ReaderDetails).
 *  - Lombok @Data: getters/setters/equals/hashCode/toString.
 *  - Presença de anotações (@Schema na classe; @NotNull no campo).
 *  - Integração com Mockito para simular ReaderDetails.
 */
class ReaderAverageDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
        validator = vf.getValidator();
    }

    // -----------------------------
    // Helpers
    // -----------------------------
    private static ReaderAverageDto validDto() {
        ReaderAverageDto dto = new ReaderAverageDto();
        dto.setReaderView(mock(ReaderDetails.class)); // mockito
        dto.setLendingCount(7L);
        return dto;
    }

    // -----------------------------
    // Bean Validation
    // -----------------------------
    @Test
    void validDto_hasNoViolations() {
        var dto = validDto();
        assertTrue(validator.validate(dto).isEmpty(), "DTO válido não deve violar constraints");
    }

    @Test
    void readerView_null_violatesNotNull() {
        var dto = validDto();
        dto.setReaderView(null);
        Set<?> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "readerView @NotNull deve ser obrigatório");
    }

    // -----------------------------
    // Lombok @Data behavior
    // -----------------------------
    @Test
    void gettersSetters_work() {
        var dto = new ReaderAverageDto();
        ReaderDetails rd = mock(ReaderDetails.class);
        dto.setReaderView(rd);
        dto.setLendingCount(42L);

        assertSame(rd, dto.getReaderView());
        assertEquals(42L, dto.getLendingCount());
    }

    @Test
    void equalsHashCode_sameData_areEqual() {
        ReaderDetails rd = mock(ReaderDetails.class);

        var a = new ReaderAverageDto();
        a.setReaderView(rd);
        a.setLendingCount(5L);

        var b = new ReaderAverageDto();
        b.setReaderView(rd);
        b.setLendingCount(5L);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equals_differentReaderView_notEqual() {
        var a = validDto();
        var b = validDto();
        // trocar o mock para forçar desigualdade
        b.setReaderView(mock(ReaderDetails.class));

        assertNotEquals(a, b);
    }

    @Test
    void toString_containsFieldNames() {
        var dto = validDto();
        String s = dto.toString();
        assertTrue(s.contains("readerView"));
        assertTrue(s.contains("lendingCount"));
    }

    // -----------------------------
    // Annotation presence
    // -----------------------------
    @Test
    void classHasSchemaAnnotation() {
        Annotation ann = ReaderAverageDto.class.getAnnotation(Schema.class);
        assertNotNull(ann, "@Schema deve estar presente na classe");
    }

    @Test
    void readerView_hasNotNullAnnotation() throws Exception {
        Field f = ReaderAverageDto.class.getDeclaredField("readerView");
        assertNotNull(f.getAnnotation(NotNull.class), "readerView deve ter @NotNull");
    }

    // -----------------------------
    // Reflexive/null/different-class branches for equals
    // -----------------------------
    @Test
    void equals_reflexive_true_and_null_false_and_diffClass_false() {
        var dto = validDto();
        assertTrue(dto.equals(dto));
        assertFalse(dto.equals(null));
        assertFalse(dto.equals("not-a-dto"));
    }
}
