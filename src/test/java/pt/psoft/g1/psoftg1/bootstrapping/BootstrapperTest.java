package pt.psoft.g1.psoftg1.bootstrapping;

import org.junit.jupiter.api.*;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.repositories.AuthorRepository;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.repositories.GenreRepository;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.LendingRepository;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderRepository;
import pt.psoft.g1.psoftg1.shared.repositories.PhotoRepository;
import pt.psoft.g1.psoftg1.shared.services.ForbiddenNameService;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SUT: Bootstrapper — boot-time data seeding.
 * Pure unit tests: all collaborators mocked, private/protected methods invoked by reflection,
 * and @Value fields set by reflection to keep isolation at 100 %.
 */
class BootstrapperTest {

    private GenreRepository genreRepository;
    private BookRepository bookRepository;
    private AuthorRepository authorRepository;
    private LendingRepository lendingRepository;
    private ReaderRepository readerRepository;
    private PhotoRepository photoRepository;
    private ForbiddenNameService forbiddenNameService;

    private Bootstrapper sut;

    @BeforeEach
    void setUp() throws Exception {
        genreRepository = mock(GenreRepository.class);
        bookRepository = mock(BookRepository.class);
        authorRepository = mock(AuthorRepository.class);
        lendingRepository = mock(LendingRepository.class);
        readerRepository = mock(ReaderRepository.class);
        photoRepository = mock(PhotoRepository.class);
        forbiddenNameService = mock(ForbiddenNameService.class);

        sut = new Bootstrapper(
                genreRepository, bookRepository, authorRepository,
                lendingRepository, readerRepository, photoRepository,
                forbiddenNameService
        );

        // set @Value fields
        setField(sut, "lendingDurationInDays", 14);
        setField(sut, "fineValuePerDayInCents", 50);
    }

    /* ---------- createAuthors ---------- */

    @Test
    @DisplayName("createAuthors — saves all 9 authors when missing")
    void createAuthors_savesAll() throws Exception {
        when(authorRepository.searchByNameName(anyString())).thenReturn(Collections.emptyList());

        invokePrivate(sut, "createAuthors");

        verify(authorRepository, times(9)).save(any(Author.class));
    }

    @Test
    @DisplayName("createAuthors — skips all when present")
    void createAuthors_skipsWhenExisting() throws Exception {
        when(authorRepository.searchByNameName(anyString()))
                .thenReturn(List.of(new Author("exists", "bio", null)));

        invokePrivate(sut, "createAuthors");

        verify(authorRepository, never()).save(any());
    }

    /* ---------- createGenres ---------- */

    @Test
    @DisplayName("createGenres — saves all 5 missing genres")
    void createGenres_savesAll() throws Exception {
        when(genreRepository.findByString(anyString())).thenReturn(Optional.empty());

        invokePrivate(sut, "createGenres");

        verify(genreRepository, times(5)).save(any(Genre.class));
    }

    @Test
    @DisplayName("createGenres — skips when all exist")
    void createGenres_skipsWhenExisting() throws Exception {
        when(genreRepository.findByString(anyString())).thenReturn(Optional.of(new Genre("exists")));

        invokePrivate(sut, "createGenres");

        verify(genreRepository, never()).save(any());
    }

    /* ---------- loadForbiddenNames ---------- */

    @Test
    @DisplayName("loadForbiddenNames — delegates to ForbiddenNameService")
    void loadForbiddenNames_callsService() throws Exception {
        invokePrivate(sut, "loadForbiddenNames");
        verify(forbiddenNameService).loadDataFromFile("forbiddenNames.txt");
    }

    /* ---------- createBooks ---------- */

    @Test
    @DisplayName("createBooks — saves all 10 books when ISBNs are missing and prerequisites exist")
    void createBooks_savesAll() throws Exception {
        // All books are missing
        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.empty());

        // Required genres exist
        when(genreRepository.findByString(anyString()))
                .thenAnswer(inv -> Optional.of(new Genre((String) inv.getArgument(0))));

        // Every author lookup returns at least one author
        when(authorRepository.searchByNameName(anyString()))
                .thenAnswer(inv -> List.of(new Author((String) inv.getArgument(0), "bio", null)));

        invokePrivate(sut, "createBooks");

        verify(bookRepository, times(10)).save(any(Book.class));
    }

    @Test
    @DisplayName("createBooks — skips all when ISBNs already exist")
    void createBooks_skipsWhenExisting() throws Exception {
        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.of(mock(Book.class)));
        when(genreRepository.findByString(anyString())).thenReturn(Optional.of(new Genre("G")));
        when(authorRepository.searchByNameName(anyString()))
                .thenReturn(List.of(new Author("A", "bio", null)));

        invokePrivate(sut, "createBooks");

        verify(bookRepository, never()).save(any());
    }

    @Test
    @DisplayName("createBooks — mixed paths: some authors missing -> some books skipped")
    void createBooks_mixedBranches() throws Exception {
        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.empty());
        when(genreRepository.findByString(anyString()))
                .thenAnswer(inv -> Optional.of(new Genre((String) inv.getArgument(0))));

        // Skip #1 + #2 (Manuel) and #5 (Antoine)
        when(authorRepository.searchByNameName("Manuel Antonio Pina")).thenReturn(Collections.emptyList());
        when(authorRepository.searchByNameName("Antoine de Saint Exupéry")).thenReturn(Collections.emptyList());

        when(authorRepository.searchByNameName(argThat(n ->
                !List.of("Manuel Antonio Pina", "Antoine de Saint Exupéry").contains(n))))
                .thenAnswer(inv -> List.of(new Author((String) inv.getArgument(0), "bio", null)));

        invokePrivate(sut, "createBooks");

        verify(bookRepository, times(7)).save(any(Book.class));
    }

    @Test
    @DisplayName("createBooks — first genre ('Infantil') missing → no exception, no saves")
    void createBooks_firstGenreMissing_noThrow_noSaves() throws Exception {
        when(genreRepository.findByString("Infantil")).thenReturn(Optional.empty());

        assertThatCode(() -> invokePrivate(sut, "createBooks")).doesNotThrowAnyException();
        verify(bookRepository, never()).save(any());
    }

    @Test
    @DisplayName("createBooks — later genre ('Informação') missing → no exception, no saves")
    void createBooks_informationGenreMissing_noThrow_noSaves() throws Exception {
        when(genreRepository.findByString("Infantil")).thenReturn(Optional.of(new Genre("Infantil")));
        when(genreRepository.findByString("Informação")).thenReturn(Optional.empty());

        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.of(mock(Book.class)));
        when(authorRepository.searchByNameName(anyString()))
                .thenReturn(List.of(new Author("any", "bio", null)));

        assertThatCode(() -> invokePrivate(sut, "createBooks")).doesNotThrowAnyException();
        verify(bookRepository, never()).save(any());
    }

    /* ---------- createLendings ---------- */

    @Test
    @DisplayName("createLendings — no saves when all lending numbers already exist")
    void createLendings_noSavesWhenExisting() throws Exception {
        stubAllBooksPresent();
        stubAllReadersPresent();
        when(lendingRepository.findByLendingNumber(anyString()))
                .thenReturn(Optional.of(mock(Lending.class)));

        invokePrivate(sut, "createLendings");

        verify(lendingRepository, never()).save(any());
    }

    @Test
    @DisplayName("createLendings — hit every loop body once (9 saves total)")
    void createLendings_coversAllLoops_onceEach() throws Exception {
        stubAllBooksPresent();
        stubAllReadersPresent();

        Set<String> empties = Set.of(
                "2024/1","2024/4","2024/7","2024/10",
                "2024/13","2024/19","2024/24","2024/30","2024/36"
        );
        when(lendingRepository.findByLendingNumber(anyString()))
                .thenAnswer(inv -> empties.contains(inv.getArgument(0, String.class))
                        ? Optional.empty()
                        : Optional.of(mock(Lending.class)));

        invokePrivate(sut, "createLendings");

        verify(lendingRepository, times(9)).save(any(Lending.class));
    }

    @Test
    @DisplayName("createLendings — last loop (36..45) all created (10 saves) to cover block fully")
    void createLendings_lastLoop_allSaves() throws Exception {
        stubAllBooksPresent();
        stubAllReadersPresent();

        when(lendingRepository.findByLendingNumber(anyString()))
                .thenAnswer(inv -> {
                    String ln = inv.getArgument(0, String.class);
                    return ln.matches("2024/(3[6-9]|4[0-5])")
                            ? Optional.empty()
                            : Optional.of(mock(Lending.class));
                });

        invokePrivate(sut, "createLendings");

        // 10 entries in that loop → 10 saves
        verify(lendingRepository, times(10)).save(any(Lending.class));
    }

    @Test
    @DisplayName("createLendings — FALSE branch for 'all books present' (no saves)")
    void createLendings_booksListNotBuilt_branchFalse() throws Exception {
        when(bookRepository.findByIsbn("9789720706386")).thenReturn(Optional.empty());
        for (String isbn : new String[]{
                "9789723716160","9789895612864","9782722203402","9789722328296",
                "9789895702756","9789897776090","9789896379636","9789896378905","9789896375225"
        }) {
            when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.of(mock(Book.class)));
        }
        when(lendingRepository.findByLendingNumber(anyString()))
                .thenReturn(Optional.of(mock(Lending.class)));
        stubAllReadersPresent();

        invokePrivate(sut, "createLendings");

        verify(lendingRepository, never()).save(any());
    }

    @Test
    @DisplayName("createLendings — FALSE branch for 'all readers present' (no saves)")
    void createLendings_readersListNotBuilt_branchFalse() throws Exception {
        stubAllBooksPresent();
        when(readerRepository.findByReaderNumber("2024/1")).thenReturn(Optional.of(mock(ReaderDetails.class)));
        when(readerRepository.findByReaderNumber("2024/2")).thenReturn(Optional.of(mock(ReaderDetails.class)));
        when(readerRepository.findByReaderNumber("2024/3")).thenReturn(Optional.empty());
        for (int i = 4; i <= 6; i++) {
            when(readerRepository.findByReaderNumber("2024/" + i))
                    .thenReturn(Optional.of(mock(ReaderDetails.class)));
        }
        when(lendingRepository.findByLendingNumber(anyString()))
                .thenReturn(Optional.of(mock(Lending.class)));

        invokePrivate(sut, "createLendings");

        verify(lendingRepository, never()).save(any());
    }

    /* ---------- run(...) smoke ---------- */

    @Test
    @DisplayName("run — end-to-end call path with all data present (no exceptions)")
    void run_smoke() {
        when(authorRepository.searchByNameName(anyString())).thenReturn(List.of(new Author("A","b",null)));
        when(genreRepository.findByString(anyString())).thenReturn(Optional.of(new Genre("G")));
        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.of(mock(Book.class)));
        stubAllBooksPresent();
        stubAllReadersPresent();
        when(lendingRepository.findByLendingNumber(anyString())).thenReturn(Optional.of(mock(Lending.class)));

        assertThatCode(() -> sut.run()).doesNotThrowAnyException();
    }

    /* ---------- helpers ---------- */

    private void invokePrivate(Object target, String methodName, Object... args) throws Exception {
        Class<?>[] types = Arrays.stream(args).map(Object::getClass).toArray(Class[]::new);
        Method m = findMethod(target.getClass(), methodName, types);
        m.setAccessible(true);
        m.invoke(target, args);
    }

    private Method findMethod(Class<?> c, String name, Class<?>[] types) throws NoSuchMethodException {
        for (Class<?> k = c; k != null; k = k.getSuperclass()) {
            try { return k.getDeclaredMethod(name, types); }
            catch (NoSuchMethodException ignored) {}
        }
        throw new NoSuchMethodException(name);
    }

    private void setField(Object target, String name, Object value) throws Exception {
        Field f = findField(target.getClass(), name);
        f.setAccessible(true);
        f.set(target, value);
    }

    private Field findField(Class<?> c, String name) throws NoSuchFieldException {
        for (Class<?> k = c; k != null; k = k.getSuperclass()) {
            try { return k.getDeclaredField(name); }
            catch (NoSuchFieldException ignored) {}
        }
        throw new NoSuchFieldException(name);
    }

    private void stubAllBooksPresent() {
        String[] isbns = {
                "9789720706386","9789723716160","9789895612864","9782722203402",
                "9789722328296","9789895702756","9789897776090","9789896379636",
                "9789896378905","9789896375225"
        };
        for (String isbn : isbns) {
            when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.of(mock(Book.class)));
        }
    }

    private void stubAllReadersPresent() {
        for (int i = 1; i <= 6; i++) {
            when(readerRepository.findByReaderNumber("2024/" + i))
                    .thenReturn(Optional.of(mock(ReaderDetails.class)));
        }
    }
}
