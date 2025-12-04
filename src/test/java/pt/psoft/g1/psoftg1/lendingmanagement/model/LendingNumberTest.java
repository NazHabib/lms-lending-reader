package pt.psoft.g1.psoftg1.lendingmanagement.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.lang.reflect.Field;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SUT: LendingNumber (Value Object / @Embeddable)
 * Tipo: Teste unitário isolado (sem Spring/JPA)
 *
 * Black-box:
 *  - Regras de construção:
 *      * (year, sequential): year ∈ [1970..anoAtual], sequential ≥ 0
 *      * (sequential): usa ano atual automaticamente
 *      * (string): formato "YYYY/seq" com validação de índices e dígitos
 *  - Normalização do valor:
 *      * Remoção de zeros à esquerda no sequential via parsing numérico
 *      * toString devolve sempre "YYYY/seq" canónico
 *
 * White-box:
 *  - Verifica metadados de persistência/validação no campo embutido:
 *      * @Embeddable na classe
 *      * @Column(name="LENDING_NUMBER", length=32) + @NotNull + @NotBlank + @Size(min=6, max=32)
 *  - Exercita ramos de erro: formato incorreto, índices fora do intervalo, NumberFormatException
 *
 * Isolamento:
 *  - 100% em memória, sem BD nem contexto Spring
 */


class LendingNumberTest {

    // ------------------------
    // Existing tests (kept)
    // ------------------------

    @Test
    void ensureLendingNumberNotNull(){
        assertThrows(IllegalArgumentException.class, () -> new LendingNumber(null));
    }

    @Test
    void ensureLendingNumberNotBlank(){
        assertThrows(IllegalArgumentException.class, () -> new LendingNumber(""));
    }

    @Test
    void ensureLendingNumberNotWrongFormat(){
        assertThrows(IllegalArgumentException.class, () -> new LendingNumber("1/2024"));   // year must be 4 digits
        assertThrows(IllegalArgumentException.class, () -> new LendingNumber("24/1"));     // year too short
        assertThrows(IllegalArgumentException.class, () -> new LendingNumber("2024-1"));   // wrong separator
        assertThrows(IllegalArgumentException.class, () -> new LendingNumber("2024\\1"));  // wrong separator
    }

    @Test
    void ensureLendingNumberIsSetWithString() {
        final var ln = new LendingNumber("2024/1");
        assertEquals("2024/1", ln.toString());
    }

    @Test
    void ensureLendingNumberIsSetWithSequential() {
        final LendingNumber ln = new LendingNumber(1);
        assertNotNull(ln);
        assertEquals(LocalDate.now().getYear() + "/1", ln.toString());
    }

    @Test
    void ensureLendingNumberIsSetWithYearAndSequential() {
        final LendingNumber ln = new LendingNumber(2024,1);
        assertNotNull(ln);
    }

    @Test
    void ensureSequentialCannotBeNegative() {
        assertThrows(IllegalArgumentException.class, () -> new LendingNumber(2024,-1));
    }

    @Test
    void ensureYearCannotBeInTheFuture() {
        assertThrows(IllegalArgumentException.class, () -> new LendingNumber(LocalDate.now().getYear()+1,1));
    }

    // ------------------------
    // New black-box tests
    // ------------------------

    @Test
    @DisplayName("year at lower bound (1970) is accepted")
    void yearLowerBoundAccepted() {
        var ln = new LendingNumber(1970, 0);
        assertEquals("1970/0", ln.toString());
    }

    @Test
    @DisplayName("year below lower bound (1969) is rejected")
    void yearBelowLowerBoundRejected() {
        assertThrows(IllegalArgumentException.class, () -> new LendingNumber(1969, 0));
    }

    @Test
    @DisplayName("year at upper bound (current year) is accepted")
    void yearUpperBoundAccepted() {
        int y = LocalDate.now().getYear();
        var ln = new LendingNumber(y, 123);
        assertEquals(y + "/123", ln.toString());
    }

    @Test
    @DisplayName("sequential zero is accepted")
    void sequentialZeroAccepted() {
        var ln = new LendingNumber(2024, 0);
        assertEquals("2024/0", ln.toString());
    }

    @Test
    @DisplayName("string constructor normalizes leading zeros in sequential (2024/0007 -> 2024/7)")
    void stringConstructorNormalizesLeadingZeros() {
        var ln = new LendingNumber("2024/0007");
        assertEquals("2024/7", ln.toString());
    }

    @Test
    @DisplayName("string constructor rejects non-digits in either part")
    void stringConstructorRejectsNonDigits() {
        assertThrows(IllegalArgumentException.class, () -> new LendingNumber("20a4/1"));
        assertThrows(IllegalArgumentException.class, () -> new LendingNumber("2024/1a"));
        assertThrows(IllegalArgumentException.class, () -> new LendingNumber("abcd/efg"));
    }

    @Test
    @DisplayName("string constructor rejects missing or misplaced slash")
    void stringConstructorRejectsSlashIssues() {
        assertThrows(IllegalArgumentException.class, () -> new LendingNumber("20241"));    // no slash
        assertThrows(IllegalArgumentException.class, () -> new LendingNumber("2024//1"));  // extra slash
        assertThrows(IllegalArgumentException.class, () -> new LendingNumber("2024/"));    // no seq part
        assertThrows(IllegalArgumentException.class, () -> new LendingNumber("/1"));       // no year part
    }

    @Test
    @DisplayName("string constructor rejects trailing/leading spaces")
    void stringConstructorRejectsSpaces() {
        assertThrows(IllegalArgumentException.class, () -> new LendingNumber(" 2024/1"));
        assertThrows(IllegalArgumentException.class, () -> new LendingNumber("2024/1 "));
        assertThrows(IllegalArgumentException.class, () -> new LendingNumber(" 2024 / 1 "));
    }

    @Test
    @DisplayName("string constructor rejects very long sequential (overflow -> NumberFormatException wrapped)")
    void stringConstructorRejectsOverflowSequential() {
        // deliberately huge sequential to cause Integer.parseInt overflow
        assertThrows(IllegalArgumentException.class, () -> new LendingNumber("2024/9999999999999999999999"));
    }

    @Test
    @DisplayName("toString is canonical for constructors (year,seq) and (seq only)")
    void toStringCanonical() {
        int currentYear = LocalDate.now().getYear();
        assertEquals(currentYear + "/42", new LendingNumber(42).toString());
        assertEquals("2024/7", new LendingNumber(2024, 7).toString());
    }

    // ------------------------
    // White-box: annotations
    // ------------------------

    @Test
    @DisplayName("class is @Embeddable")
    void classIsEmbeddable() {
        assertNotNull(LendingNumber.class.getAnnotation(Embeddable.class),
                "LendingNumber must be @Embeddable");
    }

    @Test
    @DisplayName("field lendingNumber has expected JPA/Validation annotations")
    void fieldHasJpaAndValidationAnnotations() throws Exception {
        Field f = LendingNumber.class.getDeclaredField("lendingNumber");

        Column col = f.getAnnotation(Column.class);
        assertNotNull(col, "@Column must be present");
        assertEquals("LENDING_NUMBER", col.name());
        assertEquals(32, col.length());

        assertNotNull(f.getAnnotation(NotNull.class), "@NotNull must be present");
        assertNotNull(f.getAnnotation(NotBlank.class), "@NotBlank must be present");

        Size size = f.getAnnotation(Size.class);
        assertNotNull(size, "@Size must be present");
        assertEquals(6, size.min());
        assertEquals(32, size.max());
    }
}
