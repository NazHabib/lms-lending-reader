package pt.psoft.g1.psoftg1.lendingmanagement.services;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SUT: CreateLendingRequest (DTO)
 * Tipo: Unitário isolado (sem Spring/JPA)
 *
 * Valida:
 *  - Lombok: all-args / no-args, getters/setters, equals/hashCode/toString, canEqual
 *  - Bean Validation: @NotNull/@NotBlank/@Size nas propriedades
 *  - Cobertura de ramos do equals: self, null, classe diferente,
 *    nulidade em ambos os sentidos por campo, igualdade total e desigualdades específicas.
 */
class CreateLendingRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
        validator = vf.getValidator();
    }

    // -------------------- Lombok basics --------------------

    @Test
    void allArgsConstructor_setsFields() {
        var dto = new CreateLendingRequest("9782826012092", "123456");
        assertEquals("9782826012092", dto.getIsbn());
        assertEquals("123456", dto.getReaderNumber());
        assertTrue(dto.toString().contains("9782826012092"));
        assertTrue(dto.toString().contains("123456"));
    }

    @Test
    void noArgsConstructor_allowsSetters() {
        var dto = new CreateLendingRequest();
        dto.setIsbn("9780000000002");
        dto.setReaderNumber("ABC123");
        assertEquals("9780000000002", dto.getIsbn());
        assertEquals("ABC123", dto.getReaderNumber());
    }

    // -------------------- equals / hashCode branches --------------------

    private static CreateLendingRequest q(String isbn, String reader) {
        return new CreateLendingRequest(isbn, reader);
    }

    @Test void equals_reflexive_true() {
        var a = q("9782826012092", "111111");
        assertTrue(a.equals(a));
    }

    @Test void equals_null_false() {
        var a = q("9782826012092", "111111");
        assertFalse(a.equals(null));
    }

    @Test void equals_differentClass_false() {
        var a = q("9782826012092", "111111");
        assertFalse(a.equals("not-a-dto"));
    }

    @Test void equals_sameFields_true_and_hashCodeEqual() {
        var a = q("9782826012092", "111111");
        var b = q("9782826012092", "111111");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    // isbn null/non-null branches (both orders) + both-null + different value
    @Test void equals_isbnNull_vs_nonNull_false() {
        var a = q(null, "111111");
        var b = q("9782826012092", "111111");
        assertNotEquals(a, b);
    }

    @Test void equals_isbnNonNull_vs_null_false() {
        var a = q("9782826012092", "111111");
        var b = q(null, "111111");
        assertNotEquals(a, b);
    }

    @Test void equals_isbnBothNull_true_ifOtherFieldEqual() {
        var a = q(null, "111111");
        var b = q(null, "111111");
        assertEquals(a, b);
    }

    @Test void equals_isbnDifferent_false() {
        var a = q("9782826012092", "111111");
        var b = q("9780000000002", "111111");
        assertNotEquals(a, b);
    }

    // readerNumber null/non-null branches (both orders) + both-null + different value
    @Test void equals_readerNull_vs_nonNull_false() {
        var a = q("9782826012092", null);
        var b = q("9782826012092", "111111");
        assertNotEquals(a, b);
    }

    @Test void equals_readerNonNull_vs_null_false() {
        var a = q("9782826012092", "111111");
        var b = q("9782826012092", null);
        assertNotEquals(a, b);
    }

    @Test void equals_readerBothNull_true_ifOtherFieldEqual() {
        var a = q("9782826012092", null);
        var b = q("9782826012092", null);
        assertEquals(a, b);
    }

    @Test void equals_readerDifferent_false() {
        var a = q("9782826012092", "111111");
        var b = q("9782826012092", "222222");
        assertNotEquals(a, b);
    }

    // canEqual path (Lombok)
    static class SubCreateLendingRequest extends CreateLendingRequest {
        SubCreateLendingRequest(String i, String r) { super(i, r); }
    }

    @Test
    void equals_canEqual_true_withSubclassSameValues() {
        var base = q("9782826012092", "111111");
        var sub  = new SubCreateLendingRequest("9782826012092", "111111");
        assertTrue(base.canEqual(sub));
        assertEquals(base, sub);
        assertEquals(base.hashCode(), sub.hashCode());
    }

    // -------------------- Bean Validation: annotation presence --------------------

    @Test
    void isbn_hasExpectedAnnotations() throws Exception {
        Field f = CreateLendingRequest.class.getDeclaredField("isbn");
        assertNotNull(f.getAnnotation(NotNull.class));
        assertNotNull(f.getAnnotation(NotBlank.class));
        Size s = f.getAnnotation(Size.class);
        assertNotNull(s);
        assertEquals(10, s.min());
        assertEquals(13, s.max());
    }

    @Test
    void readerNumber_hasExpectedAnnotations() throws Exception {
        Field f = CreateLendingRequest.class.getDeclaredField("readerNumber");
        assertNotNull(f.getAnnotation(NotNull.class));
        assertNotNull(f.getAnnotation(NotBlank.class));
        Size s = f.getAnnotation(Size.class);
        assertNotNull(s);
        assertEquals(6, s.min());
        assertEquals(16, s.max());
    }

    // -------------------- Bean Validation: behavior --------------------

    @Test
    void validation_acceptsValidLengths() {
        // ISBN length 10 and 13, readerNumber length 6 and 16
        assertTrue(validator.validate(new CreateLendingRequest("0123456789", "ABC123")).isEmpty());
        assertTrue(validator.validate(new CreateLendingRequest("9782826012092", "1234567890123456")).isEmpty());
    }

    @Test
    void validation_rejectsIsbnTooShortOrTooLong() {
        assertFalse(validator.validate(new CreateLendingRequest("012345678", "ABC123")).isEmpty());   // 9
        assertFalse(validator.validate(new CreateLendingRequest("01234567890123", "ABC123")).isEmpty()); // 14
    }

    @Test
    void validation_rejectsReaderNumberTooShortOrTooLong() {
        assertFalse(validator.validate(new CreateLendingRequest("0123456789", "12345")).isEmpty());       // 5
        assertFalse(validator.validate(new CreateLendingRequest("0123456789", "12345678901234567")).isEmpty()); // 17
    }

    @Test
    void validation_rejectsNullsAndBlanks() {
        assertFalse(validator.validate(new CreateLendingRequest(null, "123456")).isEmpty());
        assertFalse(validator.validate(new CreateLendingRequest("0123456789", null)).isEmpty());

        assertFalse(validator.validate(new CreateLendingRequest("", "123456")).isEmpty());
        assertFalse(validator.validate(new CreateLendingRequest("0123456789", "")).isEmpty());

        assertFalse(validator.validate(new CreateLendingRequest("   ", "   ")).isEmpty());
    }
}
