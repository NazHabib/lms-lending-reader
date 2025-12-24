package pt.psoft.g1.psoftg1.lendingmanagement.repositories;

import org.springframework.data.repository.CrudRepository;
import pt.psoft.g1.psoftg1.lendingmanagement.model.BookDetails;

import java.util.Optional;

public interface BookDetailsRepository extends CrudRepository<BookDetails, Long> {
    Optional<BookDetails> findByIsbn(String isbn);
}