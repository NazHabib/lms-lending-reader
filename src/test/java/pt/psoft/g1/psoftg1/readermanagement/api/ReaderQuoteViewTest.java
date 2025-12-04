package pt.psoft.g1.psoftg1.readermanagement.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SUT: ReaderQuoteView (DTO extending ReaderView)
 * Tipo: teste unitário puro (sem Spring/DB).
 *
 * O que validamos:
 *  - Herança: é um ReaderView
 *  - Lombok @Data/@Setter: getters/setters funcionam
 *  - @EqualsAndHashCode(callSuper = true): igualdade considera super + próprio campo
 *  - toString() inclui o campo quote
 *  - Sanidade de equals (reflexivo, null, classe diferente)
 */
@DisplayName("ReaderQuoteView – DTO behavior and Lombok semantics")
class ReaderQuoteViewTest {

    @Test
    @DisplayName("é subclasse de ReaderView")
    void isSubclassOfReaderView() {
        ReaderQuoteView v = new ReaderQuoteView();
        assertTrue(v instanceof ReaderView);
    }

    @Test
    @DisplayName("getter/setter para quote funcionam")
    void getterSetter_quote() {
        ReaderQuoteView v = new ReaderQuoteView();
        v.setQuote("Some inspirational quote");
        assertEquals("Some inspirational quote", v.getQuote());
    }

    @Test
    @DisplayName("equals/hashCode: duas instâncias default com mesma quote são iguais")
    void equals_hash_sameQuote_defaultSuper() {
        ReaderQuoteView a = new ReaderQuoteView();
        a.setQuote("Q");
        ReaderQuoteView b = new ReaderQuoteView();
        b.setQuote("Q");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("equals: quotes diferentes tornam objetos diferentes (com super padrão)")
    void equals_differentQuote_notEqual() {
        ReaderQuoteView a = new ReaderQuoteView();
        a.setQuote("Q1");
        ReaderQuoteView b = new ReaderQuoteView();
        b.setQuote("Q2");

        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("toString inclui o campo quote")
    void toString_containsQuote() {
        ReaderQuoteView v = new ReaderQuoteView();
        v.setQuote("Hello!");
        String s = v.toString();
        assertTrue(s.contains("Hello!"));
        assertTrue(s.toLowerCase().contains("quote"));
    }

    @Test
    @DisplayName("equals sanidade: reflexivo, contra null e classe diferente")
    void equals_reflexive_null_diffClass() {
        ReaderQuoteView v = new ReaderQuoteView();
        v.setQuote("X");
        assertEquals(v, v);               // reflexive
        assertNotEquals(v, null);         // null
        assertNotEquals(v, "not-a-view"); // different class
    }
}
