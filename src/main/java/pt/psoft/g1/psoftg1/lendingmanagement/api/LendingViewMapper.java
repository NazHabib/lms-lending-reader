package pt.psoft.g1.psoftg1.lendingmanagement.api;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.shared.api.MapperInterface;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Brief guides:
 * <a href="https://www.baeldung.com/mapstruct">https://www.baeldung.com/mapstruct</a>
 * <p>
 * <a href="https://medium.com/@susantamon/mapstruct-a-comprehensive-guide-in-spring-boot-context-1e7202da033e">https://medium.com/@susantamon/mapstruct-a-comprehensive-guide-in-spring-boot-context-1e7202da033e</a>
 * */
@Mapper(componentModel = "spring")
public abstract class LendingViewMapper extends MapperInterface {

    @Mapping(target = "lendingNumber", source = "lendingNumber")
    @Mapping(target = "bookTitle", source = "bookTitle")
    // Assuming getFine() returns Optional<Fine> and Fine has getCents() or similar. 
    // If Lending.java was updated as per previous steps, this expression might need adjustment to:
    // expression = "java(lending.getFine().map(f -> f.getCents()).orElse(null))"
    // Keeping as is per request, but verify your Lending/Fine models.
    @Mapping(target = "fineValueInCents", expression = "java(lending.getFine().map(pt.psoft.g1.psoftg1.lendingmanagement.model.Fine::getCents).orElse(null))")
    @Mapping(target = "_links.self", source = ".", qualifiedByName = "lendingLink")
    @Mapping(target = "_links.book", source = "bookIsbn", qualifiedByName = "bookLinkFromIsbn")
    @Mapping(target = "returnedDate", source = "returnedDate")
    @Mapping(target = "_links.reader", source = "readerDetails", qualifiedByName = "readerLink")
    public abstract LendingView toLendingView(Lending lending);

    public abstract List<LendingView> toLendingView(List<Lending> lendings);

    public abstract LendingsAverageDurationView toLendingsAverageDurationView(Double lendingsAverageDuration);

    @Named(value = "bookLinkFromIsbn")
    protected Map<String, String> mapBookLink(String isbn) {
        Map<String, String> bookLink = new HashMap<>();
        if (isbn == null) return null;
        
        String bookUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/books/")
                .path(isbn)
                .toUriString();
        bookLink.put("href", bookUri);
        return bookLink;
    }
}