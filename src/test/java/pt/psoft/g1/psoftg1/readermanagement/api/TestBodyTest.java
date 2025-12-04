package pt.psoft.g1.psoftg1.readermanagement.api;

import jakarta.annotation.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.multipart.MultipartFile;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SUT: TestBody (DTO with Lombok @Data, nullable fields)
 * Tipo: Unitário, isolado (sem Spring context)
 *
 * Verifica:
 *  - Construtor sem args deixa campos a null
 *  - Getters/Setters de Lombok funcionam (inclui MultipartFile)
 *  - equals/hashCode coerentes (objetos com os mesmos valores são iguais)
 *  - toString inclui campos relevantes
 *  - Referência da lista não é defensive-copied (aliasing)
 *  - @Nullable presente nos três campos
 */
@DisplayName("TestBody – Lombok DTO behavior & annotations")
class TestBodyTest {

    @Test
    @DisplayName("no-args constructor starts with all fields null")
    void noArgsConstructor_initializesNulls() {
        var dto = new TestBody();
        assertNull(dto.getFullName());
        assertNull(dto.getPhoto());
        assertNull(dto.getAges());
    }

    @Test
    @DisplayName("setters/getters work (String, MultipartFile, List<Long>)")
    void settersAndGetters_work() {
        var dto = new TestBody();

        MultipartFile mockPhoto = Mockito.mock(MultipartFile.class);
        List<Long> ages = new ArrayList<>(List.of(21L, 34L));

        dto.setFullName("Alice Example");
        dto.setPhoto(mockPhoto);
        dto.setAges(ages);

        assertEquals("Alice Example", dto.getFullName());
        assertSame(mockPhoto, dto.getPhoto());
        assertEquals(ages, dto.getAges());
    }

    @Test
    @DisplayName("equals/hashCode true for identical values")
    void equalsHashCode_sameValues_true() {
        MultipartFile p = Mockito.mock(MultipartFile.class);
        List<Long> list = new ArrayList<>(List.of(1L, 2L));

        var a = new TestBody();
        a.setFullName("Bob");
        a.setPhoto(p);
        a.setAges(list);

        var b = new TestBody();
        b.setFullName("Bob");
        b.setPhoto(p);
        b.setAges(new ArrayList<>(List.copyOf(list))); // same contents

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("equals false when any field differs")
    void equals_falseOnDifferences() {
        var base = new TestBody();
        base.setFullName("C");
        base.setPhoto(Mockito.mock(MultipartFile.class));
        base.setAges(List.of(10L));

        var diffName = new TestBody();
        diffName.setFullName("D");
        diffName.setPhoto(base.getPhoto());
        diffName.setAges(base.getAges());

        var diffPhoto = new TestBody();
        diffPhoto.setFullName("C");
        diffPhoto.setPhoto(Mockito.mock(MultipartFile.class)); // different instance
        diffPhoto.setAges(base.getAges());

        var diffAges = new TestBody();
        diffAges.setFullName("C");
        diffAges.setPhoto(base.getPhoto());
        diffAges.setAges(List.of(11L));

        assertNotEquals(base, diffName);
        assertNotEquals(base, diffPhoto);
        assertNotEquals(base, diffAges);
    }

    @Test
    @DisplayName("toString contains field names")
    void toString_containsFields() {
        var dto = new TestBody();
        dto.setFullName("Z");
        dto.setAges(List.of(5L));

        String s = dto.toString();
        assertTrue(s.contains("fullName"));
        assertTrue(s.contains("ages"));
        assertTrue(s.contains("Z"));
        assertTrue(s.contains("5"));
    }

    @Test
    @DisplayName("list reference is not defensively copied (aliasing)")
    void list_isAliased() {
        var dto = new TestBody();
        List<Long> ages = new ArrayList<>();
        dto.setAges(ages);

        ages.add(99L); // mutate external list
        assertEquals(1, dto.getAges().size());
        assertSame(ages, dto.getAges());
    }

    @Test
    @DisplayName("@Nullable present on all three fields")
    void nullableAnnotations_present() throws Exception {
        assertHasNullable(TestBody.class.getDeclaredField("fullName"));
        assertHasNullable(TestBody.class.getDeclaredField("photo"));
        assertHasNullable(TestBody.class.getDeclaredField("ages"));
    }

    private static void assertHasNullable(Field f) {
        Annotation ann = f.getAnnotation(Nullable.class);
        assertNotNull(ann, () -> f.getName() + " should be annotated with @Nullable");
    }
}
