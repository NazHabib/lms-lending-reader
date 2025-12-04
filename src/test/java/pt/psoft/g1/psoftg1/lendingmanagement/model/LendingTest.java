package pt.psoft.g1.psoftg1.lendingmanagement.model;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.usermanagement.model.Reader;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SUT: Lending (Entidade de domínio / JPA Entity)
 * Tipo: Teste unitário isolado (sem Spring/JPA reais)
 *
 * Black-box:
 *  - Regras de construção: book/reader não podem ser null, datas iniciais (start/limit),
 *    número de empréstimo, cálculo de dias (untilReturn/overdue) e multa.
 *  - Regras de devolução (setReturned): impedimento de devolução repetida,
 *    controlo de versão (optimistic locking).
 *  - Fábrica de bootstrapping: newBootstrappingLending configura ano/seq, datas e multa.
 *
 * White-box:
 *  - Verificação de anotações JPA/Validation (e.g., @Version, @ManyToOne, @Column(updatable=false)).
 *  - Ajustes internos de campos transitórios (daysUntilReturn/daysOverdue) e
 *    cálculo de fineValue via getDaysDelayed().
 *
 * Dependências:
 *  - Objetos reais mínimos (Book, ReaderDetails) apenas para satisfazer o construtor;
 *    testes executam 100% em memória.
 *
 * Isolamento:
 *  - Sem contexto Spring; sem acesso a base de dados.
 */
@PropertySource({"classpath:config/library.properties"})
@DisplayName("Lending – construction, behavior, versioning and annotations")
class LendingTest {

    // --------- FIXTURES (iguais aos teus testes originais) ---------
    private static final ArrayList<Author> authors = new ArrayList<>();
    private static Book book;
    private static ReaderDetails readerDetails;

    @Value("${lendingDurationInDays}")
    private int lendingDurationInDays; // usado somente pelos testes já existentes
    @Value("${fineValuePerDayInCents}")
    private int fineValuePerDayInCents; // usado somente pelos testes já existentes

    @BeforeAll
    public static void setup(){
        Author author = new Author("Manuel Antonio Pina",
                "Manuel António Pina foi um jornalista e escritor português, premiado em 2011 com o Prémio Camões",
                null);
        authors.add(author);
        book = new Book("9782826012092",
                "O Inspetor Max",
                "conhecido pastor-alemão que trabalha para a Judiciária, vai ser fundamental para resolver um importante caso de uma rede de malfeitores que quer colocar uma bomba num megaconcerto de uma ilustre cantora",
                new Genre("Romance"),
                authors,
                null);
        readerDetails = new ReaderDetails(1,
                Reader.newReader("manuel@gmail.com", "Manuelino123!", "Manuel Sarapinto das Coives"),
                "2000-01-01",
                "919191919",
                true,
                true,
                true,
                null,
                null);
    }

    // =================== TESTES ORIGINAIS (mantidos) ===================

    @Test
    void ensureBookNotNull(){
        assertThrows(IllegalArgumentException.class, () -> new Lending(null, readerDetails, 1, lendingDurationInDays, fineValuePerDayInCents));
    }

    @Test
    void ensureReaderNotNull(){
        assertThrows(IllegalArgumentException.class, () -> new Lending(book, null, 1, lendingDurationInDays, fineValuePerDayInCents));
    }

    @Test
    void ensureValidReaderNumber(){
        assertThrows(IllegalArgumentException.class, () -> new Lending(book, readerDetails, -1, lendingDurationInDays, fineValuePerDayInCents));
    }

    @Test
    void testSetReturned(){
        Lending lending = new Lending(book, readerDetails, 1, lendingDurationInDays, fineValuePerDayInCents);
        lending.setReturned(0,null);
        assertEquals(LocalDate.now(), lending.getReturnedDate());
    }

    @Test
    void testGetDaysDelayed(){
        Lending lending = new Lending(book, readerDetails, 1, lendingDurationInDays, fineValuePerDayInCents);
        assertEquals(0, lending.getDaysDelayed());
    }

    @Test
    void testGetDaysUntilReturn(){
        Lending lending = new Lending(book, readerDetails, 1, lendingDurationInDays, fineValuePerDayInCents);
        assertEquals(Optional.of(lendingDurationInDays), lending.getDaysUntilReturn());
    }

    @Test
    void testGetDaysOverDue(){
        Lending lending = new Lending(book, readerDetails, 1, lendingDurationInDays, fineValuePerDayInCents);
        assertEquals(Optional.empty(), lending.getDaysOverdue());
    }

    @Test
    void testGetTitle() {
        Lending lending = new Lending(book, readerDetails, 1, lendingDurationInDays, fineValuePerDayInCents);
        assertEquals("O Inspetor Max", lending.getTitle());
    }

    @Test
    void testGetLendingNumber() {
        Lending lending = new Lending(book, readerDetails, 1, lendingDurationInDays, fineValuePerDayInCents);
        assertEquals(LocalDate.now().getYear() + "/1", lending.getLendingNumber());
    }

    @Test
    void testGetBook() {
        Lending lending = new Lending(book, readerDetails, 1, lendingDurationInDays, fineValuePerDayInCents);
        assertEquals(book, lending.getBook());
    }

    @Test
    void testGetReaderDetails() {
        Lending lending = new Lending(book, readerDetails, 1, lendingDurationInDays, fineValuePerDayInCents);
        assertEquals(readerDetails, lending.getReaderDetails());
    }

    @Test
    void testGetStartDate() {
        Lending lending = new Lending(book, readerDetails, 1, lendingDurationInDays, fineValuePerDayInCents);
        assertEquals(LocalDate.now(), lending.getStartDate());
    }

    @Test
    void testGetLimitDate() {
        Lending lending = new Lending(book, readerDetails, 1, lendingDurationInDays, fineValuePerDayInCents);
        assertEquals(LocalDate.now().plusDays(lendingDurationInDays), lending.getLimitDate());
    }

    @Test
    void testGetReturnedDate() {
        Lending lending = new Lending(book, readerDetails, 1, lendingDurationInDays, fineValuePerDayInCents);
        assertNull(lending.getReturnedDate());
    }

    // =================== NOVOS TESTES (ampliam cobertura) ===================

    @Test
    @DisplayName("setReturned lança StaleObjectStateException quando versão não coincide")
    void setReturned_staleVersion_throws() throws Exception {
        int duration = 7, fineCents = 10;
        Lending lending = new Lending(book, readerDetails, 1, duration, fineCents);

        // Forçar versão atual = 1 para simular concorrência
        Field v = Lending.class.getDeclaredField("version");
        v.setAccessible(true);
        v.set(lending, 1L);

        assertThrows(org.hibernate.StaleObjectStateException.class,
                () -> lending.setReturned(0L, "late comment"));
        assertNull(lending.getReturnedDate(), "Não deve marcar devolução em caso de versão desatualizada");
    }

    @Test
    @DisplayName("setReturned duas vezes lança IllegalArgumentException")
    void setReturned_twice_throws() {
        int duration = 7, fineCents = 10;
        Lending lending = new Lending(book, readerDetails, 1, duration, fineCents);
        lending.setReturned(0L, "ok");

        assertThrows(IllegalArgumentException.class, () -> lending.setReturned(0L, "again"));
    }

    @Test
    @DisplayName("getDaysDelayed: devolvido dentro do prazo retorna 0")
    void getDaysDelayed_returnedOnTime_zero() {
        int duration = 5, fineCents = 10;
        LocalDate start = LocalDate.now().minusDays(3); // limite = start+5 -> daqui a 2 dias
        LocalDate returned = LocalDate.now();           // antes do limite

        Lending l = Lending.newBootstrappingLending(book, readerDetails,
                LocalDate.now().getYear(), 42, start, returned, duration, fineCents);

        assertEquals(0, l.getDaysDelayed());
        assertEquals(Optional.empty(), l.getDaysOverdue());
        assertTrue(l.getFineValueInCents().isEmpty());
    }

    @Test
    @DisplayName("getDaysDelayed: devolvido atrasado retorna dias de atraso e multa")
    void getDaysDelayed_returnedLate_positiveAndFine() {
        int duration = 3, fineCents = 50;
        LocalDate start = LocalDate.now().minusDays(10);   // limite = start+3 -> há 7 dias
        LocalDate returned = LocalDate.now().minusDays(2); // atraso = 5 dias

        Lending l = Lending.newBootstrappingLending(book, readerDetails,
                2024, 7, start, returned, duration, fineCents);

        assertEquals(5, l.getDaysDelayed());
        assertEquals(Optional.of(5), l.getDaysOverdue());
        assertEquals(Optional.of(5 * 50), l.getFineValueInCents());
    }

    @Test
    @DisplayName("getDaysDelayed: não devolvido e já passou do limite retorna dias de atraso")
    void getDaysDelayed_notReturned_overdue() {
        int duration = 4, fineCents = 10;
        LocalDate start = LocalDate.now().minusDays(10);   // limite = start+4 -> há 6 dias
        LocalDate returned = null;

        Lending l = Lending.newBootstrappingLending(book, readerDetails,
                2023, 10, start, returned, duration, fineCents);

        assertEquals(6, l.getDaysDelayed());
        assertEquals(Optional.of(6), l.getDaysOverdue());
        assertEquals(Optional.of(6 * 10), l.getFineValueInCents());
        assertTrue(l.getDaysUntilReturn().isEmpty(), "Sem dias até devolução quando já passou do limite");
    }

    @Test
    @DisplayName("getDaysUntilReturn: com prazo futuro devolve Optional com dias restantes")
    void getDaysUntilReturn_futureLimit() {
        int duration = 8, fineCents = 10;
        Lending l = new Lending(book, readerDetails, 99, duration, fineCents);

        assertEquals(Optional.of(duration), l.getDaysUntilReturn());
        assertTrue(l.getDaysOverdue().isEmpty());
    }

    @Test
    @DisplayName("newBootstrappingLending: valida argumentos nulos e constrói corretamente")
    void bootstrappingFactory_validatesAndBuilds() {
        int duration = 14, fineCents = 20;
        LocalDate start = LocalDate.of(2024, 3, 1);
        LocalDate ret = LocalDate.of(2024, 3, 10);

        Lending l = Lending.newBootstrappingLending(book, readerDetails,
                2024, 123, start, ret, duration, fineCents);

        assertEquals("2024/123", l.getLendingNumber());
        assertEquals(start, l.getStartDate());
        assertEquals(start.plusDays(duration), l.getLimitDate());
        assertEquals(ret, l.getReturnedDate());
        assertEquals(fineCents, l.getFineValuePerDayInCents());
    }

    @Test
    @DisplayName("newBootstrappingLending: book ou reader nulos lançam IllegalArgumentException")
    void bootstrappingFactory_nullArgs_throw() {
        int duration = 7, fineCents = 10;
        LocalDate start = LocalDate.now().minusDays(1);

        assertThrows(IllegalArgumentException.class,
                () -> Lending.newBootstrappingLending(null, readerDetails,
                        2024, 1, start, null, duration, fineCents));
        assertThrows(IllegalArgumentException.class,
                () -> Lending.newBootstrappingLending(book, null,
                        2024, 1, start, null, duration, fineCents));
    }

    // ------------------- Metadados JPA/Validation (white-box) -------------------

    @Test
    @DisplayName("tem @Version em version e construtor sem args protegido para JPA")
    void versionAndNoArgConstructor() throws Exception {
        Field v = Lending.class.getDeclaredField("version");
        assertNotNull(v.getAnnotation(jakarta.persistence.Version.class), "version deve ter @Version");

        Constructor<Lending> c = Lending.class.getDeclaredConstructor();
        assertTrue(Modifier.isProtected(c.getModifiers()), "construtor sem args deve ser protected");
    }

    @Test
    @DisplayName("startDate é @Column(nullable=false, updatable=false)")
    void startDateColumnSettings() throws Exception {
        Field sd = Lending.class.getDeclaredField("startDate");
        var col = sd.getAnnotation(jakarta.persistence.Column.class);
        assertNotNull(col, "startDate deve ter @Column");
        assertFalse(col.updatable());
        assertFalse(col.nullable());
    }

    @Test
    @DisplayName("book/readerDetails são @ManyToOne(optional=false)")
    void associationsManyToOne() throws Exception {
        Field b = Lending.class.getDeclaredField("book");
        var mb = b.getAnnotation(jakarta.persistence.ManyToOne.class);
        assertNotNull(mb);
        assertFalse(mb.optional());

        Field r = Lending.class.getDeclaredField("readerDetails");
        var mr = r.getAnnotation(jakarta.persistence.ManyToOne.class);
        assertNotNull(mr);
        assertFalse(mr.optional());
    }
}
