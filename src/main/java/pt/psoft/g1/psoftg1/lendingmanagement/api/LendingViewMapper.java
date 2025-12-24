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

@Mapper(componentModel = "spring")
public abstract class LendingViewMapper extends MapperInterface {

    @Mapping(target = "lendingNumber", source = "lendingNumber")
    @Mapping(target = "bookTitle", source = "bookTitle")
    @Mapping(target = "fineValueInCents", expression = "java((Integer) (lending.getFine().map(f -> f.getCents()).orElse(null)))")
    @Mapping(target = "_links.self", source = ".", qualifiedByName = "lendingLink")
    @Mapping(target = "_links.book", source = "bookIsbn", qualifiedByName = "bookLinkFromIsbn")
    @Mapping(target = "returnedDate", source = "returnedDate")
    @Mapping(target = "_links.reader", source = "readerDetails", qualifiedByName = "readerLink")
    // Fix: Explicitly cast to Integer to match DTO field type
    @Mapping(target = "daysUntilReturn", expression = "java((Integer) lending.getDaysUntilReturn())") 
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