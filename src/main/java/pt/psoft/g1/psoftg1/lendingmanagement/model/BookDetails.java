package pt.psoft.g1.psoftg1.lendingmanagement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pt.psoft.g1.psoftg1.shared.model.EntityWithPhoto;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "BOOK_DETAILS_LENDING_CONTEXT")
public class BookDetails extends EntityWithPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long pk;

    @Version
    private Long version;

    @NotNull
    @Column(nullable = false, unique = true)
    private String isbn;

    @NotNull
    @Column(nullable = false)
    private String title;

    @Column(nullable = true)
    private String genre;

    public BookDetails(String isbn, String title, String genre) {
        this.isbn = isbn;
        this.title = title;
        this.genre = genre;
    }
}