package pt.psoft.g1.psoftg1.lendingmanagement.services;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;


/**

 SUT: SetLendingReturnedRequest (DTO)

 Tipo: Unitário isolado (sem Spring/JPA)

 Black-box:

 Comportamento Lombok @Data: getters/setters, construtores, equals/hashCode/toString.

 Regras de validação: @Size(max=1024) no campo commentary (nulo é permitido).

 White-box:

 Verifica presença e valor da anotação @Size via reflexão.

 Testa fronteiras do tamanho (1024 ok, 1025 falha).
 */


class SetLendingReturnedRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
        validator = vf.getValidator();
    }

    // ----------------- Lombok behavior -----------------

    @Test
    void noArgsConstructor_plusSetterGetter() {
        var dto = new SetLendingReturnedRequest();
        dto.setCommentary("ok");
        assertEquals("ok", dto.getCommentary());
    }

    @Test
    void allArgsConstructor_setsField() {
        var dto = new SetLendingReturnedRequest("some text");
        assertEquals("some text", dto.getCommentary());
    }

    @Test
    void equals_hashCode_sameValues_true() {
        var a = new SetLendingReturnedRequest("x");
        var b = new SetLendingReturnedRequest("x");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equals_reflexive_null_differentClass() {
        var a = new SetLendingReturnedRequest("x");
        assertEquals(a, a);
        assertNotEquals(a, null);
        assertNotEquals(a, "not-a-dto");
    }

    @Test
    void toString_includesFieldName() {
        var dto = new SetLendingReturnedRequest("hello");
        String s = dto.toString();
        assertTrue(s.contains("commentary"));
        assertTrue(s.contains("hello"));
    }

    // ----------------- Validation / annotation -----------------

    @Test
    void annotation_sizeMax1024_present() throws Exception {
        var f = SetLendingReturnedRequest.class.getDeclaredField("commentary");
        var size = f.getAnnotation(Size.class);
        assertNotNull(size, "@Size must be present");
        assertEquals(1024, size.max());
    }

    @Test
    void nullCommentary_isValid() {
        var dto = new SetLendingReturnedRequest(null);
        assertTrue(validator.validate(dto).isEmpty(),
                "null commentary is allowed");
    }

    @Test
    void emptyCommentary_isValid() {
        var dto = new SetLendingReturnedRequest("");
        assertTrue(validator.validate(dto).isEmpty(),
                "empty string within max size should be valid");
    }

    @Test
    void commentary_exactly1024_isValid() {
        var dto = new SetLendingReturnedRequest("a".repeat(1024));
        assertTrue(validator.validate(dto).isEmpty());
    }

    @Test
    void commentary_oversize1025_failsValidation() {
        var dto = new SetLendingReturnedRequest("a".repeat(1025));
        assertFalse(validator.validate(dto).isEmpty(),
                "1025 chars must violate @Size(max=1024)");
    }

    @AfterAll
    static void tearDownValidator() {
        validator = null;
    }
}
