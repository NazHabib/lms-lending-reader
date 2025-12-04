package pt.psoft.g1.psoftg1.lendingmanagement.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.format.annotation.DateTimeFormat;

import static org.junit.jupiter.api.Assertions.*;

/**

 SUT: SearchLendingQuery (DTO)

 Tipo: Unitário isolado (sem Spring/JPA)

 Black-box:

 Comportamento gerado por Lombok @Data:

 * getters/setters

 * all-args e no-args constructors

 * equals/hashCode/toString consistentes


 Metadados @DateTimeFormat nos campos de data (pattern "yyyy-MM-dd")

 White-box:

 Verifica presença e valor do pattern nas anotações

 Exercita caminhos de equals: reflexivo, null, classe diferente, campos diferentes/iguais,

 e comportamento com subclasses (canEqual)
 */

@DisplayName("SearchLendingQuery – Lombok DTO + annotations")
class SearchLendingQueryTest {

    @Test
    @DisplayName("no-args constructor + setters/getters")
    void noArgs_thenSettersAndGettersWork() {
        SearchLendingQuery q = new SearchLendingQuery();
        q.setReaderNumber("123");
        q.setIsbn("9782826012092");
        q.setReturned(Boolean.TRUE);
        q.setStartDate("2024-01-01");
        q.setEndDate("2024-12-31");

        assertEquals("123", q.getReaderNumber());
        assertEquals("9782826012092", q.getIsbn());
        assertTrue(q.getReturned());
        assertEquals("2024-01-01", q.getStartDate());
        assertEquals("2024-12-31", q.getEndDate());
    }

    @Test
    @DisplayName("all-args constructor sets fields")
    void allArgsConstructor_setsFields() {
        SearchLendingQuery q = new SearchLendingQuery(
                "007", "9780000000000", null, "2023-05-01", null
        );

        assertEquals("007", q.getReaderNumber());
        assertEquals("9780000000000", q.getIsbn());
        assertNull(q.getReturned());
        assertEquals("2023-05-01", q.getStartDate());
        assertNull(q.getEndDate());
    }

    @Test
    @DisplayName("@DateTimeFormat present with pattern yyyy-MM-dd")
    void dateFields_haveDateTimeFormatPattern() throws Exception {
        var start = SearchLendingQuery.class.getDeclaredField("startDate")
                .getAnnotation(DateTimeFormat.class);
        var end = SearchLendingQuery.class.getDeclaredField("endDate")
                .getAnnotation(DateTimeFormat.class);

        assertNotNull(start, "startDate must be annotated with @DateTimeFormat");
        assertNotNull(end, "endDate must be annotated with @DateTimeFormat");

        assertEquals("yyyy-MM-dd", start.pattern());
        assertEquals("yyyy-MM-dd", end.pattern());
    }

    @Test
    @DisplayName("equals/hashCode: same values -> equal")
    void equals_hashCode_sameValues_true() {
        var a = new SearchLendingQuery("1","i", true,  "2024-01-01", "2024-01-31");
        var b = new SearchLendingQuery("1","i", true,  "2024-01-01", "2024-01-31");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("equals: different fields -> not equal")
    void equals_differentFields_false() {
        var a = new SearchLendingQuery("1","i", true,  "2024-01-01", "2024-01-31");
        var b = new SearchLendingQuery("2","i", true,  "2024-01-01", "2024-01-31");
        var c = new SearchLendingQuery("1","j", true,  "2024-01-01", "2024-01-31");
        var d = new SearchLendingQuery("1","i", false, "2024-01-01", "2024-01-31");
        var e = new SearchLendingQuery("1","i", true,  "2024-02-01", "2024-01-31");
        var f = new SearchLendingQuery("1","i", true,  "2024-01-01", "2024-02-01");

        assertNotEquals(a, b);
        assertNotEquals(a, c);
        assertNotEquals(a, d);
        assertNotEquals(a, e);
        assertNotEquals(a, f);
    }

    @Test
    @DisplayName("equals: reflexive/null/different class")
    void equals_reflexive_null_differentClass() {
        var a = new SearchLendingQuery("1","i", true,  "2024-01-01", "2024-01-31");

        assertEquals(a, a);               // reflexive
        assertNotEquals(a, null);         // null
        assertNotEquals(a, "not a dto");  // different class
    }

    // Subclass to exercise Lombok canEqual behavior
    static class SearchLendingQuerySub extends SearchLendingQuery {
        SearchLendingQuerySub(String readerNumber, String isbn, Boolean returned, String start, String end) {
            super(readerNumber, isbn, returned, start, end);
        }
    }

    @Test
    @DisplayName("equals: subclass with same values still considered equal (canEqual true)")
    void equals_withSubclass_canEqualTrue() {
        var a = new SearchLendingQuery("1","i", true, "2024-01-01","2024-01-31");
        var b = new SearchLendingQuerySub("1","i", true, "2024-01-01","2024-01-31");

        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
    }

    @Test
    @DisplayName("toString includes key fields")
    void toString_includesFields() {
        var a = new SearchLendingQuery("7","isbn", null, "2024-03-10", "2024-03-20");
        String s = a.toString();

        assertTrue(s.contains("readerNumber=7"));
        assertTrue(s.contains("isbn=isbn"));
        assertTrue(s.contains("startDate=2024-03-10"));
        assertTrue(s.contains("endDate=2024-03-20"));
    }

    @Test
    void equals_reflexive_true() {
        var q = new SearchLendingQuery("r1", "isbn", true, "2024-01-01", "2024-02-01");
        assertTrue(q.equals(q)); // self comparison
    }

    @Test
    void equals_null_false() {
        var q = new SearchLendingQuery("r1", "isbn", true, "2024-01-01", "2024-02-01");
        assertFalse(q.equals(null)); // null branch
    }

    @Test
    void equals_differentClass_false() {
        var q = new SearchLendingQuery("r1", "isbn", true, "2024-01-01", "2024-02-01");
        assertFalse(q.equals("not a query")); // type mismatch branch
    }

    @Test
    void equals_sameFields_true() {
        var a = new SearchLendingQuery("r1", "isbn", true, "2024-01-01", "2024-02-01");
        var b = new SearchLendingQuery("r1", "isbn", true, "2024-01-01", "2024-02-01");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equals_differentReaderNumber_false() {
        var a = new SearchLendingQuery("r1", "isbn", true, "2024-01-01", "2024-02-01");
        var b = new SearchLendingQuery("r2", "isbn", true, "2024-01-01", "2024-02-01");
        assertNotEquals(a, b);
    }

    @Test
    void equals_differentIsbn_false() {
        var a = new SearchLendingQuery("r1", "isbn1", true, "2024-01-01", "2024-02-01");
        var b = new SearchLendingQuery("r1", "isbn2", true, "2024-01-01", "2024-02-01");
        assertNotEquals(a, b);
    }

    @Test
    void equals_differentReturned_false() {
        var a = new SearchLendingQuery("r1", "isbn", true, "2024-01-01", "2024-02-01");
        var b = new SearchLendingQuery("r1", "isbn", false, "2024-01-01", "2024-02-01");
        assertNotEquals(a, b);
    }

    @Test
    void equals_differentStartDate_false() {
        var a = new SearchLendingQuery("r1", "isbn", true, "2024-01-01", "2024-02-01");
        var b = new SearchLendingQuery("r1", "isbn", true, "2024-01-02", "2024-02-01");
        assertNotEquals(a, b);
    }

    @Test
    void equals_differentEndDate_false() {
        var a = new SearchLendingQuery("r1", "isbn", true, "2024-01-01", "2024-02-01");
        var b = new SearchLendingQuery("r1", "isbn", true, "2024-01-01", "2024-02-02");
        assertNotEquals(a, b);
    }

    @Test
    void canEqual_true_withSubclass() {
        class SubQuery extends SearchLendingQuery {}
        var base = new SearchLendingQuery("r1", "isbn", true, "2024-01-01", "2024-02-01");
        var sub = new SubQuery();
        sub.setReaderNumber("r1");
        sub.setIsbn("isbn");
        sub.setReturned(true);
        sub.setStartDate("2024-01-01");
        sub.setEndDate("2024-02-01");

        // covers canEqual branch
        assertTrue(base.canEqual(sub));
        assertEquals(base, sub);
    }

    private SearchLendingQuery q(String r, String i, Boolean ret, String sd, String ed) {
        return new SearchLendingQuery(r, i, ret, sd, ed);
    }

    // ----- baseline / guards -----

    @Test void equals_sameFields_true_and_hashCodeEqual() {
        var a = q("r","i",true,"2024-01-01","2024-02-01");
        var b = q("r","i",true,"2024-01-01","2024-02-01");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    // ----- readerNumber branches -----

    @Test void equals_readerNull_vs_nonNull_false_aNull_bValue() {
        var a = q(null,"i",true,"2024-01-01","2024-02-01");
        var b = q("r","i",true,"2024-01-01","2024-02-01");
        assertNotEquals(a, b);
    }

    @Test void equals_readerNonNull_vs_null_false_aValue_bNull() {
        var a = q("r","i",true,"2024-01-01","2024-02-01");
        var b = q(null,"i",true,"2024-01-01","2024-02-01");
        assertNotEquals(a, b);
    }

    @Test void equals_readerBothNull_otherFieldsEqual_true() {
        var a = q(null,"i",true,"2024-01-01","2024-02-01");
        var b = q(null,"i",true,"2024-01-01","2024-02-01");
        assertEquals(a, b);
    }

    @Test void equals_readerDifferentValues_false() {
        var a = q("r1","i",true,"2024-01-01","2024-02-01");
        var b = q("r2","i",true,"2024-01-01","2024-02-01");
        assertNotEquals(a, b);
    }

    // ----- isbn branches -----

    @Test void equals_isbnNull_vs_nonNull_false() {
        var a = q("r",null,true,"2024-01-01","2024-02-01");
        var b = q("r","i",true,"2024-01-01","2024-02-01");
        assertNotEquals(a, b);
    }

    @Test void equals_isbnNonNull_vs_null_false() {
        var a = q("r","i",true,"2024-01-01","2024-02-01");
        var b = q("r",null,true,"2024-01-01","2024-02-01");
        assertNotEquals(a, b);
    }

    @Test void equals_isbnBothNull_otherFieldsEqual_true() {
        var a = q("r",null,true,"2024-01-01","2024-02-01");
        var b = q("r",null,true,"2024-01-01","2024-02-01");
        assertEquals(a, b);
    }

    @Test void equals_isbnDifferent_false() {
        var a = q("r","i1",true,"2024-01-01","2024-02-01");
        var b = q("r","i2",true,"2024-01-01","2024-02-01");
        assertNotEquals(a, b);
    }

    // ----- returned (Boolean nullable) branches -----

    @Test void equals_returnedNull_vs_nonNull_false() {
        var a = q("r","i",null,"2024-01-01","2024-02-01");
        var b = q("r","i",Boolean.TRUE,"2024-01-01","2024-02-01");
        assertNotEquals(a, b);
    }

    @Test void equals_returnedNonNull_vs_null_false() {
        var a = q("r","i",Boolean.TRUE,"2024-01-01","2024-02-01");
        var b = q("r","i",null,"2024-01-01","2024-02-01");
        assertNotEquals(a, b);
    }

    @Test void equals_returnedBothNull_true() {
        var a = q("r","i",null,"2024-01-01","2024-02-01");
        var b = q("r","i",null,"2024-01-01","2024-02-01");
        assertEquals(a, b);
    }

    @Test void equals_returnedDifferent_false() {
        var a = q("r","i",Boolean.TRUE,"2024-01-01","2024-02-01");
        var b = q("r","i",Boolean.FALSE,"2024-01-01","2024-02-01");
        assertNotEquals(a, b);
    }

    // ----- startDate branches -----

    @Test void equals_startDateNull_vs_nonNull_false() {
        var a = q("r","i",true,null,"2024-02-01");
        var b = q("r","i",true,"2024-01-01","2024-02-01");
        assertNotEquals(a, b);
    }

    @Test void equals_startDateNonNull_vs_null_false() {
        var a = q("r","i",true,"2024-01-01","2024-02-01");
        var b = q("r","i",true,null,"2024-02-01");
        assertNotEquals(a, b);
    }

    @Test void equals_startDateBothNull_true() {
        var a = q("r","i",true,null,"2024-02-01");
        var b = q("r","i",true,null,"2024-02-01");
        assertEquals(a, b);
    }

    @Test void equals_startDateDifferent_false() {
        var a = q("r","i",true,"2024-01-01","2024-02-01");
        var b = q("r","i",true,"2024-01-02","2024-02-01");
        assertNotEquals(a, b);
    }

    // ----- endDate branches -----

    @Test void equals_endDateNull_vs_nonNull_false() {
        var a = q("r","i",true,"2024-01-01",null);
        var b = q("r","i",true,"2024-01-01","2024-02-01");
        assertNotEquals(a, b);
    }

    @Test void equals_endDateNonNull_vs_null_false() {
        var a = q("r","i",true,"2024-01-01","2024-02-01");
        var b = q("r","i",true,"2024-01-01",null);
        assertNotEquals(a, b);
    }

    @Test void equals_endDateBothNull_true() {
        var a = q("r","i",true,"2024-01-01",null);
        var b = q("r","i",true,"2024-01-01",null);
        assertEquals(a, b);
    }

    @Test void equals_endDateDifferent_false() {
        var a = q("r","i",true,"2024-01-01","2024-02-01");
        var b = q("r","i",true,"2024-01-01","2024-02-02");
        assertNotEquals(a, b);
    }

    // ----- canEqual branch via subclass -----

    static class SubQuery extends SearchLendingQuery {
        SubQuery(String r, String i, Boolean ret, String s, String e) { super(r, i, ret, s, e); }
    }

    @Test void canEqual_true_withSubclass_sameValues() {
        var base = q("r","i",true,"2024-01-01","2024-02-01");
        var sub  = new SubQuery("r","i",true,"2024-01-01","2024-02-01");
        assertTrue(base.canEqual(sub));   // exercises canEqual path
        assertEquals(base, sub);
        assertEquals(base.hashCode(), sub.hashCode());
    }


}
