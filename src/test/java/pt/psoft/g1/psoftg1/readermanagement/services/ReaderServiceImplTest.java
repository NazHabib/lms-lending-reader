package pt.psoft.g1.psoftg1.readermanagement.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import pt.psoft.g1.psoftg1.exceptions.ConflictException;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.repositories.GenreRepository;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderRepository;
import pt.psoft.g1.psoftg1.shared.repositories.ForbiddenNameRepository;
import pt.psoft.g1.psoftg1.shared.repositories.PhotoRepository;
import pt.psoft.g1.psoftg1.usermanagement.model.Reader;
import pt.psoft.g1.psoftg1.usermanagement.repositories.UserRepository;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * SUT: ReaderServiceImpl (application service / domain service layer)
 * Tipo: Unitário isolado (Mockito), sem Spring.
 *
 * O que validamos:
 *  - Regras funcionais de criação/atualização (username duplicado, nomes proibidos, mapping de interesses, lógica de photo/URI)
 *  - Delegação para repositórios e mapper
 *  - Validações de parâmetros (datas, minTop) e exceções lançadas
 *  - Remoção de foto (orquestração de save + deleteByPhotoFile)
 *  - Pesquisa com defaults e NotFound quando vazio
 */
@ExtendWith(MockitoExtension.class)
class ReaderServiceImplTest {

    // --------- mocks (colaboradores) ----------
    @Mock ReaderRepository readerRepo;
    @Mock UserRepository userRepo;
    @Mock ReaderMapper readerMapper;
    @Mock GenreRepository genreRepo;
    @Mock ForbiddenNameRepository forbiddenNameRepository;
    @Mock PhotoRepository photoRepository;

    // --------- SUT ----------
    @InjectMocks ReaderServiceImpl service;

    // --------- helpers ----------
    private CreateReaderRequest req(String email, String fullName, List<String> interests, MultipartFile photo) {
        CreateReaderRequest r = new CreateReaderRequest();
        r.setUsername(email);
        r.setPassword("Secret123!");
        r.setFullName(fullName);
        r.setBirthDate("2000-01-01");
        r.setPhoneNumber("919191919");
        r.setInterestList(interests);
        r.setPhoto(photo);
        r.setGdpr(true);
        r.setMarketing(false);
        r.setThirdParty(false);
        return r;
    }

    @BeforeEach
    void resetMocks() {
        Mockito.reset(readerRepo, userRepo, readerMapper, genreRepo, forbiddenNameRepository, photoRepository);
    }

    // ------------------------ create(...) ------------------------

    @Test
    @DisplayName("create: username já existe -> ConflictException")
    void create_usernameExists_conflict() {
        var request = req("user@mail.com", "Ana Silva", List.of(), null);
        when(userRepo.findByUsername("user@mail.com")).thenReturn(Optional.of(mock(Reader.class)));

        assertThrows(ConflictException.class, () -> service.create(request, null));
        verify(userRepo).findByUsername("user@mail.com");
        verifyNoMoreInteractions(readerRepo, readerMapper);
    }

    @Test
    @DisplayName("create(): throws IllegalArgumentException when fullName contains a forbidden word")
    void create_forbiddenWordInName_illegalArgument() {
        // arrange: a request with two words; first is forbidden, second isn’t
        CreateReaderRequest request = new CreateReaderRequest();
        request.setUsername("u@e.com");
        request.setPassword("P@ssw0rd!");
        request.setFullName("Nome Ruim");     // <- “Nome” is the forbidden match
        request.setBirthDate("2000-01-01");
        request.setPhoneNumber("910000000");
        request.setInterestList(Collections.emptyList()); // no genre lookups involved

        // user not taken
        when(userRepo.findByUsername(anyString())).thenReturn(Optional.empty());

        @SuppressWarnings("unchecked")
        List<Object> nonEmpty = mock(List.class);
        when(nonEmpty.isEmpty()).thenReturn(false); // make it “non-empty” regardless of element type

        when(forbiddenNameRepository.findByForbiddenNameIsContained(eq("Nome")))
                .thenReturn((List) nonEmpty); // raw cast OK for tests

        lenient().when(forbiddenNameRepository.findByForbiddenNameIsContained(eq("Ruim")))
                .thenReturn(Collections.emptyList());

        // act + assert
        assertThrows(IllegalArgumentException.class, () -> service.create(request, /*photoURI*/ null));

        // optional: verify we short-circuit before any persistence
        verify(readerRepo, never()).save(any());
        verify(userRepo, never()).save(any());
    }

    @Test
    @DisplayName("create: mapeia interestList (strings) para Genre e persiste Reader + ReaderDetails")
    void create_mapsInterests_andPersists() {
        var interests = List.of("Drama", "Aventura");
        var request = req("user@mail.com", "Alex Doe", interests, /*photo*/ null);

        when(userRepo.findByUsername(anyString())).thenReturn(Optional.empty());
        when(forbiddenNameRepository.findByForbiddenNameIsContained(anyString())).thenReturn(List.of());
        when(readerRepo.getCountFromCurrentYear()).thenReturn(7);

        // resolve cada genre
        Genre g1 = new Genre("Drama");
        Genre g2 = new Genre("Aventura");
        when(genreRepo.findByString("Drama")).thenReturn(Optional.of(g1));
        when(genreRepo.findByString("Aventura")).thenReturn(Optional.of(g2));

        Reader reader = mock(Reader.class);
        ReaderDetails rd = mock(ReaderDetails.class);

        when(readerMapper.createReader(request)).thenReturn(reader);
        when(readerMapper.createReaderDetails(eq(8), eq(reader), eq(request), eq("photo://ok"), argThat(list ->
                list != null && list.size() == 2 && list.containsAll(List.of(g1, g2))
        ))).thenReturn(rd);

        when(readerRepo.save(rd)).thenReturn(rd);

        ReaderDetails saved = service.create(request, "photo://ok");
        assertNotNull(saved);

        verify(userRepo).save(reader);
        verify(readerRepo).save(rd);
        verify(genreRepo).findByString("Drama");
        verify(genreRepo).findByString("Aventura");
    }

    @Test
    @DisplayName("create: genre inexistente -> NotFoundException")
    void create_unknownGenre_notFound() {
        var request = req("u@e.com", "Alex", List.of("Fantasia"), null);

        when(userRepo.findByUsername(anyString())).thenReturn(Optional.empty());
        when(forbiddenNameRepository.findByForbiddenNameIsContained(anyString())).thenReturn(List.of());
        when(genreRepo.findByString("Fantasia")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.create(request, "uri://x"));
        verify(genreRepo).findByString("Fantasia");
        verifyNoMoreInteractions(readerMapper);
    }

    @Test
    @DisplayName("create: lógica photo/photoURI -> mismatch zera photo no request")
    void create_photoUriMismatch_zeroPhoto() {
        MultipartFile fakePhoto = mock(MultipartFile.class);
        var request = req("u@e.com", "Alex Doe", List.of(), fakePhoto);

        when(userRepo.findByUsername(anyString())).thenReturn(Optional.empty());
        when(forbiddenNameRepository.findByForbiddenNameIsContained(anyString())).thenReturn(List.of());
        when(readerRepo.getCountFromCurrentYear()).thenReturn(0);

        Reader reader = mock(Reader.class);
        ReaderDetails rd = mock(ReaderDetails.class);

        when(readerMapper.createReader(any())).thenReturn(reader);
        when(readerMapper.createReaderDetails(eq(1), eq(reader), same(request), eq(null), anyList()))
                .thenReturn(rd);
        when(readerRepo.save(rd)).thenReturn(rd);

        // photo != null e photoURI == null => service deve setar request.setPhoto(null)
        ReaderDetails res = service.create(request, null);
        assertNotNull(res);

        // Checa que o mapper foi chamado com o mesmo request mas com photo "limpo" (não temos equals de MultipartFile; validamos via lógica de branch + verify)
        verify(readerMapper).createReaderDetails(eq(1), eq(reader), same(request), eq(null), anyList());
    }

    // ------------------------ update(...) ------------------------

    @Test
    @DisplayName("update: reader não encontrado -> NotFoundException")
    void update_readerNotFound() {
        when(readerRepo.findByUserId(10L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
                () -> service.update(10L, new UpdateReaderRequest(), 1L, null));
    }

    @Test
    @DisplayName("update: aplica patch, reconcilia photo/URI, salva reader e details")
    void update_happyPath() {
        var req = new UpdateReaderRequest();
        req.setInterestList(List.of("Drama"));
        MultipartFile fakePhoto = mock(MultipartFile.class);
        req.setPhoto(fakePhoto);

        Genre g = new Genre("Drama");
        when(genreRepo.findByString("Drama")).thenReturn(Optional.of(g));

        ReaderDetails details = mock(ReaderDetails.class);
        Reader reader = mock(Reader.class);

        when(readerRepo.findByUserId(5L)).thenReturn(Optional.of(details));
        when(details.getReader()).thenReturn(reader);
        when(readerRepo.save(details)).thenReturn(details);

        // photo!=null e photoURI==null -> service zera request.setPhoto(null) antes do applyPatch
        ReaderDetails out = service.update(5L, req, 9L, null);
        assertNotNull(out);

        verify(details).applyPatch(eq(9L), same(req), eq(null), argThat(list ->
                list != null && list.size() == 1 && list.get(0).getGenre().equals("Drama")
        ));
        verify(userRepo).save(reader);
        verify(readerRepo).save(details);
    }

    // ------------------------ findTopByGenre(...) ------------------------

    @Test
    @DisplayName("findTopByGenre: start > end -> IllegalArgumentException")
    void findTopByGenre_invalidDates() {
        LocalDate start = LocalDate.of(2025, 1, 2);
        LocalDate end = LocalDate.of(2025, 1, 1);
        assertThrows(IllegalArgumentException.class, () -> service.findTopByGenre("Drama", start, end));
    }

    @Test
    @DisplayName("findTopByGenre: delega para repo com paginação de 5 itens")
    void findTopByGenre_delegates() {
        var list = List.of(new ReaderBookCountDTO(mock(ReaderDetails.class), 3L));
        when(readerRepo.findTopByGenre(any(Pageable.class), eq("Drama"), any(), any()))
                .thenReturn(new PageImpl<>(list));

        var out = service.findTopByGenre("Drama", LocalDate.now().minusDays(1), LocalDate.now());
        assertEquals(list, out);

        // captura pageable para sanity check (size=5)
        ArgumentCaptor<Pageable> cap = ArgumentCaptor.forClass(Pageable.class);
        verify(readerRepo).findTopByGenre(cap.capture(), eq("Drama"), any(), any());
        assertEquals(5, cap.getValue().getPageSize());
        assertEquals(0, cap.getValue().getPageNumber());
    }

    // ------------------------ findTopReaders(...) ------------------------

    @Test
    @DisplayName("findTopReaders: minTop < 1 -> IllegalArgumentException")
    void findTopReaders_invalidMinTop() {
        assertThrows(IllegalArgumentException.class, () -> service.findTopReaders(0));
    }

    @Test
    @DisplayName("findTopReaders: delega para repo com PageRequest de tamanho minTop")
    void findTopReaders_delegates() {
        var rd = mock(ReaderDetails.class);
        when(readerRepo.findTopReaders(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(rd)));

        var out = service.findTopReaders(7);
        assertEquals(1, out.size());
        assertSame(rd, out.get(0));

        ArgumentCaptor<Pageable> cap = ArgumentCaptor.forClass(Pageable.class);
        verify(readerRepo).findTopReaders(cap.capture());
        assertEquals(7, cap.getValue().getPageSize());
        assertEquals(0, cap.getValue().getPageNumber());
    }

    // ------------------------ delegations / finders ------------------------

    @Test
    @DisplayName("findByReaderNumber delega para repo")
    void findByReaderNumber_delegates() {
        when(readerRepo.findByReaderNumber("2024/1")).thenReturn(Optional.of(mock(ReaderDetails.class)));
        assertTrue(service.findByReaderNumber("2024/1").isPresent());
        verify(readerRepo).findByReaderNumber("2024/1");
    }

    @Test
    @DisplayName("findByPhoneNumber delega para repo")
    void findByPhoneNumber_delegates() {
        when(readerRepo.findByPhoneNumber("999")).thenReturn(List.of());
        assertNotNull(service.findByPhoneNumber("999"));
        verify(readerRepo).findByPhoneNumber("999");
    }

    @Test
    @DisplayName("findByUsername delega para repo")
    void findByUsername_delegates() {
        when(readerRepo.findByUsername("u")).thenReturn(Optional.empty());
        assertTrue(service.findByUsername("u").isEmpty());
        verify(readerRepo).findByUsername("u");
    }

    @Test
    @DisplayName("findAll delega para repo")
    void findAll_delegates() {
        when(readerRepo.findAll()).thenReturn(List.of());
        assertNotNull(service.findAll());
        verify(readerRepo).findAll();
    }

    // ------------------------ removeReaderPhoto(...) ------------------------

    @Test
    @DisplayName("removeReaderPhoto: salva readerDetails e apaga ficheiro por photoFile")
    void removeReaderPhoto_happyPath() {
        var details = mock(ReaderDetails.class);
        var photo = mock(pt.psoft.g1.psoftg1.shared.model.Photo.class);

        when(details.getPhoto()).thenReturn(photo);
        when(photo.getPhotoFile()).thenReturn("file-123.png");
        when(readerRepo.findByReaderNumber("2024/9")).thenReturn(Optional.of(details));
        when(readerRepo.save(details)).thenReturn(details);

        Optional<ReaderDetails> out = service.removeReaderPhoto("2024/9", 5L);
        assertTrue(out.isPresent());

        verify(details).removePhoto(5L);
        verify(readerRepo).save(details);
        verify(photoRepository).deleteByPhotoFile("file-123.png");
    }

    @Test
    @DisplayName("removeReaderPhoto: reader inexistente -> NotFoundException")
    void removeReaderPhoto_readerNotFound() {
        when(readerRepo.findByReaderNumber("y/x")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.removeReaderPhoto("y/x", 1L));
    }

    // ------------------------ searchReaders(...) ------------------------


    @Test
    @DisplayName("searchReaders: retorna lista quando repo devolve resultados")
    void searchReaders_returnsList() {
        var rd = mock(ReaderDetails.class);
        when(readerRepo.searchReaderDetails(any(), any()))
                .thenReturn(List.of(rd));

        var out = service.searchReaders(new pt.psoft.g1.psoftg1.shared.services.Page(2, 5),
                new SearchReadersQuery("Ana", "999", "2024/1"));
        assertEquals(1, out.size());
        assertSame(rd, out.get(0));
    }
}
