package pt.psoft.g1.psoftg1.readermanagement.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import pt.psoft.g1.psoftg1.readermanagement.api.ReaderViewAMQP;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.shared.services.Page;

/**
 *
 */
public interface ReaderService {
 //   ReaderDetails create(CreateReaderRequest request, String photoURI);
//    ReaderDetails update(Long id, UpdateReaderRequest request, long desireVersion, String photoURI);
    ReaderDetails create(ReaderViewAMQP readerViewAMQP);
    ReaderDetails update(ReaderViewAMQP readerViewAMQP);
    Optional<ReaderDetails> findByUsername(final String username);
    Optional<ReaderDetails> findByReaderNumber(String readerNumber);
    Iterable<ReaderDetails> findAll();
}
