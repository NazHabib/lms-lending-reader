package pt.psoft.g1.psoftg1.readermanagement.services;

import io.swagger.v3.oas.annotations.media.Schema;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SUT: ReaderLendingsAvgPerMonthDto (DTO)
 * Tipo: Unitário isolado (sem dependências externas)
 *
 * O que cobrimos:
 *  - Construtor all-args (valores atribuídos corretamente).
 *  - Getters/Setters gerados por Lombok @Data.
 *  - equals/hashCode (objetos iguais; reflexivo/null/classe diferente; campos diferentes).
 *  - toString contém campos relevantes.
 *  - Lista não é defensive-copied (aliasing) e aceita null.
 *  - @Schema presente na classe.
 */
@DisplayName("ReaderLendingsAvgPerMonthDto – Lombok & DTO behavior")
class ReaderLendingsAvgPerMonthDtoTest {

    private static ReaderAverageDto avg(String name, long count) {
        // Construção “realista” mas simples: apenas seta lendingCount
        ReaderAverageDto dto = new ReaderAverageDto();
        dto.setLendingCount(count);
        // readerView pode ficar null (sem validação Bean Validation no teste)
        return dto;
    }

    @Test
    @DisplayName("all-args constructor define campos corretamente")
    void allArgsConstructor_setsFields() {
        List<ReaderAverageDto> list = List.of(avg("A", 3L));
        ReaderLendingsAvgPerMonthDto dto = new ReaderLendingsAvgPerMonthDto(2024, 10, list);

        assertEquals(2024, dto.getYear());
        assertEquals(10, dto.getMonth());
        assertEquals(list, dto.getDurationAverages());
    }

    @Test
    @DisplayName("getters/setters de @Data funcionam")
    void lombok_getters_setters_work() {
        ReaderLendingsAvgPerMonthDto dto =
                new ReaderLendingsAvgPerMonthDto(2023, 8, new ArrayList<>());

        dto.setYear(2025);
        dto.setMonth(1);

        List<ReaderAverageDto> newList = new ArrayList<>();
        newList.add(avg("X", 5L));
        dto.setDurationAverages(newList);

        assertEquals(2025, dto.getYear());
        assertEquals(1, dto.getMonth());
        assertSame(newList, dto.getDurationAverages());
        assertEquals(1, dto.getDurationAverages().size());
        assertEquals(5L, dto.getDurationAverages().get(0).getLendingCount());
    }

    @Test
    @DisplayName("equals/hashCode – objetos com mesmos campos são iguais")
    void equalsHashCode_sameData_areEqual() {
        List<ReaderAverageDto> a1 = List.of(avg("A", 2L));
        List<ReaderAverageDto> a2 = List.of(avg("A", 2L));

        ReaderLendingsAvgPerMonthDto x =
                new ReaderLendingsAvgPerMonthDto(2024, 9, a1);
        ReaderLendingsAvgPerMonthDto y =
                new ReaderLendingsAvgPerMonthDto(2024, 9, a2);

        assertEquals(x, y);
        assertEquals(x.hashCode(), y.hashCode());
    }

    @Test
    @DisplayName("equals – campos diferentes tornam objetos diferentes")
    void equals_differentFields_notEqual() {
        var base  = new ReaderLendingsAvgPerMonthDto(2024, 9, List.of(avg("A", 1L)));
        var yDiff = new ReaderLendingsAvgPerMonthDto(2023, 9, List.of(avg("A", 1L)));
        var mDiff = new ReaderLendingsAvgPerMonthDto(2024, 8, List.of(avg("A", 1L)));
        var lDiff = new ReaderLendingsAvgPerMonthDto(2024, 9, List.of(avg("B", 2L)));

        assertNotEquals(base, yDiff);
        assertNotEquals(base, mDiff);
        assertNotEquals(base, lDiff);
    }

    @Test
    @DisplayName("equals – reflexivo, null e classe diferente")
    void equals_reflexive_null_diffClass() {
        var dto = new ReaderLendingsAvgPerMonthDto(2025, 3, List.of());
        assertTrue(dto.equals(dto));
        assertFalse(dto.equals(null));
        assertFalse(dto.equals("not-a-dto"));
    }

    @Test
    @DisplayName("toString contém year, month e algum conteúdo da lista")
    void toString_containsFields() {
        var dto = new ReaderLendingsAvgPerMonthDto(2024, 12, List.of(avg("A", 4L)));
        String s = dto.toString();

        assertTrue(s.contains("2024"));
        assertTrue(s.contains("12"));
        assertTrue(s.contains("durationAverages"));
    }

    @Test
    @DisplayName("lista é referenciada (sem defensive copy) e aceita null")
    void list_aliasing_and_null() {
        List<ReaderAverageDto> shared = new ArrayList<>();
        shared.add(avg("A", 1L));

        var dto = new ReaderLendingsAvgPerMonthDto(2025, 6, shared);
        // aliasing
        shared.add(avg("B", 2L));
        assertSame(shared, dto.getDurationAverages());
        assertEquals(2, dto.getDurationAverages().size());

        // null allowed
        dto.setDurationAverages(null);
        assertNull(dto.getDurationAverages());
    }

    @Test
    @DisplayName("@Schema presente na classe")
    void schemaAnnotation_present() {
        Annotation ann = ReaderLendingsAvgPerMonthDto.class.getAnnotation(Schema.class);
        assertNotNull(ann, "@Schema deve estar presente");
    }
}
