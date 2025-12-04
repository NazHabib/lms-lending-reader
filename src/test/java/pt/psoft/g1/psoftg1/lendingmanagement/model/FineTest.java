package pt.psoft.g1.psoftg1.lendingmanagement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * SUT: Fine (Entidade de domínio / JPA Entity)
 * Tipo: Teste unitário isolado (sem Spring/JPA)
 *
 * Black-box:
 *  - Regras de criação:
 *      * Lançar IllegalArgumentException quando lending.getDaysDelayed() <= 0
 *      * Lançar NullPointerException quando lending == null (ordem das operações)
 *      * Calcular centsValue = fineValuePerDayInCents * daysDelayed
 *      * Capturar fineValuePerDayInCents no momento de criação a partir de Lending
 *  - API pública:
 *      * Getters de pk, fineValuePerDayInCents, centsValue, lending
 *      * Único setter público permitido: setLending()
 *
 * White-box:
 *  - Metadados JPA/Bean Validation:
 *      * @Id + @GeneratedValue em pk
 *      * @PositiveOrZero em fineValuePerDayInCents e centsValue
 *      * @Column(updatable=false) em fineValuePerDayInCents
 *      * @OneToOne(optional=false, orphanRemoval=true) + @JoinColumn(name="lending_pk", nullable=true, unique=true)
 *      * Construtor sem argumentos protegido para JPA
 *
 * Isolamento:
 *  - Dependências externas isoladas via Mockito (Lending stub/mock)
 *  - Sem contexto Spring, sem base de dados
 */
@DisplayName("Fine – construction rules, annotations, and API")
class FineTest {

    // -----------------------
    // Construction rules
    // -----------------------

    @Test
    @DisplayName("constructs fine with correct per-day and total cents based on Lending")
    void constructsWithComputedValues() {
        Lending lending = mock(Lending.class);
        when(lending.getDaysDelayed()).thenReturn(3);
        when(lending.getFineValuePerDayInCents()).thenReturn(50); // €0.50/day

        Fine fine = new Fine(lending);

        assertEquals(50, fine.getFineValuePerDayInCents());
        assertEquals(150, fine.getCentsValue()); // 3 * 50
        assertSame(lending, fine.getLending());
    }

    @Test
    @DisplayName("throws IllegalArgumentException when lending is not overdue (daysDelayed <= 0)")
    void throwsWhenNotOverdue_zeroOrNegative() {
        Lending zero = mock(Lending.class);
        when(zero.getDaysDelayed()).thenReturn(0);
        when(zero.getFineValuePerDayInCents()).thenReturn(10);

        Lending negative = mock(Lending.class);
        when(negative.getDaysDelayed()).thenReturn(-5);
        when(negative.getFineValuePerDayInCents()).thenReturn(10);

        assertThrows(IllegalArgumentException.class, () -> new Fine(zero));
        assertThrows(IllegalArgumentException.class, () -> new Fine(negative));
    }

    @Test
    @DisplayName("throws NullPointerException when lending is null (due to call order)")
    void throwsWhenLendingNull() {
        // Fine constructor chama lending.getDaysDelayed() antes do requireNonNull,
        // portanto a exceção efetiva é NullPointerException.
        assertThrows(NullPointerException.class, () -> new Fine(null));
    }

    // -----------------------
    // Public API surface
    // -----------------------

    @Test
    @DisplayName("exposes only setLending as public setter (immutability of values after creation)")
    void onlyLendingHasPublicSetter() {
        boolean anyOtherPublicSetter =
                java.util.Arrays.stream(Fine.class.getMethods())
                        .filter(m -> Modifier.isPublic(m.getModifiers()))
                        .anyMatch(m ->
                                m.getName().startsWith("set")
                                        && !m.getName().equals("setLending")
                                        && m.getParameterCount() == 1
                        );
        assertFalse(anyOtherPublicSetter, "Fine should not expose setters besides setLending()");
    }

    @Test
    @DisplayName("setLending replaces association")
    void setLendingReplacesAssociation() {
        Lending first = mock(Lending.class);
        when(first.getDaysDelayed()).thenReturn(1);
        when(first.getFineValuePerDayInCents()).thenReturn(20);

        Fine fine = new Fine(first);

        Lending second = mock(Lending.class);
        fine.setLending(second);

        assertSame(second, fine.getLending());
    }

    // -----------------------
    // JPA / Validation metadata
    // -----------------------

    @Test
    @DisplayName("pk has @Id and @GeneratedValue")
    void pkHasIdAndGeneratedValue() throws Exception {
        Field pk = Fine.class.getDeclaredField("pk");
        assertNotNull(pk.getAnnotation(Id.class), "pk must have @Id");
        assertNotNull(pk.getAnnotation(GeneratedValue.class), "pk must have @GeneratedValue");
    }

    @Test
    @DisplayName("fineValuePerDayInCents has @PositiveOrZero and @Column(updatable=false)")
    void perDayFieldHasExpectedAnnotations() throws Exception {
        Field f = Fine.class.getDeclaredField("fineValuePerDayInCents");
        assertNotNull(f.getAnnotation(PositiveOrZero.class), "fineValuePerDayInCents must be @PositiveOrZero");

        Column col = f.getAnnotation(Column.class);
        assertNotNull(col, "fineValuePerDayInCents must have @Column");
        assertFalse(col.updatable(), "fineValuePerDayInCents must be non-updatable");
    }

    @Test
    @DisplayName("centsValue has @PositiveOrZero")
    void centsValueHasPositiveOrZero() throws Exception {
        Field f = Fine.class.getDeclaredField("centsValue");
        assertNotNull(f.getAnnotation(PositiveOrZero.class), "centsValue must be @PositiveOrZero");
    }

    @Test
    @DisplayName("lending is @OneToOne(optional=false, orphanRemoval=true) with @JoinColumn settings")
    void lendingAssociationAnnotations() throws Exception {
        Field f = Fine.class.getDeclaredField("lending");
        OneToOne one = f.getAnnotation(OneToOne.class);
        assertNotNull(one, "lending must be @OneToOne");
        assertFalse(one.optional(), "lending must be optional=false");
        assertTrue(one.orphanRemoval(), "lending must enable orphanRemoval");

        JoinColumn jc = f.getAnnotation(JoinColumn.class);
        assertNotNull(jc, "lending must have @JoinColumn");
        assertEquals("lending_pk", jc.name());
        assertFalse(jc.nullable());
        assertTrue(jc.unique());
    }

    @Test
    @DisplayName("has protected no-arg constructor for JPA")
    void hasProtectedNoArgConstructor() throws Exception {
        Constructor<Fine> ctor = Fine.class.getDeclaredConstructor();
        assertTrue(Modifier.isProtected(ctor.getModifiers()),
                "No-arg constructor should be protected for JPA");
    }
}
