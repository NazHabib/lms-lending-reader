/* 
package pt.psoft.g1.psoftg1.bootstrapping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.stream.IntStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import pt.psoft.g1.psoftg1.authormanagement.repositories.AuthorRepository;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;
import pt.psoft.g1.psoftg1.genremanagement.repositories.GenreRepository;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.LendingRepository;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderRepository;
import pt.psoft.g1.psoftg1.shared.repositories.PhotoRepository;
import pt.psoft.g1.psoftg1.shared.services.ForbiddenNameService;
import pt.psoft.g1.psoftg1.usermanagement.repositories.UserRepository;
*/
/**
 * SUTs: Bootstrapper + UserBootstrapper
 * Type: Integration (JPA slice) with H2 — no web/server; manual instantiation of bootstrappers.
 * Isolation: non-JPA collaborators are local mocks; @Value fields set via reflection.
 */
/* 
@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class BootstrapIntegrationIT {

    // Real repositories (H2)
    @org.springframework.beans.factory.annotation.Autowired private GenreRepository genreRepository;
    @org.springframework.beans.factory.annotation.Autowired private BookRepository bookRepository;
    @org.springframework.beans.factory.annotation.Autowired private AuthorRepository authorRepository;
    @org.springframework.beans.factory.annotation.Autowired private LendingRepository lendingRepository;
    @org.springframework.beans.factory.annotation.Autowired private ReaderRepository readerRepository;
    @org.springframework.beans.factory.annotation.Autowired private UserRepository userRepository;
    @org.springframework.beans.factory.annotation.Autowired private JdbcTemplate jdbcTemplate;

    /* Create Bootstrapper with non-JPA deps mocked and @Value fields set. */

    /* 
    private Bootstrapper newBootstrapper(int lendingDays, int fineCents) throws Exception {
        PhotoRepository photoRepo = mock(PhotoRepository.class);
        ForbiddenNameService forbiddenSvc = mock(ForbiddenNameService.class);

        Bootstrapper b = new Bootstrapper(
                genreRepository, bookRepository, authorRepository,
                lendingRepository, readerRepository, photoRepo, forbiddenSvc
        );
        setField(b, "lendingDurationInDays", lendingDays);
        setField(b, "fineValuePerDayInCents", fineCents);
        return b;
    }

    private UserBootstrapper newUserBootstrapper() {
        // Uses real jdbcTemplate (H2) for UPDATE timestamps
        return new UserBootstrapper(userRepository, readerRepository, genreRepository, jdbcTemplate);
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field f = findField(target.getClass(), name);
        f.setAccessible(true);
        f.set(target, value);
    }
    private static Field findField(Class<?> c, String name) throws NoSuchFieldException {
        for (Class<?> k = c; k != null; k = k.getSuperclass()) {
            try { return k.getDeclaredField(name); }
            catch (NoSuchFieldException ignored) {}
        }
        throw new NoSuchFieldException(name);
    }

    @Test
    @DisplayName("Populate once (UserBootstrapper first, then Bootstrapper) without crashes and with seeded data present")
    void populate_core_data_once() throws Exception {
        var userBootstrapper = newUserBootstrapper();
        var bootstrapper = newBootstrapper(14, 50);

        // IMPORTANT: readers must exist before lendings → run user bootstrapper first
        userBootstrapper.run();
        bootstrapper.run();

        final int year = LocalDate.now().getYear();

        // Users / Readers
        assertThat(userRepository.findByUsername("maria@gmail.com")).isPresent();
        IntStream.rangeClosed(1, 8).forEach(n ->
                assertThat(readerRepository.findByReaderNumber(year + "/" + n)).isPresent()
        );

        // Genres (subset)
        assertThat(genreRepository.findByString("Fantasia")).isPresent();
        assertThat(genreRepository.findByString("Informação")).isPresent();
        assertThat(genreRepository.findByString("Infantil")).isPresent();
        assertThat(genreRepository.findByString("Thriller")).isPresent();

        // Books (subset)
        assertThat(bookRepository.findByIsbn("9789720706386")).isPresent();
        assertThat(bookRepository.findByIsbn("9789723716160")).isPresent();
        assertThat(bookRepository.findByIsbn("9789895612864")).isPresent();

        // Authors (subset)
        assertThat(authorRepository.searchByNameName("J R R Tolkien")).isNotEmpty();
        assertThat(authorRepository.searchByNameName("Manuel Antonio Pina")).isNotEmpty();

        // Lendings: first sequence created by seeding (uses year 2024 in bootstrap logic)
        assertThat(lendingRepository.findByLendingNumber("2024/1")).isPresent();
    }

    @Test
    @DisplayName("Idempotency: running both bootstrappers twice doesn’t throw and data remains accessible")
    void idempotent_second_run() throws Exception {
        var userBootstrapper = newUserBootstrapper();
        var bootstrapper = newBootstrapper(14, 50);

        // First pass
        userBootstrapper.run();
        bootstrapper.run();

        // Second pass should not error (lookups/guards prevent duplicates)
        assertThatCode(() -> {
            userBootstrapper.run();
            bootstrapper.run();
        }).doesNotThrowAnyException();

        // Sanity: key rows still present
        assertThat(userRepository.findByUsername("maria@gmail.com")).isPresent();
        assertThat(bookRepository.findByIsbn("9789720706386")).isPresent();
        assertThat(genreRepository.findByString("Fantasia")).isPresent();
        assertThat(lendingRepository.findByLendingNumber("2024/1")).isPresent();
    }
}

*/