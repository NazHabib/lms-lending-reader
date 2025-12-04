package pt.psoft.g1.psoftg1.lendingmanagement.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * SUT: LendingLinksView (DTO)
 * Tipo: Unitário e isolado.
 *
 * Comportamento esperado pelos testes:
 *  - Getters/Setters simples para {@code self}, {@code book}, {@code reader}.
 *  - Campos podem ser {@code null} (sem validação).
 *  - Alias de mapas (sem cópia defensiva): mutações externas são visíveis.
 *  - equals/hashCode baseados nos três campos (conteúdo dos mapas).
 *  - toString contém os nomes das chaves e representa os mapas.
 */

@DisplayName("LendingLinksView – Lombok DTO behavior")
class LendingLinksViewTest {

    @Test
    @DisplayName("getters/setters from Lombok @Data work")
    void gettersSetters_work() {
        LendingLinksView v = new LendingLinksView();

        Map<String, String> self = new HashMap<>();
        self.put("href", "/api/lendings/2025/7");

        Map<String, String> book = new HashMap<>();
        book.put("href", "/api/books/9782826012092");

        Map<String, String> reader = new HashMap<>();
        reader.put("href", "/api/readers/2025/1");

        v.setSelf(self);
        v.setBook(book);
        v.setReader(reader);

        assertSame(self, v.getSelf());
        assertSame(book, v.getBook());
        assertSame(reader, v.getReader());
        assertEquals("/api/books/9782826012092", v.getBook().get("href"));
    }

    @Test
    @DisplayName("equals/hashCode: objects with same map contents are equal")
    void equalsHashCode_sameContents_true() {
        Map<String, String> self1   = new LinkedHashMap<>();
        Map<String, String> book1   = new LinkedHashMap<>();
        Map<String, String> reader1 = new LinkedHashMap<>();
        self1.put("href", "/l/1"); book1.put("href", "/b/1"); reader1.put("href", "/r/1");

        Map<String, String> self2   = new LinkedHashMap<>();
        Map<String, String> book2   = new LinkedHashMap<>();
        Map<String, String> reader2 = new LinkedHashMap<>();
        self2.put("href", "/l/1"); book2.put("href", "/b/1"); reader2.put("href", "/r/1");

        LendingLinksView a = new LendingLinksView();
        a.setSelf(self1); a.setBook(book1); a.setReader(reader1);

        LendingLinksView b = new LendingLinksView();
        b.setSelf(self2); b.setBook(book2); b.setReader(reader2);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("equals: objects with different maps are not equal")
    void equals_different_false() {
        Map<String, String> self = Map.of("href", "/l/1");
        Map<String, String> book = Map.of("href", "/b/1");

        LendingLinksView a = new LendingLinksView();
        a.setSelf(self); a.setBook(book); a.setReader(Map.of("href", "/r/1"));

        LendingLinksView b = new LendingLinksView();
        b.setSelf(self); b.setBook(book); b.setReader(Map.of("href", "/r/2"));

        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("toString contains key hints (useful for logging)")
    void toString_containsHints() {
        LendingLinksView v = new LendingLinksView();
        v.setSelf(Map.of("href", "/api/lendings/2025/7"));
        v.setBook(Map.of("href", "/api/books/9782826012092"));
        v.setReader(Map.of("href", "/api/readers/2025/1"));

        String s = v.toString();
        assertTrue(s.contains("self"));
        assertTrue(s.contains("book"));
        assertTrue(s.contains("reader"));
        assertTrue(s.contains("2025/7"));
    }

    @Test
    @DisplayName("nulls are allowed for fields (no validation on DTO)")
    void nullsAllowed() {
        LendingLinksView v = new LendingLinksView();
        assertNull(v.getSelf());
        assertNull(v.getBook());
        assertNull(v.getReader());

        v.setSelf(null);
        v.setBook(null);
        v.setReader(null);
        assertNull(v.getSelf());
        assertNull(v.getBook());
        assertNull(v.getReader());
    }

    @Test
    @DisplayName("maps are aliased (no defensive copy) – external mutation is visible")
    void aliasing_externalMutationVisible() {
        Map<String, String> self = new HashMap<>();
        LendingLinksView v = new LendingLinksView();
        v.setSelf(self);

        self.put("href", "/x");
        assertEquals("/x", v.getSelf().get("href"));
    }

    @Test
    @DisplayName("can accept Mockito mocks as maps (type flexibility)")
    void acceptsMockitoMock() {
        @SuppressWarnings("unchecked")
        Map<String, String> mockMap = mock(Map.class);
        Mockito.when(mockMap.get("href")).thenReturn("/mocked");

        LendingLinksView v = new LendingLinksView();
        v.setReader(mockMap);

        assertSame(mockMap, v.getReader());
        assertEquals("/mocked", v.getReader().get("href"));
        Mockito.verify(mockMap).get("href");
    }
}
