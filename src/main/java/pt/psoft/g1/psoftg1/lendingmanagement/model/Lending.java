package pt.psoft.g1.psoftg1.lendingmanagement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import pt.psoft.g1.psoftg1.exceptions.ConflictException;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.shared.model.EntityWithPhoto;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Entity
@Table(name = "Lending")
public class Lending extends EntityWithPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long pk;

    @Column(nullable = false, unique = true)
    @Getter
    private String lendingNumber;

    @NotNull
    @Column(nullable = false)
    @Getter
    private LocalDate startDate;

    @Column(nullable = false)
    @Getter
    private LocalDate limitDate;

    @Getter
    private LocalDate returnedDate;

    // FIX: Replaced Book Entity with direct DB columns
    // We store the ISBN and Title to maintain a reference to the book in the Books Service
    @Column(nullable = false)
    @Getter
    private String bookIsbn;

    @Column(nullable = false)
    @Getter
    private String bookTitle;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @Getter
    private ReaderDetails readerDetails;

    @Getter
    private int fineValuePerDayInCents;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @Setter
    private Fine fine;

    @Version
    @Getter
    private long version;

    @Column(length = 1024)
    @Getter
    @Setter
    private String commentary;

    protected Lending() {
    }

    // Updated Constructor to accept ISBN and Title strings instead of Book object
    public Lending(String bookIsbn, String bookTitle, ReaderDetails readerDetails, int year, int seq, LocalDate startDate, LocalDate returnedDate, int lendingDuration, int fineValuePerDayInCents) {
        if (fineValuePerDayInCents < 0) {
            throw new IllegalArgumentException("Fine value cannot be negative");
        }
        this.bookIsbn = bookIsbn;
        this.bookTitle = bookTitle;
        this.readerDetails = readerDetails;
        this.lendingNumber = year + "/" + seq;
        this.startDate = startDate;
        this.limitDate = startDate.plusDays(lendingDuration);
        this.fineValuePerDayInCents = fineValuePerDayInCents;
        this.returnedDate = returnedDate;
        
        // If created already returned (bootstrapping), check for fines immediately
        if (returnedDate != null && returnedDate.isAfter(this.limitDate)) {
            this.fine = new Fine(this);
        }
    }

    // Factory method matching the updated Bootstrapper
    public static Lending newBootstrappingLending(String bookIsbn, String bookTitle, ReaderDetails readerDetails, int year, int seq, LocalDate startDate, LocalDate returnedDate, int lendingDuration, int fineValuePerDayInCents) {
        return new Lending(bookIsbn, bookTitle, readerDetails, year, seq, startDate, returnedDate, lendingDuration, fineValuePerDayInCents);
    }

    public void setReturned(LocalDate returnedDate, String commentary) {
        if (this.returnedDate != null) {
            throw new ConflictException("Lending already returned");
        }
        this.returnedDate = returnedDate;
        this.commentary = commentary;

        if (returnedDate.isAfter(this.limitDate)) {
            this.fine = new Fine(this);
        }
    }
    
    public Optional<Fine> getFine() {
        return Optional.ofNullable(fine);
    }
    
    public int getDaysDelayed() {
        if (returnedDate != null) {
            return (int) Math.max(0, ChronoUnit.DAYS.between(limitDate, returnedDate));
        } else {
            return (int) Math.max(0, ChronoUnit.DAYS.between(limitDate, LocalDate.now()));
        }
    }

    public int getDaysUntilReturn() {
        // Implementation logic
        return 0; // placeholder
    }
    
    public int getDaysOverdue() {
        return getDaysDelayed();
    }
}