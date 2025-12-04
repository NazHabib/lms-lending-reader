package pt.psoft.g1.psoftg1.lendingmanagement.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * SUT: FineView (DTO)
 * Tipo: Unitário isolado (Mockito somente para aninhado LendingView)
 *
 * O que validamos:
 *  - Bean Validation: @PositiveOrZero em centsValue e @NotNull em lending (violação/aceitação).
 *  - Presença das anotações nas fields via reflexão.
 *  - Lombok @Data: getters/setters, equals/hashCode, toString.
 *  - @Schema presente na classe.
 *  - Uso de Mockito para simular LendingView.
 */
class FineViewTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
        validator = vf.getValidator();
    }

    // ------------- helpers -------------

    private static FineView validFine() {
        FineView v = new FineView();
        v.setCentsValue(0);
        v.setLending(mock(LendingView.class));
        return v;
    }

    // ------------- Bean Validation behavior -------------

    @Test
    @DisplayName("centsValue >= 0 passa; negativo viola @PositiveOrZero")
    void centsValue_positiveOrZero_validates() {
        FineView ok = validFine();
        ok.setCentsValue(10);
        Set<?> violationsOk = validator.validate(ok);
        assertTrue(violationsOk.isEmpty(), "centsValue >= 0 deve ser válido");

        FineView zero = validFine();
        zero.setCentsValue(0);
        assertTrue(validator.validate(zero).isEmpty(), "zero também é válido");

        FineView neg = validFine();
        neg.setCentsValue(-1);
        assertFalse(validator.validate(neg).isEmpty(), "valor negativo deve violar @PositiveOrZero");
    }

    @Test
    @DisplayName("lending null viola @NotNull")
    void lending_notNull_validates() {
        FineView v = validFine();
        v.setLending(null);
        assertFalse(validator.validate(v).isEmpty(), "lending null deve violar @NotNull");
    }

    // ------------- Annotation presence (reflection) -------------

    @Test
    @DisplayName("fields têm anotações esperadas (@PositiveOrZero, @NotNull)")
    void annotationPresence_onFields() throws Exception {
        Field cents = FineView.class.getDeclaredField("centsValue");
        Annotation pos = cents.getAnnotation(PositiveOrZero.class);
        assertNotNull(pos, "centsValue deve possuir @PositiveOrZero");

        Field lending = FineView.class.getDeclaredField("lending");
        Annotation nn = lending.getAnnotation(NotNull.class);
        assertNotNull(nn, "lending deve possuir @NotNull");
    }

    @Test
    @DisplayName("@Schema presente na classe")
    void schemaAnnotation_onClass() {
        Schema ann = FineView.class.getAnnotation(Schema.class);
        assertNotNull(ann, "@Schema deve estar presente na classe");
        assertTrue(ann.description() != null && !ann.description().isBlank());
    }

    // ------------- Lombok @Data (getters/setters/equals/hashCode/toString) -------------

    @Test
    @DisplayName("getters e setters funcionam (inclui mock do LendingView)")
    void lombok_getters_setters() {
        LendingView lv = Mockito.mock(LendingView.class);

        FineView v = new FineView();
        v.setCentsValue(123);
        v.setLending(lv);

        assertEquals(123, v.getCentsValue());
        assertSame(lv, v.getLending());

        String s = v.toString();
        assertTrue(s.contains("centsValue=123"), "toString deve incluir centsValue");
    }

    @Test
    @DisplayName("equals/hashCode: objetos com mesmos dados são iguais")
    void equals_hashCode_sameData() {
        LendingView lv = mock(LendingView.class);

        FineView a = new FineView();
        a.setCentsValue(50);
        a.setLending(lv);

        FineView b = new FineView();
        b.setCentsValue(50);
        b.setLending(lv);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("equals: difere quando algum campo difere")
    void equals_differsOnFields() {
        LendingView lv = mock(LendingView.class);

        FineView a = new FineView();
        a.setCentsValue(10);
        a.setLending(lv);

        FineView b = new FineView();
        b.setCentsValue(11); // diferente
        b.setLending(lv);

        assertNotEquals(a, b);
    }

    @AfterAll
    static void tearDown() {
        validator = null;
    }
}
