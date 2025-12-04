package pt.psoft.g1.psoftg1.readermanagement.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.lendingmanagement.api.LendingViewMapper;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.lendingmanagement.services.LendingService;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.services.ReaderService;
import pt.psoft.g1.psoftg1.shared.services.ConcurrencyService;
import pt.psoft.g1.psoftg1.shared.services.FileStorageService;
import pt.psoft.g1.psoftg1.external.service.ApiNinjasService;
import pt.psoft.g1.psoftg1.usermanagement.model.Librarian;
import pt.psoft.g1.psoftg1.usermanagement.model.User;
import pt.psoft.g1.psoftg1.usermanagement.services.UserService;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// added imports for BirthDate/LocalDate
import java.time.LocalDate;
import pt.psoft.g1.psoftg1.readermanagement.model.BirthDate;

/**
 * ReaderController Web layer tests WITHOUT importing other test classes.
 * All collaborators are mocked with @MockBean.
 */
@WebMvcTest(controllers = ReaderController.class)
@AutoConfigureMockMvc(addFilters = false) // disable security filters to avoid 401s in slice tests
class ReaderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // ---- Mock collaborators used by ReaderController ----
    @MockBean private ReaderService readerService;
    @MockBean private UserService userService;
    @MockBean private ReaderViewMapper readerViewMapper;
    @MockBean private LendingService lendingService;
    @MockBean private LendingViewMapper lendingViewMapper;
    @MockBean private ConcurrencyService concurrencyService;
    @MockBean private FileStorageService fileStorageService;
    @MockBean private ApiNinjasService apiNinjasService;

    // ---------- Helpers ----------
    private static ReaderDetails dummyReaderDetails(String readerNumber) {
        ReaderDetails rd = Mockito.mock(ReaderDetails.class);
        when(rd.getReaderNumber()).thenReturn(readerNumber);
        when(rd.getVersion()).thenReturn(9L);

        // FIX: BirthDate expects a String
        BirthDate bd = new BirthDate("2000-01-01");
        when(rd.getBirthDate()).thenReturn(bd);

        return rd;
    }

    private static ReaderView dummyReaderView(String rn) {
        ReaderView v = new ReaderView();
        v.setReaderNumber(rn);
        v.setEmail("user@example.com");
        v.setFullName("Test User");
        v.setBirthDate("2000-01-01");
        v.setPhoneNumber("910000000");
        v.setGdprConsent(true);
        v.setMarketingConsent(false);
        v.setThirdPartySharingConsent(false);
        v.setInterestList(List.of("Drama"));
        return v;
    }

    // ---------- Tests ----------

    @Test
    @DisplayName("GET /api/readers (as Librarian) -> 200 with list of readers")
    void getData_asLibrarian_returnsList() throws Exception {
        // logged user is a Librarian
        User librarian = Mockito.mock(Librarian.class);
        when(userService.getAuthenticatedUser(any())).thenReturn(librarian);

        // service returns 2 ReaderDetails
        ReaderDetails r1 = dummyReaderDetails("2024/1");
        ReaderDetails r2 = dummyReaderDetails("2024/2");
        when(readerService.findAll()).thenReturn(List.of(r1, r2));

        // mapper -> two views
        ReaderView v1 = dummyReaderView("2024/1");
        ReaderView v2 = dummyReaderView("2024/2");
        when(readerViewMapper.toReaderView(any(Iterable.class))).thenReturn(List.of(v1, v2));

        mockMvc.perform(get("/api/readers"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].readerNumber").value("2024/1"))
                .andExpect(jsonPath("$[1].readerNumber").value("2024/2"));
    }

    @Test
    @DisplayName("GET /api/readers/{year}/{seq} -> 200 OK with ETag and Quote")
    void findByReaderNumber_ok() throws Exception {
        // simulate authenticated user (role doesn’t matter for this endpoint)
        User anyUser = Mockito.mock(User.class);
        when(userService.getAuthenticatedUser(any())).thenReturn(anyUser);

        ReaderDetails rd = dummyReaderDetails("2024/9");
        when(readerService.findByReaderNumber("2024/9")).thenReturn(Optional.of(rd));

        // mapper returns a ReaderQuoteView (extends ReaderView)
        ReaderQuoteView view = new ReaderQuoteView();
        view.setReaderNumber("2024/9");
        view.setEmail("r@example.com");
        view.setFullName("Reader Nine");
        when(readerViewMapper.toReaderQuoteView(rd)).thenReturn(view);

        // quote from external service
        when(apiNinjasService.getRandomEventFromYearMonth(anyInt(), anyInt())).thenReturn("A cool fact");

        mockMvc.perform(get("/api/readers/{year}/{seq}", 2024, 9))
                .andExpect(status().isOk())
                .andExpect(header().exists("ETag"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.readerNumber").value("2024/9"));
    }

    @Test
    @DisplayName("GET /api/readers/{year}/{seq} -> 404 when not found")
    void findByReaderNumber_notFound() throws Exception {
        User anyUser = Mockito.mock(User.class);
        when(userService.getAuthenticatedUser(any())).thenReturn(anyUser);

        when(readerService.findByReaderNumber("2024/1")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/readers/{year}/{seq}", 2024, 1))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/readers?phoneNumber=xxx -> 200 with list when service returns data")
    void findByPhoneNumber_ok() throws Exception {
        // Pretend service returns two reader details
        ReaderDetails r1 = dummyReaderDetails("2024/11");
        ReaderDetails r2 = dummyReaderDetails("2024/12");
        when(readerService.findByPhoneNumber("999")).thenReturn(List.of(r1, r2));

        ReaderView v1 = dummyReaderView("2024/11");
        ReaderView v2 = dummyReaderView("2024/12");
        when(readerViewMapper.toReaderView(any(Iterable.class))).thenReturn(List.of(v1, v2));

        mockMvc.perform(get("/api/readers").param("phoneNumber", "999"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.items[0].readerNumber").value("2024/11"));
    }
}
