package pt.psoft.g1.psoftg1.readermanagement.api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * SUT: ReaderViewMapper (MapStruct Spring bean)
 * Type: Spring bean + mapping integration (no web layer)
 *
 * We verify:
 *  - Bean is created by Spring (componentModel="spring").
 *  - Mapping fills the fields we expect.
 *  - The generated photo URL uses ServletUriComponentsBuilder; we bind a mock request.
 */
@SpringBootTest
class ReaderViewMapperTest {

    @Autowired ReaderViewMapper mapper;

    /** Bind a fake request so ServletUriComponentsBuilder can build the photo URL. */
    private void bindRequest(String host, int port, String contextPath) {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/");
        req.setServerName(host);
        req.setServerPort(port);
        req.setContextPath(contextPath);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(req));
    }

    @AfterEach
    void clearRequest() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("Mapper bean is available in Spring context")
    void mapper_isRegisteredAsSpringBean() {
        assertNotNull(mapper);
    }

    @Test
    @DisplayName("toReaderView maps nested fields and builds photo URL")
    void toReaderView_mapsAndBuildsPhotoUrl() {
        // Arrange: bind a fake request (affects generated photo URL host/base)
        bindRequest("localhost", 8080, "");

        // Deep-stubbed ReaderDetails; only getters used by the mapper are stubbed
        ReaderDetails rd = mock(ReaderDetails.class, RETURNS_DEEP_STUBS);
        when(rd.getReaderNumber()).thenReturn("2024/7");
        when(rd.getReader().getName().getName()).thenReturn("Alice");
        when(rd.getReader().getUsername()).thenReturn("alice@example.com");
        when(rd.getBirthDate().getBirthDate()).thenReturn(LocalDate.of(1990, 1, 2));
        when(rd.getPhoneNumber()).thenReturn("910000000");
        when(rd.isGdprConsent()).thenReturn(true);
        when(rd.getInterestList()).thenReturn(List.of(new Genre("Drama")));

        // Act
        ReaderView view = mapper.toReaderView(rd);

        // Assert
        assertEquals("Alice", view.getFullName());
        assertEquals("alice@example.com", view.getEmail());
        assertEquals("910000000", view.getPhoneNumber());
        assertTrue(view.getPhoto().endsWith("/api/readers/2024/7/photo"),
                "photo URL should end with /api/readers/2024/7/photo");
        assertEquals(List.of("Drama"), view.getInterestList());
    }
}
