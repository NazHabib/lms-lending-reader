package pt.psoft.g1.psoftg1.readermanagement.api;

import io.swagger.v3.oas.annotations.media.Schema;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SUT: ReaderView (DTO)
 * Tipo: teste unitário puro (sem Spring/DB).
 *
 * Validamos:
 *  - Getters/Setters gerados por Lombok (@Data)
 *  - equals/hashCode (mesmos campos -> igual; campo diferente -> diferente)
 *  - toString inclui campos úteis
 *  - List<String> interestList é a mesma referência (sem cópia defensiva)
 *  - Flags booleanos default (false)
 *  - Presença de @Schema na classe
 *  - Sanidade de equals: reflexivo, null, classe diferente
 */
@DisplayName("ReaderView – DTO behavior and Lombok semantics")
class ReaderViewTest {

    private static ReaderView sample() {
        ReaderView v = new ReaderView();
        v.setReaderNumber("2024/7");
        v.setEmail("alice@example.com");
        v.setFullName("Alice Doe");
        v.setBirthDate("2000-01-01");
        v.setPhoneNumber("910000000");
        v.setPhoto("photos/alice.png");
        v.setGdprConsent(true);
        v.setMarketingConsent(false);
        v.setThirdPartySharingConsent(true);
        v.setInterestList(new ArrayList<>(List.of("Fantasy", "Sci-Fi")));
        return v;
    }

    @Test
    @DisplayName("getters/setters populam e devolvem os valores")
    void gettersSetters_work() {
        ReaderView v = new ReaderView();
        v.setReaderNumber("2025/1");
        v.setEmail("bob@example.com");
        v.setFullName("Bob Smith");
        v.setBirthDate("1999-12-31");
        v.setPhoneNumber("912345678");
        v.setPhoto("photos/bob.jpg");
        v.setGdprConsent(true);
        v.setMarketingConsent(true);
        v.setThirdPartySharingConsent(false);
        List<String> interests = new ArrayList<>(List.of("Drama"));
        v.setInterestList(interests);

        assertAll(
                () -> assertEquals("2025/1", v.getReaderNumber()),
                () -> assertEquals("bob@example.com", v.getEmail()),
                () -> assertEquals("Bob Smith", v.getFullName()),
                () -> assertEquals("1999-12-31", v.getBirthDate()),
                () -> assertEquals("912345678", v.getPhoneNumber()),
                () -> assertEquals("photos/bob.jpg", v.getPhoto()),
                () -> assertTrue(v.isGdprConsent()),
                () -> assertTrue(v.isMarketingConsent()),
                () -> assertFalse(v.isThirdPartySharingConsent()),
                () -> assertEquals(List.of("Drama"), v.getInterestList())
        );
    }

    @Test
    @DisplayName("equals/hashCode: objetos com os mesmos campos são iguais")
    void equalsHash_sameData_true() {
        ReaderView a = sample();
        ReaderView b = sample();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("equals: difere quando um único campo difere")
    void equals_differentField_false() {
        ReaderView a = sample();
        ReaderView b = sample();
        b.setEmail("other@example.com");
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("toString inclui campos chave (readerNumber, email, fullName)")
    void toString_containsFields() {
        ReaderView v = sample();
        String s = v.toString();
        assertTrue(s.contains("2024/7"));
        assertTrue(s.contains("alice@example.com"));
        assertTrue(s.contains("Alice Doe"));
    }

    @Test
    @DisplayName("interestList mantém a mesma referência (sem cópia defensiva)")
    void interestList_aliasing() {
        List<String> shared = new ArrayList<>(List.of("Fantasy"));
        ReaderView v = new ReaderView();
        v.setInterestList(shared);
        shared.add("History");
        assertSame(shared, v.getInterestList());
        assertEquals(2, v.getInterestList().size());
    }

    @Test
    @DisplayName("booleans default são false")
    void booleanDefaults_false() {
        ReaderView v = new ReaderView();
        assertFalse(v.isGdprConsent());
        assertFalse(v.isMarketingConsent());
        assertFalse(v.isThirdPartySharingConsent());
    }

    @Test
    @DisplayName("@Schema está presente na classe")
    void schemaAnnotation_present() {
        Annotation ann = ReaderView.class.getAnnotation(Schema.class);
        assertNotNull(ann, "@Schema deve estar presente");
    }

    @Test
    @DisplayName("equals sanidade: reflexivo, contra null e classe diferente")
    void equals_reflexive_null_diffClass() {
        ReaderView v = sample();
        assertEquals(v, v);
        assertNotEquals(v, null);
        assertNotEquals(v, "not-a-reader-view");
    }
}
