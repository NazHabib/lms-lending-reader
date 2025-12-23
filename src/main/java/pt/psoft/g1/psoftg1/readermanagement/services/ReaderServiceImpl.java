package pt.psoft.g1.psoftg1.readermanagement.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pt.psoft.g1.psoftg1.exceptions.ConflictException;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.readermanagement.api.ReaderViewAMQP;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderRepository;
import pt.psoft.g1.psoftg1.shared.repositories.ForbiddenNameRepository;
//import pt.psoft.g1.psoftg1.shared.repositories.PhotoRepository;
//import pt.psoft.g1.psoftg1.usermanagement.model.Reader;
//import pt.psoft.g1.psoftg1.usermanagement.repositories.UserRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class ReaderServiceImpl implements ReaderService {
    private final ReaderRepository readerRepo;
    private final ForbiddenNameRepository forbiddenNameRepository;


//    @Override
//    public ReaderDetails create(CreateReaderRequest request, String photoURI) {
//        if (readerRepo.findByUsername(request.getUsername()).isPresent()) {
//            throw new ConflictException("Username already exists!");
//        }
//
//        Iterable<String> words = List.of(request.getFullName().split("\\s+"));
//        for (String word : words){
//            if(!forbiddenNameRepository.findByForbiddenNameIsContained(word).isEmpty()) {
//                throw new IllegalArgumentException("Name contains a forbidden word");
//            }
//        }
//
//
//        MultipartFile photo = request.getPhoto();
//        if(photo == null && photoURI != null || photo != null && photoURI == null) {
//            request.setPhoto(null);
//        }
//
//        int count = readerRepo.getCountFromCurrentYear();
//        ReaderDetails rd = readerMapper.createReaderDetails(count+1, request, photoURI);
//
//
//        return readerRepo.save(rd);
//    }


//    @Override
//    public ReaderDetails update(final Long id, final UpdateReaderRequest request, final long desiredVersion, String photoURI){
//        final ReaderDetails readerDetails = readerRepo.findByUserId(id)
//                .orElseThrow(() -> new NotFoundException("Cannot find reader"));
//
//        MultipartFile photo = request.getPhoto();
//        if(photo == null && photoURI != null || photo != null && photoURI == null) {
//            request.setPhoto(null);
//        }
//
//        readerDetails.applyPatch(desiredVersion, request, photoURI);
//
//        return readerRepo.save(readerDetails);
//    }


    @Override
    public Optional<ReaderDetails> findByReaderNumber(String readerNumber) {
        return this.readerRepo.findByReaderNumber(readerNumber);
    }

    @Override
    public ReaderDetails create(ReaderViewAMQP readerViewAMQP) {
        if (readerRepo.findByUsername(readerViewAMQP.getUsername()).isPresent()) {
            throw new ConflictException("Username already exists!");
        }

        ReaderDetails rd = new ReaderDetails(
                readerViewAMQP.getReaderNumber(),
                readerViewAMQP.getBirthDate(),
                readerViewAMQP.getPhoneNumber(),
                readerViewAMQP.isGdpr(),
                readerViewAMQP.isMarketing(),
                readerViewAMQP.isThirdParty(),
                readerViewAMQP.getPhotoURI(),
                readerViewAMQP.getUsername());

        return readerRepo.save(rd);
    }

    @Override
    public ReaderDetails update(ReaderViewAMQP readerViewAMQP) {
        final ReaderDetails readerDetails = readerRepo.findByUserId(readerViewAMQP.getUserId())
        .orElseThrow(() -> new NotFoundException("Cannot find reader"));

        readerDetails.applyPatch(readerViewAMQP);

        return readerRepo.save(readerDetails);
    }

    @Override
    public Optional<ReaderDetails> findByUsername(final String username) {
        return this.readerRepo.findByUsername(username);
    }


    @Override
    public Iterable<ReaderDetails> findAll() {
        return this.readerRepo.findAll();
    }
}
