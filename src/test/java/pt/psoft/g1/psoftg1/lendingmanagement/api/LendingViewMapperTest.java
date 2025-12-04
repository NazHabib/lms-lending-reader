package pt.psoft.g1.psoftg1.lendingmanagement.api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * SUT: LendingViewMapper
 * Notes:
 *  - Do not stub book.getTitle(): in your domain it’s not a String.
 *  - Use alphanumeric IDs (no "/") so ServletUriComponentsBuilder.pathSegment(...) won’t throw.
 */
@DisplayName("LendingViewMapper – maps domain to view (core fields + links)")
public class LendingViewMapperTest {

    private final LendingViewMapper mapper = Mappers.getMapper(LendingViewMapper.class);

    @BeforeEach
    void bindRequest() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI("/api/lendings");
        req.setContextPath("");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(req));
    }

    @AfterEach
    void clearRequest() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("toLendingView maps fields and populates _links (no slash IDs)")
    void toLendingView_mapsFields_andLinks() {
        String lendingNumber = "LN202409";
        String readerNumber  = "RN202401";

        Book book = mock(Book.class);

        ReaderDetails rd = mock(ReaderDetails.class);
        when(rd.getReaderNumber()).thenReturn(readerNumber);

        Lending lending = mock(Lending.class);
        when(lending.getLendingNumber()).thenReturn(lendingNumber);
        when(lending.getBook()).thenReturn(book);
        when(lending.getReaderDetails()).thenReturn(rd);
        when(lending.getStartDate()).thenReturn(LocalDate.of(2024, 10, 1));
        when(lending.getLimitDate()).thenReturn(LocalDate.of(2024, 10, 10));
        when(lending.getReturnedDate()).thenReturn(null);
        when(lending.getFineValueInCents()).thenReturn(Optional.of(300));

        LendingView view = mapper.toLendingView(lending);

        assertNotNull(view);
        assertEquals(lendingNumber, view.getLendingNumber());
        assertEquals(LocalDate.of(2024, 10, 1), view.getStartDate());
        assertEquals(LocalDate.of(2024, 10, 10), view.getLimitDate());
        assertNull(view.getReturnedDate());
        assertEquals(300, view.getFineValueInCents());

        assertNotNull(view.get_links(), "_links must not be null");
        assertNotNull(view.get_links().getSelf(), "self link must not be null");
        assertNotNull(view.get_links().getBook(), "book link must not be null");
        assertNotNull(view.get_links().getReader(), "reader link must not be null");
    }

    @Test
    @DisplayName("toLendingsAverageDurationView wraps the value")
    void toLendingsAverageDurationView_maps() {
        LendingsAverageDurationView v = mapper.toLendingsAverageDurationView(2.5d);
        assertNotNull(v);
        assertEquals(2.5d, v.getLendingsAverageDuration());
    }
}
