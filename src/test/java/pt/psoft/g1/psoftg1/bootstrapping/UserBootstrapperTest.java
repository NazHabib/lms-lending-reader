package pt.psoft.g1.psoftg1.bootstrapping;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.repositories.GenreRepository;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderRepository;
import pt.psoft.g1.psoftg1.usermanagement.model.User;
import pt.psoft.g1.psoftg1.usermanagement.repositories.UserRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SUT: UserBootstrapper (boot-time user seeding)
 * Type: Pure unit tests, fully isolated with Mockito (no Spring context, no DB/JDBC).
 *
 * What we verify:
 *  - When users do NOT exist, the bootstrapper creates 8 Readers + 1 Librarian, creates
 *    8 ReaderDetails, and executes 8 JDBC updates (one per inserted Reader).
 *  - When users already exist, nothing is saved and no JDBC updates are executed.
 *  - Reader numbers looked up use the current year and expected suffixes (/1..../8).
 */
public class UserBootstrapperTest {

    private UserBootstrapper newSutWith(
            UserRepository userRepository,
            ReaderRepository readerRepository,
            GenreRepository genreRepository,
            JdbcTemplate jdbcTemplate
    ) {
        return new UserBootstrapper(userRepository, readerRepository, genreRepository, jdbcTemplate);
    }

    @Test
    @DisplayName("run() inserts 8 readers + 1 librarian, creates 8 ReaderDetails and executes 8 SQL updates when missing")
    void run_creates_everything_when_missing() throws Exception {
        var userRepository = mock(UserRepository.class);
        var readerRepository = mock(ReaderRepository.class);
        var genreRepository = mock(GenreRepository.class);
        var jdbcTemplate = mock(JdbcTemplate.class);

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(readerRepository.findByReaderNumber(anyString())).thenReturn(Optional.empty());
        when(genreRepository.findByString(anyString())).thenReturn(Optional.of(new Genre("G")));

        var sut = newSutWith(userRepository, readerRepository, genreRepository, jdbcTemplate);


        sut.run();

        verify(userRepository, times(9)).save(any(User.class));

        verify(readerRepository, times(8)).save(any(ReaderDetails.class));

        verify(jdbcTemplate, times(8)).update(anyString());

        var year = LocalDate.now().getYear();
        ArgumentCaptor<String> numbers = ArgumentCaptor.forClass(String.class);
        verify(readerRepository, times(8)).findByReaderNumber(numbers.capture());
        assertThat(numbers.getAllValues())
                .containsExactlyInAnyOrder(
                        year + "/1", year + "/2", year + "/3", year + "/4",
                        year + "/5", year + "/6", year + "/7", year + "/8"
                );
    }

    @Test
    @DisplayName("run() does nothing when all users already exist")
    void run_skips_when_everything_exists() throws Exception {
        var userRepository = mock(UserRepository.class);
        var readerRepository = mock(ReaderRepository.class);
        var genreRepository = mock(GenreRepository.class);
        var jdbcTemplate = mock(JdbcTemplate.class);

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(mock(User.class)));

        var sut = newSutWith(userRepository, readerRepository, genreRepository, jdbcTemplate);


        sut.run();

        verify(userRepository, never()).save(any());
        verify(readerRepository, never()).save(any());
        verify(jdbcTemplate, never()).update(anyString());
        verify(readerRepository, never()).findByReaderNumber(anyString());
    }
}
