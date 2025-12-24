package pt.psoft.g1.psoftg1.lendingmanagement.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pt.psoft.g1.psoftg1.lendingmanagement.model.BookDetails;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.BookDetailsRepository;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BookEventRabbitmqReceiver {

    private final BookDetailsRepository bookDetailsRepository;

    @RabbitListener(queues = "#{autoDeleteQueue_Book_Created.name}")
    @Transactional
    public void receiveBookCreated(Message msg) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonReceived = new String(msg.getBody(), StandardCharsets.UTF_8);
            BookViewAMQP bookViewAMQP = objectMapper.readValue(jsonReceived, BookViewAMQP.class);

            System.out.println(" [x] Received Book Created by AMQP: " + bookViewAMQP.getIsbn());

            if (bookDetailsRepository.findByIsbn(bookViewAMQP.getIsbn()).isEmpty()) {
                BookDetails book = new BookDetails(
                        bookViewAMQP.getIsbn(),
                        bookViewAMQP.getTitle(),
                        bookViewAMQP.getGenre()
                );
                bookDetailsRepository.save(book);
                System.out.println(" [x] New local BookDetails inserted from AMQP.");
            } else {
                System.out.println(" [x] Book already exists locally. No need to store it.");
            }
        } catch (Exception ex) {
            System.out.println(" [x] Exception receiving book event from AMQP: '" + ex.getMessage() + "'");
        }
    }

    @RabbitListener(queues = "#{autoDeleteQueue_Book_Updated.name}")
    @Transactional
    public void receiveBookUpdated(Message msg) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonReceived = new String(msg.getBody(), StandardCharsets.UTF_8);
            BookViewAMQP bookViewAMQP = objectMapper.readValue(jsonReceived, BookViewAMQP.class);

            System.out.println(" [x] Received Book Updated by AMQP: " + bookViewAMQP.getIsbn());

            Optional<BookDetails> bookDetailsOpt = bookDetailsRepository.findByIsbn(bookViewAMQP.getIsbn());
            
            if (bookDetailsOpt.isPresent()) {
                BookDetails book = bookDetailsOpt.get();
                book.setTitle(bookViewAMQP.getTitle());
                book.setGenre(bookViewAMQP.getGenre());
                // We don't track version or description for lending purposes, just Title/ISBN usually
                bookDetailsRepository.save(book);
                System.out.println(" [x] Local BookDetails updated from AMQP.");
            } else {
                // Optional: If we receive an update for a book we don't have, we could create it
                BookDetails book = new BookDetails(
                        bookViewAMQP.getIsbn(),
                        bookViewAMQP.getTitle(),
                        bookViewAMQP.getGenre()
                );
                bookDetailsRepository.save(book);
                System.out.println(" [x] Book did not exist, created from Update event.");
            }
        } catch (Exception ex) {
            System.out.println(" [x] Exception receiving book event from AMQP: '" + ex.getMessage() + "'");
        }
    }

    // Local DTO to handle the incoming JSON.
    // We ignore fields we don't care about (like description, authors list, etc.)
    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class BookViewAMQP {
        private String isbn;
        private String title;
        private String genre;
        // private String description; // Ignored/Not needed for Lending logic
    }
}