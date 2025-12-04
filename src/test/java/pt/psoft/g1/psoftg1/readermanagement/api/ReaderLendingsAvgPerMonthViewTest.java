package pt.psoft.g1.psoftg1.readermanagement.api;

import io.swagger.v3.oas.annotations.media.Schema;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SUT: ReaderLendingsAvgPerMonthView (DTO)
 * Tipo: teste unitário puro (sem Spring/DB).
 *
 * O que validamos (black-box):
 *  - Construtor all-args preenche corretamente (year, month, durationAverages)
 *  - Getters/Setters gerados por Lombok @Data
 *  - equals/hashCode consistentes para o mesmo conteúdo; desigual quando campos diferem
 *  - toString() é seguro e inclui campos relevantes
 *  - Aceita lista null e não faz defensive copy (aliasing)
 *  - Presença da anotação OpenAPI @Schema
 */
@DisplayName("ReaderLendingsAvgPerMonthView – DTO behavior and annotations")
class ReaderLendingsAvgPerMonthViewTest {

    // ----- helpers -----------------------------------------------------------

    /** Build a minimal ReaderAverageView with a (default) ReaderView and a count. */
    private static ReaderAverageView rav(long count) {
        ReaderAverageView v = new ReaderAverageView();
        v.setReaderView(new ReaderView()); // @NotNull field satisfied
        v.setLendingCount(count);
        return v;
    }

    // ----- tests -------------------------------------------------------------

    @Test
    @DisplayName("all-args constructor sets fields correctly")
    void allArgsConstructor_setsFields() {
        List<ReaderAverageView> vals = List.of(rav(2L));
        var view = new ReaderLendingsAvgPerMonthView(2024, 5, vals);

        assertEquals(2024, view.getYear());
        assertEquals(5, view.getMonth());
        assertEquals(vals, view.getDurationAverages());
    }

    @Test
    @DisplayName("Lombok @Data: getters & setters work")
    void lombok_getters_setters() {
        var view = new ReaderLendingsAvgPerMonthView(2023, 8, new ArrayList<>());

        view.setYear(2026);
        view.setMonth(12);

        List<ReaderAverageView> newList = new ArrayList<>();
        newList.add(rav(7L));
        view.setDurationAverages(newList);

        assertEquals(2026, view.getYear());
        assertEquals(12, view.getMonth());
        assertSame(newList, view.getDurationAverages());
        assertEquals(7L, view.getDurationAverages().get(0).getLendingCount());
    }

    @Test
    @DisplayName("equals/hashCode true for identical values (same element instance)")
    void equals_hash_sameData() {
        var shared = rav(3L); // use same instance to avoid reliance on ReaderView.equals
        var a = new ReaderLendingsAvgPerMonthView(2024, 9, List.of(shared));
        var b = new ReaderLendingsAvgPerMonthView(2024, 9, List.of(shared));

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("equals false when year, month, or list differ")
    void equals_false_whenFieldsDiffer() {
        var base   = new ReaderLendingsAvgPerMonthView(2024, 9, List.of(rav(1L)));
        var yDiff  = new ReaderLendingsAvgPerMonthView(2023, 9, List.of(rav(1L)));
        var mDiff  = new ReaderLendingsAvgPerMonthView(2024, 8, List.of(rav(1L)));
        var lDiff  = new ReaderLendingsAvgPerMonthView(2024, 9, List.of(rav(2L))); // different value

        assertNotEquals(base, yDiff);
        assertNotEquals(base, mDiff);
        assertNotEquals(base, lDiff);
    }

    @Test
    @DisplayName("toString contains year, month and some list info")
    void toString_containsFields() {
        var v = new ReaderLendingsAvgPerMonthView(2025, 2, List.of(rav(10L)));
        String s = v.toString();
        assertTrue(s.contains("2025"));
        assertTrue(s.contains("2"));
        assertTrue(s.contains("durationAverages"));
    }

    @Test
    @DisplayName("null list is allowed")
    void nullList_allowed() {
        var v = new ReaderLendingsAvgPerMonthView(2025, 6, null);
        assertNull(v.getDurationAverages());
    }

    @Test
    @DisplayName("list is not defensively copied (aliasing)")
    void list_notDefensivelyCopied() {
        List<ReaderAverageView> shared = new ArrayList<>();
        shared.add(rav(1L));

        var v = new ReaderLendingsAvgPerMonthView(2025, 3, shared);
        shared.add(rav(2L));

        assertSame(shared, v.getDurationAverages());
        assertEquals(2, v.getDurationAverages().size());
    }

    @Test
    @DisplayName("@Schema annotation present")
    void schemaAnnotation_present() {
        Annotation ann = ReaderLendingsAvgPerMonthView.class.getAnnotation(Schema.class);
        assertNotNull(ann, "@Schema must be present on ReaderLendingsAvgPerMonthView");
    }

    @Test
    @DisplayName("equals: reflexive, null and different-class sanity")
    void equals_reflexive_null_diffClass() {
        var v = new ReaderLendingsAvgPerMonthView(2024, 1, List.of());
        assertEquals(v, v);
        assertNotEquals(v, null);
        assertNotEquals(v, "not-a-dto");
    }
}
