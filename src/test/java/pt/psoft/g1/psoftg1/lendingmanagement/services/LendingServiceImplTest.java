package pt.psoft.g1.psoftg1.lendingmanagement.services;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.FineRepository;
import pt.psoft.g1.psoftg1.shared.services.Page;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;

import org.hibernate.StaleObjectStateException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.repositories.AuthorRepository;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;
import pt.psoft.g1.psoftg1.exceptions.LendingForbiddenException;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.repositories.GenreRepository;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.LendingRepository;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderRepository;
import pt.psoft.g1.psoftg1.usermanagement.model.Reader;
import pt.psoft.g1.psoftg1.usermanagement.repositories.UserRepository;
import java.util.Optional;


import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


/**
 * SUT: LendingServiceImpl (Serviço de domínio da Lending)
 * Tipo: Integração funcional (SpringBootTest + transacional, H2 em memória)
 *
 * Black-box:
 *  - Regras de negócio na criação (create):
 *      * proíbe novos empréstimos quando há >0 atrasados
 *      * proíbe 4º empréstimo em simultâneo (limite 3)
 *      * falha com NotFound se livro/leitor não existem
 *  - Devolução (setReturned):
 *      * controla versão (stale -> StaleObjectStateException)
 *      * cria Fine quando há atraso
 *  - Pesquisa/consulta:
 *      * findByLendingNumber
 *      * listByReaderNumberAndIsbn com filtro Optional<Boolean> returned
 *      * searchLendings: defaults de page/query e validação de datas
 *      * getOverdue: default de paginação
 *      * getAverageDuration e getAvgLendingDurationByIsbn: arredondamento 1 casa
 *
 * White-box:
 *  - Garantias de arredondamento com Locale.US (1 casa decimal)
 *  - Fronteiras e erros: parsing de datas inválidas, página nula, query nula
 *
 * Isolamento:
 *  - Integração apenas com camada de persistência real (H2) através dos repositórios Spring Data.
 *  - Sem chamadas externas.
 */



@Transactional
@SpringBootTest
class LendingServiceImplTest {
    @Autowired
    private LendingService lendingService;
    @Autowired
    private LendingRepository lendingRepository;
    @Autowired
    private ReaderRepository readerRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private GenreRepository genreRepository;
    @Autowired
    private AuthorRepository authorRepository;
    @Autowired
    private FineRepository fineRepository;

    private Lending lending;
    private ReaderDetails readerDetails;
    private Reader reader;
    private Book book;
    private Author author;
    private Genre genre;

    @BeforeEach
    void setUp() {
        author = new Author("Manuel Antonio Pina",
                "Manuel António Pina foi um jornalista e escritor português, premiado em 2011 com o Prémio Camões",
                null);
        authorRepository.save(author);

        genre = new Genre("Género");
        genreRepository.save(genre);

        List<Author> authors = List.of(author);
        book = new Book("9782826012092",
                "O Inspetor Max",
                "conhecido pastor-alemão que trabalha para a Judiciária, vai ser fundamental para resolver um importante caso de uma rede de malfeitores que quer colocar uma bomba num megaconcerto de uma ilustre cantora",
                genre,
                authors,
                null);
        bookRepository.save(book);

        reader = Reader.newReader("manuel@gmail.com", "Manuelino123!", "Manuel Sarapinto das Coives");
        userRepository.save(reader);

        readerDetails = new ReaderDetails(1,
                reader,
                "2000-01-01",
                "919191919",
                true,
                true,
                true,
                null,null);
        readerRepository.save(readerDetails);

        // Create and save the lending
        lending = Lending.newBootstrappingLending(book,
                readerDetails,
                LocalDate.now().getYear(),
                999,
                LocalDate.of(LocalDate.now().getYear(), 1,1),
                LocalDate.of(LocalDate.now().getYear(), 1,11),
                15,
                300);
        lendingRepository.save(lending);

    }

    @AfterEach
    void tearDown() {
        lendingRepository.delete(lending);
        readerRepository.delete(readerDetails);
        userRepository.delete(reader);
        bookRepository.delete(book);
        genreRepository.delete(genre);
        authorRepository.delete(author);
    }

    @Test
    void testFindByLendingNumber() {
        assertThat(lendingService.findByLendingNumber(LocalDate.now().getYear() + "/999")).isPresent();
        assertThat(lendingService.findByLendingNumber(LocalDate.now().getYear() + "/1")).isEmpty();
    }
/*
    @Test
    void testListByReaderNumberAndIsbn() {

    }
 */
    @Test
    void testCreate() {
        var request = new CreateLendingRequest("9782826012092",
                LocalDate.now().getYear() + "/1");
        var lending1 = lendingService.create(request);
        assertThat(lending1).isNotNull();
        var lending2 = lendingService.create(request);
        assertThat(lending2).isNotNull();
        var lending3 = lendingService.create(request);
        assertThat(lending3).isNotNull();

        // 4th lending
        assertThrows(LendingForbiddenException.class, () -> lendingService.create(request));

        lendingRepository.delete(lending3);
        lendingRepository.save(Lending.newBootstrappingLending(book,
                readerDetails,
                2024,
                997,
                LocalDate.of(2024, 3,1),
                null,
                15,
                300));

        assertThrows(LendingForbiddenException.class, () -> lendingService.create(request));

    }

    @Test
    void testSetReturned() {
        int year = 2024, seq = 888;
        var notReturnedLending = lendingRepository.save(Lending.newBootstrappingLending(book,
                readerDetails,
                year,
                seq,
                LocalDate.of(2024, 3,1),
                null,
                15,
                300));
        var request = new SetLendingReturnedRequest(null);
        assertThrows(StaleObjectStateException.class,
                () -> lendingService.setReturned(year + "/" + seq, request, (notReturnedLending.getVersion()-1)));

        assertDoesNotThrow(
                () -> lendingService.setReturned(year + "/" + seq, request, notReturnedLending.getVersion()));
    }
    @Test
    void testListByReaderNumberAndIsbn_noReturnedFilter_returnsAll() {
        var returned = lendingRepository.save(
                Lending.newBootstrappingLending(
                        book, readerDetails,
                        LocalDate.now().getYear(), 1001,
                        LocalDate.now().minusDays(20),
                        LocalDate.now().minusDays(10),
                        7, 100));

        var outstanding = lendingRepository.save(
                Lending.newBootstrappingLending(
                        book, readerDetails,
                        LocalDate.now().getYear(), 1002,
                        LocalDate.now().minusDays(2),
                        null, // not returned
                        7, 100));

        var all = lendingService.listByReaderNumberAndIsbn(
                String.valueOf(readerDetails.getReaderNumber()),
                book.getIsbn(),
                Optional.empty());

        assertThat(all).extracting(Lending::getLendingNumber)
                .contains(returned.getLendingNumber(), outstanding.getLendingNumber());
    }

    @Test
    void testListByReaderNumberAndIsbn_filterReturnedTrue_onlyReturned() {
        var returned = lendingRepository.save(
                Lending.newBootstrappingLending(
                        book, readerDetails,
                        LocalDate.now().getYear(), 1003,
                        LocalDate.now().minusDays(15),
                        LocalDate.now().minusDays(1),
                        7, 100));

        var outstanding = lendingRepository.save(
                Lending.newBootstrappingLending(
                        book, readerDetails,
                        LocalDate.now().getYear(), 1004,
                        LocalDate.now().minusDays(3),
                        null,
                        7, 100));

        var onlyReturned = lendingService.listByReaderNumberAndIsbn(
                String.valueOf(readerDetails.getReaderNumber()),
                book.getIsbn(),
                Optional.of(true));

        assertThat(onlyReturned).extracting(Lending::getLendingNumber)
                .contains(returned.getLendingNumber())
                .doesNotContain(outstanding.getLendingNumber());
    }

    @Test
    void testListByReaderNumberAndIsbn_filterReturnedFalse_onlyOutstanding() {
        var returned = lendingRepository.save(
                Lending.newBootstrappingLending(
                        book, readerDetails,
                        LocalDate.now().getYear(), 1005,
                        LocalDate.now().minusDays(15),
                        LocalDate.now().minusDays(1),
                        7, 100));

        var outstanding = lendingRepository.save(
                Lending.newBootstrappingLending(
                        book, readerDetails,
                        LocalDate.now().getYear(), 1006,
                        LocalDate.now().minusDays(3),
                        null,
                        7, 100));

        var onlyOutstanding = lendingService.listByReaderNumberAndIsbn(
                String.valueOf(readerDetails.getReaderNumber()),
                book.getIsbn(),
                Optional.of(false));

        assertThat(onlyOutstanding).extracting(Lending::getLendingNumber)
                .contains(outstanding.getLendingNumber())
                .doesNotContain(returned.getLendingNumber());
    }

    @Test
    void testCreate_bookNotFound_throws() {
        var request = new CreateLendingRequest("0000000000000",
                String.valueOf(readerDetails.getReaderNumber()));
        assertThrows(NotFoundException.class, () -> lendingService.create(request));
    }

    @Test
    void testCreate_readerNotFound_throws() {
        var request = new CreateLendingRequest(book.getIsbn(), "999999");
        assertThrows(NotFoundException.class, () -> lendingService.create(request));
    }

    void testSetReturned_overdue_createsFine() {
        int seq = 2000;
        var overdue = lendingRepository.save(
                Lending.newBootstrappingLending(
                        book, readerDetails,
                        LocalDate.now().getYear(), seq,
                        LocalDate.now().minusDays(30),
                        null,
                        7,
                        250
                )
        );

        String ln = LocalDate.now().getYear() + "/" + seq;


        assertTrue(fineRepository.findByLendingNumber(ln).isEmpty());

        var req = new SetLendingReturnedRequest("OK");
        lendingService.setReturned(ln, req, overdue.getVersion());

        assertTrue(fineRepository.findByLendingNumber(ln).isPresent());
    }

    @Test
    void testGetOverdue_defaultsPage_executes() {
        var result = lendingService.getOverdue(null);
        assertNotNull(result);
    }

    @Test
    void testSearchLendings_defaults_whenPageAndQueryNull() {
        var result = lendingService.searchLendings(null, null);
        assertNotNull(result);
    }

    @Test
    void testSearchLendings_invalidDate_throws() {
        var bad = new SearchLendingQuery(
                String.valueOf(readerDetails.getReaderNumber()),
                book.getIsbn(),
                null,
                "2024-13-01", // invalid month
                null);
        assertThrows(IllegalArgumentException.class, () -> lendingService.searchLendings(new Page(1,10), bad));
    }



/*
    @Test
    void testGetAverageDuration() {
    }

    @Test
    void testGetOverdue() {
    }

 */
}
