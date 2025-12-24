package pt.psoft.g1.psoftg1.readermanagement.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pt.psoft.g1.psoftg1.exceptions.ConflictException;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.readermanagement.api.ReaderViewAMQP;
import pt.psoft.g1.psoftg1.readermanagement.model.*;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderRepository;
import pt.psoft.g1.psoftg1.shared.services.Page;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReaderServiceImpl implements ReaderService {
    private final ReaderRepository readerRepository;
    private final ReaderMapper readerMapper;

    @Override
    public ReaderDetails create(CreateReaderRequest request) {
        if (readerRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new ConflictException("Username already exists");
        }

        // Fix: getGdprConsent() etc are now explicit in Request class
        boolean gdpr = Boolean.TRUE.equals(request.getGdprConsent());
        boolean marketing = Boolean.TRUE.equals(request.getMarketingConsent());
        boolean thirdParty = Boolean.TRUE.equals(request.getThirdPartySharingConsent());

        ReaderDetails reader = new ReaderDetails(
                new ReaderNumber(readerRepository.count() + 1), // Check if count() returns long
                new BirthDate(request.getBirthDate()),
                new PhoneNumber(request.getPhoneNumber()),
                request.getUsername(),
                request.getFullName(),
                gdpr,
                marketing,
                thirdParty,
                request.getPhoto(),
                request.getInterestList() != null ? request.getInterestList() : Collections.emptyList()
        );

        return readerRepository.save(reader);
    }

    @Override
    public Optional<ReaderDetails> findByReaderNumber(String readerNumber) {
        return readerRepository.findByReaderNumber(readerNumber);
    }

    @Override
    public Optional<ReaderDetails> findByUsername(String username) {
        return readerRepository.findByUsername(username);
    }

    @Override
    public List<ReaderDetails> searchReaders(Page page, SearchReadersQuery query) {
        if(page == null) page = new Page(1, 10);
        return readerRepository.searchReaders(page, query);
    }

    @Override
    public List<ReaderDetails> findAll() {
        return (List<ReaderDetails>) readerRepository.findAll();
    }

    // Implementing the missing update method
    @Override
    public ReaderDetails update(ReaderViewAMQP view) {
        ReaderDetails reader = readerRepository.findByUsername(view.getUsername())
                .orElseThrow(() -> new NotFoundException("Reader not found"));
        
        // Manual update or via mapper
        // reader.setFullName(view.getFullName());
        // ... other fields
        
        return readerRepository.save(reader);
    }
}