package pt.psoft.g1.psoftg1.readermanagement.infraestructure.repositories.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.util.StringUtils;
import pt.psoft.g1.psoftg1.readermanagement.services.SearchReadersQuery;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderRepository;

//import org.springframework.data.domain.Pageable;
//import pt.psoft.g1.psoftg1.readermanagement.services.ReaderBookCountDTO;
//import pt.psoft.g1.psoftg1.usermanagement.model.User;
//
//import java.time.LocalDate;
//import java.util.ArrayList;
//import java.util.List;
import java.util.Optional;


public interface SpringDataReaderRepositoryImpl extends ReaderRepository, CrudRepository<ReaderDetails, Long> {
    @Override
    @Query("SELECT r " +
            "FROM ReaderDetails r " +
            "WHERE r.readerNumber.readerNumber = :readerNumber")
    Optional<ReaderDetails> findByReaderNumber(@Param("readerNumber") @NotNull String readerNumber);

    @Override
    @Query("SELECT r " +
            "FROM ReaderDetails r " +
            "WHERE r.username = :username")
    Optional<ReaderDetails> findByUsername(@Param("username") @NotNull String username);

    @Override
    @Query("SELECT r " +
            "FROM ReaderDetails r " +
            "WHERE r.userId = :userId")
    Optional<ReaderDetails> findByUserId(@Param("userId") @NotNull Long userId);

//
//    @Override
//    @Query("SELECT COUNT (rd) " +
//            "FROM ReaderDetails rd " +
//            "JOIN User u ON rd.reader.id = u.id " +
//            "WHERE YEAR(u.createdAt) = YEAR(CURRENT_DATE)")
//    int getCountFromCurrentYear();
}


