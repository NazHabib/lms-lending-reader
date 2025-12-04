package pt.psoft.g1.psoftg1.readermanagement.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * SUT: ReaderBookCountDTO (DTO)
 * Tipo: Unitário isolado (sem dependências externas)
 *
 * O que cobrimos:
 *  - Construtores Lombok: no-args e all-args.
 *  - Getters/Setters gerados por @Data/@Getter.
 *  - equals/hashCode: objetos iguais, ramos reflexivo/null/diff-class,
 *    e casos com readerDetails diferentes e/ou lendingCount diferente.
 *  - toString contém nomes/campos relevantes.
 *
 * Isolamento:
 *  - ReaderDetails é simulado via Mockito (sem dependências reais).
 */
@DisplayName("ReaderBookCountDTO – Lombok & equality behavior")
class ReaderBookCountDTOTest {

    @Test
    @DisplayName("all-args constructor define campos corretamente")
    void allArgsConstructor_setsFields() {
        ReaderDetails rd = mock(ReaderDetails.class);
        ReaderBookCountDTO dto = new ReaderBookCountDTO(rd, 7L);

        assertSame(rd, dto.getReaderDetails());
        assertEquals(7L, dto.getLendingCount());
    }

    @Test
    @DisplayName("no-args + setters funcionam")
    void noArgsConstructor_and_setters_work() {
        ReaderBookCountDTO dto = new ReaderBookCountDTO();

        ReaderDetails rd = mock(ReaderDetails.class);
        dto.setReaderDetails(rd);
        dto.setLendingCount(42L);

        assertSame(rd, dto.getReaderDetails());
        assertEquals(42L, dto.getLendingCount());
    }

    @Test
    @DisplayName("equals/hashCode – objetos com mesmos campos são iguais")
    void equalsHashCode_sameData_areEqual() {
        ReaderDetails rd = mock(ReaderDetails.class);

        ReaderBookCountDTO a = new ReaderBookCountDTO(rd, 10L);
        ReaderBookCountDTO b = new ReaderBookCountDTO(rd, 10L);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("equals – readerDetails diferentes tornam objetos diferentes")
    void equals_differentReaderDetails_notEqual() {
        ReaderBookCountDTO a = new ReaderBookCountDTO(mock(ReaderDetails.class), 10L);
        ReaderBookCountDTO b = new ReaderBookCountDTO(mock(ReaderDetails.class), 10L);

        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("equals – lendingCount diferente torna objetos diferentes")
    void equals_differentCount_notEqual() {
        ReaderDetails rd = mock(ReaderDetails.class);

        ReaderBookCountDTO a = new ReaderBookCountDTO(rd, 1L);
        ReaderBookCountDTO b = new ReaderBookCountDTO(rd, 2L);

        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("equals – reflexivo, null e classe diferente")
    void equals_reflexive_null_diffClass() {
        ReaderBookCountDTO a = new ReaderBookCountDTO(mock(ReaderDetails.class), 3L);

        assertTrue(a.equals(a), "reflexivo");
        assertFalse(a.equals(null), "null");
        assertFalse(a.equals("not-a-dto"), "classe diferente");
    }

    @Test
    @DisplayName("toString contém nomes de campos")
    void toString_containsFieldNames() {
        ReaderBookCountDTO a = new ReaderBookCountDTO(mock(ReaderDetails.class), 5L);
        String s = a.toString();

        assertTrue(s.contains("readerDetails"));
        assertTrue(s.contains("lendingCount"));
        assertTrue(s.contains("5"));
    }
}
