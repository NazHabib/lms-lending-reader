package pt.psoft.g1.psoftg1.readermanagement.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.services.SearchReadersQuery;
import pt.psoft.g1.psoftg1.shared.services.Page;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReaderRepository extends CrudRepository<ReaderDetails, Long> {
    Optional<ReaderDetails> findByReaderNumber(String readerNumber);
    Optional<ReaderDetails> findByUsername(String username);
    
    // Custom search method implemented in SpringDataReaderRepositoryImpl or via naming convention
    List<ReaderDetails> searchReaders(Page page, SearchReadersQuery query);
}