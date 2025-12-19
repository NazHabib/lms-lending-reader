package pt.psoft.g1.psoftg1.bootstrapping;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.LendingRepository;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderRepository;
import pt.psoft.g1.psoftg1.shared.services.ForbiddenNameService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Profile("bootstrap")
@PropertySource({"classpath:config/library.properties"})
@Order(2)
public class Bootstrapper implements CommandLineRunner {
    @Value("${lendingDurationInDays}")
    private int lendingDurationInDays;
    @Value("${fineValuePerDayInCents}")
    private int fineValuePerDayInCents;

    private final LendingRepository lendingRepository;
    private final ReaderRepository readerRepository;
    private final ForbiddenNameService forbiddenNameService;

    // Estrutura auxiliar local para representar os dados mínimos do livro necessários para o empréstimo
    private record BookData(String isbn, String title) {}

    @Override
    @Transactional
    public void run(final String... args) {
        loadForbiddenNames();
        createLendings();
    }

    protected void loadForbiddenNames() {
        String fileName = "forbiddenNames.txt";
        forbiddenNameService.loadDataFromFile(fileName);
    }

    private void createLendings() {
        // Definição local dos livros (apenas ISBN e Título) pois a entidade Book não existe neste serviço
        List<BookData> books = new ArrayList<>();
        books.add(new BookData("9789720706386", "O País das Pessoas de Pernas Para o Ar"));
        books.add(new BookData("9789723716160", "Como se Desenha Uma Casa"));
        books.add(new BookData("9789895612864", "C e Algoritmos"));
        books.add(new BookData("9782722203402", "Introdução ao Desenvolvimento Moderno para a Web"));
        books.add(new BookData("9789722328296", "O Principezinho"));
        books.add(new BookData("9789895702756", "A Criada Está a Ver"));
        books.add(new BookData("9789897776090", "O Hobbit"));
        books.add(new BookData("9789896379636", "Histórias de Vigaristas e Canalhas"));
        books.add(new BookData("9789896378905", "Histórias de Aventureiros e Patifes"));
        books.add(new BookData("9789896375225", "Windhaven"));

        // Recuperar leitores existentes (criados pelo UserBootstrapper)
        List<ReaderDetails> readers = new ArrayList<>();
        // Nota: Assume-se que o UserBootstrapper já correu. 
        // Se apenas o leitor 2024/1 foi criado, apenas esse será usado ou os loops abaixo devem ser ajustados.
        // Aqui tentamos carregar os leitores esperados para manter a lógica original o máximo possível.
        readerRepository.findByReaderNumber("2024/1").ifPresent(readers::add);
        readerRepository.findByReaderNumber("2024/2").ifPresent(readers::add);
        readerRepository.findByReaderNumber("2024/3").ifPresent(readers::add);
        readerRepository.findByReaderNumber("2024/4").ifPresent(readers::add);
        readerRepository.findByReaderNumber("2024/5").ifPresent(readers::add);
        readerRepository.findByReaderNumber("2024/6").ifPresent(readers::add);

        if (readers.isEmpty()) {
            return; // Se não houver leitores, não cria empréstimos
        }

        LocalDate startDate;
        LocalDate returnedDate;
        Lending lending;
        int seq = 0;

        // A lógica de distribuição dos leitores (readers.get(index)) foi mantida, 
        // mas protegida com módulo (%) para evitar IndexOutOfBounds se houver poucos leitores.

        // Lendings 1 through 3 (late, returned)
        for (int i = 0; i < 3; i++) {
            ++seq;
            if (lendingRepository.findByLendingNumber("2024/" + seq).isEmpty()) {
                startDate = LocalDate.of(2024, 1, 31 - i);
                returnedDate = LocalDate.of(2024, 2, 15 + i);
                // Assume-se que o construtor/factory de Lending foi adaptado para receber ISBN e Título
                lending = Lending.newBootstrappingLending(
                        books.get(i).isbn(),
                        books.get(i).title(),
                        readers.get((i * 2) % readers.size()),
                        2024, seq, startDate, returnedDate, lendingDurationInDays, fineValuePerDayInCents);
                lendingRepository.save(lending);
            }
        }

        // Lendings 4 through 6 (overdue, not returned)
        for (int i = 0; i < 3; i++) {
            ++seq;
            if (lendingRepository.findByLendingNumber("2024/" + seq).isEmpty()) {
                startDate = LocalDate.of(2024, 3, 25 + i);
                lending = Lending.newBootstrappingLending(
                        books.get(1 + i).isbn(),
                        books.get(1 + i).title(),
                        readers.get((1 + i * 2) % readers.size()),
                        2024, seq, startDate, null, lendingDurationInDays, fineValuePerDayInCents);
                lendingRepository.save(lending);
            }
        }

        // Lendings 7 through 9 (late, overdue, not returned)
        for (int i = 0; i < 3; i++) {
            ++seq;
            if (lendingRepository.findByLendingNumber("2024/" + seq).isEmpty()) {
                startDate = LocalDate.of(2024, 4, (1 + 2 * i));
                lending = Lending.newBootstrappingLending(
                        books.get(3 / (i + 1)).isbn(),
                        books.get(3 / (i + 1)).title(),
                        readers.get((i * 2) % readers.size()),
                        2024, seq, startDate, null, lendingDurationInDays, fineValuePerDayInCents);
                lendingRepository.save(lending);
            }
        }

        // Lendings 10 through 12 (returned)
        for (int i = 0; i < 3; i++) {
            ++seq;
            if (lendingRepository.findByLendingNumber("2024/" + seq).isEmpty()) {
                startDate = LocalDate.of(2024, 5, (i + 1));
                returnedDate = LocalDate.of(2024, 5, (i + 2));
                lending = Lending.newBootstrappingLending(
                        books.get(3 - i).isbn(),
                        books.get(3 - i).title(),
                        readers.get((1 + i * 2) % readers.size()),
                        2024, seq, startDate, returnedDate, lendingDurationInDays, fineValuePerDayInCents);
                lendingRepository.save(lending);
            }
        }

        // Lendings 13 through 18 (returned)
        for (int i = 0; i < 6; i++) {
            ++seq;
            if (lendingRepository.findByLendingNumber("2024/" + seq).isEmpty()) {
                startDate = LocalDate.of(2024, 5, (i + 2));
                returnedDate = LocalDate.of(2024, 5, (i + 2 * 2));
                lending = Lending.newBootstrappingLending(
                        books.get(i).isbn(),
                        books.get(i).title(),
                        readers.get(i % readers.size()),
                        2024, seq, startDate, returnedDate, lendingDurationInDays, fineValuePerDayInCents);
                lendingRepository.save(lending);
            }
        }

        // Lendings 19 through 23 (returned)
        for (int i = 0; i < 6; i++) { // Nota: O loop original vai até 6, mas o comentário diz 19-23 (5 itens)
            ++seq;
            if (lendingRepository.findByLendingNumber("2024/" + seq).isEmpty()) {
                startDate = LocalDate.of(2024, 5, (i + 8));
                returnedDate = LocalDate.of(2024, 5, (2 * i + 8));
                lending = Lending.newBootstrappingLending(
                        books.get(i).isbn(),
                        books.get(i).title(),
                        readers.get((1 + i % 4) % readers.size()),
                        2024, seq, startDate, returnedDate, lendingDurationInDays, fineValuePerDayInCents);
                lendingRepository.save(lending);
            }
        }

        // Lendings 24 through 29 (returned)
        for (int i = 0; i < 6; i++) {
            ++seq;
            if (lendingRepository.findByLendingNumber("2024/" + seq).isEmpty()) {
                startDate = LocalDate.of(2024, 5, (i + 18));
                returnedDate = LocalDate.of(2024, 5, (2 * i + 18));
                lending = Lending.newBootstrappingLending(
                        books.get(i).isbn(),
                        books.get(i).title(),
                        readers.get((i % 2 + 2) % readers.size()),
                        2024, seq, startDate, returnedDate, lendingDurationInDays, fineValuePerDayInCents);
                lendingRepository.save(lending);
            }
        }

        // Lendings 30 through 35 (not returned, not overdue)
        for (int i = 0; i < 6; i++) {
            ++seq;
            if (lendingRepository.findByLendingNumber("2024/" + seq).isEmpty()) {
                startDate = LocalDate.of(2024, 6, (i / 3 + 1));
                lending = Lending.newBootstrappingLending(
                        books.get(i).isbn(),
                        books.get(i).title(),
                        readers.get((i % 2 + 3) % readers.size()),
                        2024, seq, startDate, null, lendingDurationInDays, fineValuePerDayInCents);
                lendingRepository.save(lending);
            }
        }

        // Lendings 36 through 45 (not returned, not overdue)
        for (int i = 0; i < 10; i++) {
            ++seq;
            if (lendingRepository.findByLendingNumber("2024/" + seq).isEmpty()) {
                startDate = LocalDate.of(2024, 6, (2 + i / 4));
                lending = Lending.newBootstrappingLending(
                        books.get(i % books.size()).isbn(),
                        books.get(i % books.size()).title(),
                        readers.get((4 - i % 4) % readers.size()),
                        2024, seq, startDate, null, lendingDurationInDays, fineValuePerDayInCents);
                lendingRepository.save(lending);
            }
        }
    }
}