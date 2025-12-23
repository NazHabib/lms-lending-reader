package pt.psoft.g1.psoftg1.lendingmanagement.infrastructure.repositories.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.util.StringUtils;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.LendingRepository;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.shared.services.Page;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface SpringDataLendingRepository extends LendingRepository, LendingRepoCustom, CrudRepository<Lending, Long> {
    @Override
    @Query("SELECT l " +
            "FROM Lending l " +
            "WHERE l.lendingNumber = :lendingNumber")
    Optional<Lending> findByLendingNumber(@Param("lendingNumber") String lendingNumber);

    @Override
    @Query("SELECT l " +
            "FROM Lending l " +
            "JOIN l.readerDetails r " +
            "WHERE l.bookIsbn = :isbn " +
            "AND r.readerNumber.readerNumber = :readerNumber ")
    List<Lending> listByReaderNumberAndIsbn(@Param("readerNumber") String readerNumber, @Param("isbn") String isbn);

    @Override
    @Query("SELECT COUNT (l) " +
            "FROM Lending l " +
            "WHERE YEAR(l.startDate) = YEAR(CURRENT_DATE)")
    int getCountFromCurrentYear();

    @Override
    @Query("SELECT l " +
            "FROM Lending l " +
            "JOIN l.readerDetails r " +
            "WHERE r.readerNumber.readerNumber = :readerNumber " +
            "AND l.returnedDate IS NULL")
    List<Lending> listOutstandingByReaderNumber(@Param("readerNumber") String readerNumber);

    @Override
    @Query(value =
            "SELECT AVG(DATEDIFF('DAY', l.start_date, l.returned_date)) " +
            "FROM Lending l " +
            "WHERE l.returned_date IS NOT NULL"
            , nativeQuery = true)
    Double getAverageDuration();

    @Override
    @Query(value =
            "SELECT AVG(DATEDIFF('DAY', l.start_date, l.returned_date)) " +
                    "FROM Lending l " +
                    "WHERE l.book_isbn = :isbn " +
                    "AND l.returned_date IS NOT NULL"
            , nativeQuery = true)
    Double getAvgLendingDurationByIsbn(@Param("isbn") String isbn);

    // Implement default methods required by the LendingRepository interface if they are not covered by Spring Data magic
    // Note: If you have default methods in the interface, you don't need to implement them here explicitly unless overriding.
    // However, for strict compilation if using the previous interface definition:

    @Override
    @Query("SELECT l FROM Lending l WHERE l.readerDetails = :readerDetails AND l.bookIsbn = :isbn " +
            "AND (:returned IS NULL " +
            "OR (:returned = true AND l.returnedDate IS NOT NULL) " +
            "OR (:returned = false AND l.returnedDate IS NULL))")
    List<Lending> listByReaderDetailsAndIsbnRaw(@Param("readerDetails") ReaderDetails readerDetails,
                                                @Param("isbn") String isbn,
                                                @Param("returned") Boolean returned);

    @Override
    @Query("SELECT l FROM Lending l WHERE l.readerDetails = :readerDetails AND l.returnedDate IS NULL AND l.limitDate < CURRENT_DATE")
    List<Lending> listOverdue(@Param("readerDetails") ReaderDetails readerDetails);

    @Override
    @Query("SELECT count(l) FROM Lending l WHERE l.readerDetails = :readerDetails AND l.returnedDate IS NULL")
    long countOutstanding(@Param("readerDetails") ReaderDetails readerDetails);

    @Override
    @Query("SELECT l FROM Lending l WHERE l.returnedDate IS NULL")
    List<Lending> listOutstanding();

    @Override
    @Query("SELECT l FROM Lending l WHERE " +
            "(:readerNumber IS NULL OR l.readerDetails.readerNumber.readerNumber = :readerNumber) AND " +
            "(:isbn IS NULL OR l.bookIsbn = :isbn) AND " +
            "(:startDate IS NULL OR l.startDate >= :startDate) AND " +
            "(:endDate IS NULL OR l.startDate <= :endDate) AND " +
            "(:returned IS NULL OR (:returned = true AND l.returnedDate IS NOT NULL) OR (:returned = false AND l.returnedDate IS NULL))")
    List<Lending> searchLendingsRaw(org.springframework.data.domain.Pageable pageable,
                                    @Param("readerNumber") String readerNumber,
                                    @Param("isbn") String isbn,
                                    @Param("returned") Boolean returned,
                                    @Param("startDate") LocalDate startDate,
                                    @Param("endDate") LocalDate endDate);

    @Override
    @Query("SELECT l FROM Lending l WHERE l.returnedDate IS NULL AND l.limitDate < CURRENT_DATE")
    List<Lending> getOverdueRaw(org.springframework.data.domain.Pageable pageable);
}

interface LendingRepoCustom {
    List<Lending> getOverdue(Page page);
    List<Lending> searchLendings(Page page, String readerNumber, String isbn, Boolean returned, LocalDate startDate, LocalDate endDate);
}

@RequiredArgsConstructor
class LendingRepoCustomImpl implements LendingRepoCustom {
    private final EntityManager em;

    @Override
    public List<Lending> getOverdue(Page page) {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<Lending> cq = cb.createQuery(Lending.class);
        final Root<Lending> root = cq.from(Lending.class);
        cq.select(root);

        final List<Predicate> where = new ArrayList<>();

        // Select overdue lendings where returnedDate is null and limitDate is before the current date
        where.add(cb.isNull(root.get("returnedDate")));
        where.add(cb.lessThan(root.get("limitDate"), LocalDate.now()));

        cq.where(where.toArray(new Predicate[0]));
        cq.orderBy(cb.asc(root.get("limitDate")));

        final TypedQuery<Lending> q = em.createQuery(cq);
        q.setFirstResult((page.getNumber() - 1) * page.getLimit());
        q.setMaxResults(page.getLimit());

        return q.getResultList();
    }

    @Override
    public List<Lending> searchLendings(Page page, String readerNumber, String isbn, Boolean returned, LocalDate startDate, LocalDate endDate) {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<Lending> cq = cb.createQuery(Lending.class);
        final Root<Lending> lendingRoot = cq.from(Lending.class);
        
        // Removed Join to Book entity
        final Join<Lending, ReaderDetails> readerDetailsJoin = lendingRoot.join("readerDetails");
        cq.select(lendingRoot);

        final List<Predicate> where = new ArrayList<>();

        if (StringUtils.hasText(readerNumber))
            where.add(cb.like(readerDetailsJoin.get("readerNumber").get("readerNumber"), readerNumber));
        
        if (StringUtils.hasText(isbn))
            // Changed to use the string field directly
            where.add(cb.like(lendingRoot.get("bookIsbn"), isbn));
            
        if (returned != null) {
            if (returned) {
                where.add(cb.isNotNull(lendingRoot.get("returnedDate")));
            } else {
                where.add(cb.isNull(lendingRoot.get("returnedDate")));
            }
        }
        if (startDate != null)
            where.add(cb.greaterThanOrEqualTo(lendingRoot.get("startDate"), startDate));
        if (endDate != null)
            where.add(cb.lessThanOrEqualTo(lendingRoot.get("startDate"), endDate));

        cq.where(where.toArray(new Predicate[0]));
        cq.orderBy(cb.asc(lendingRoot.get("lendingNumber")));

        final TypedQuery<Lending> q = em.createQuery(cq);
        q.setFirstResult((page.getNumber() - 1) * page.getLimit());
        q.setMaxResults(page.getLimit());

        return q.getResultList();
    }
}