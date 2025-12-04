package pt.psoft.g1.psoftg1.readermanagement.services;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * SUT: CreateReaderRequest (DTO)
 * Tipo: Unitário isolado (sem dependências externas)
 *
 * O que testamos:
 *  - Validação Bean Validation (@NotBlank, @Email).
 *  - Presença de anotações (inclui @DateTimeFormat no birthDate).
 *  - Getters/Setters gerados por Lombok (@Data).
 *  - equals/hashCode/toString (comportamento gerado por Lombok).
 *  - Integração com Mockito para MultipartFile (photo).
 *  - Flags booleanas com getters customizados (getGdpr/getMarketing/getThirdParty).
 *  - Interesse (interestList) pode ser null e pode ser atribuído.
 */
class CreateReaderRequestTest {

    private static Validator validator;

    @BeforeAll
    static void initValidator() {
        ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
        validator = vf.getValidator();
    }

    // ----------------------------
    // Helpers
    // ----------------------------
    private static CreateReaderRequest valid() {
        CreateReaderRequest r = new CreateReaderRequest();
        r.setUsername("alice@example.com");
        r.setPassword("Aa123456!");
        r.setFullName("Alice Doe");
        r.setBirthDate("2000-01-01");
        r.setPhoneNumber("910000000");
        r.setGdpr(true);
        r.setMarketing(false);
        r.setThirdParty(true);
        r.setInterestList(List.of("Drama", "Comedy"));
        return r;
    }

    // ----------------------------
    // Bean Validation – happy path
    // ----------------------------
    @Test
    void validObject_hasNoViolations() {
        var r = valid();
        Set<?> v = validator.validate(r);
        assertTrue(v.isEmpty(), "Um recurso válido não deve violar constraints");
    }

    // ----------------------------
    // Bean Validation – required/format
    // ----------------------------
    @Test
    void username_mustBeEmail_andNotBlank() {
        // invalid email (non-null) -> violates @Email
        var r1 = valid();
        r1.setUsername("not-an-email");
        assertFalse(validator.validate(r1).isEmpty(), "Username inválido deve violar @Email");

        // blank (non-null) -> violates @NotBlank
        var r2 = valid();
        r2.setUsername("   ");
        assertFalse(validator.validate(r2).isEmpty(), "Username em branco deve violar @NotBlank");
    }

    @Test
    void username_null_triggersLombokNonNullSetter() {
        // Lombok @NonNull on the field/setter throws NPE before Bean Validation
        var r3 = valid();
        assertThrows(NullPointerException.class, () -> r3.setUsername(null),
                "Setter com @NonNull deve lançar NullPointerException ao receber null");
    }


    @Test
    void password_notBlank() {
        var r = valid();
        r.setPassword("   ");
        assertFalse(validator.validate(r).isEmpty());
    }

    @Test
    void fullName_notBlank() {
        var r = valid();
        r.setFullName("");
        assertFalse(validator.validate(r).isEmpty());
    }

    @Test
    void birthDate_notBlank_stringFormat_isntBeanValidated() throws Exception {
        // @DateTimeFormat is for Spring binding, not Bean Validation.
        // Verificamos: NotBlank viola; formato não é validado pelo Bean Validation.
        var r1 = valid();
        r1.setBirthDate("   ");
        assertFalse(validator.validate(r1).isEmpty(), "birthDate em branco deve violar @NotBlank");

        // formato "2024/01/01" não produz violação de Bean Validation (não há @Pattern)
        var r2 = valid();
        r2.setBirthDate("2024/01/01");
        assertTrue(validator.validate(r2).isEmpty(), "Sem @Pattern, o formato diferente não viola Bean Validation");

        // checamos presença de @DateTimeFormat
        Field f = CreateReaderRequest.class.getDeclaredField("birthDate");
        assertNotNull(f.getAnnotation(DateTimeFormat.class), "@DateTimeFormat deve estar presente no birthDate");
    }

    @Test
    void phoneNumber_notBlank() {
        var r = valid();
        r.setPhoneNumber(" ");
        assertFalse(validator.validate(r).isEmpty(), "phoneNumber em branco deve violar @NotBlank");
    }

    // ----------------------------
    // Annotation presence quick check
    // ----------------------------
    @Test
    void annotations_present_on_username_password_fullName() throws Exception {
        Field u = CreateReaderRequest.class.getDeclaredField("username");
        assertNotNull(u.getAnnotation(NotBlank.class));
        assertNotNull(u.getAnnotation(Email.class));

        Field p = CreateReaderRequest.class.getDeclaredField("password");
        assertNotNull(p.getAnnotation(NotBlank.class));

        Field n = CreateReaderRequest.class.getDeclaredField("fullName");
        assertNotNull(n.getAnnotation(NotBlank.class));
    }

    // ----------------------------
    // Lombok data: getters/setters & toString
    // ----------------------------
    @Test
    void gettersSetters_work_and_toString_containsKeyFields() {
        var r = new CreateReaderRequest();
        r.setUsername("bob@example.com");
        r.setPassword("Secret!");
        r.setFullName("Bob Jones");
        r.setBirthDate("1999-12-31");
        r.setPhoneNumber("930000000");
        r.setGdpr(true);
        r.setMarketing(true);
        r.setThirdParty(false);
        r.setInterestList(List.of("Sci-Fi"));

        assertEquals("bob@example.com", r.getUsername());
        assertEquals("Secret!", r.getPassword());
        assertEquals("Bob Jones", r.getFullName());
        assertEquals("1999-12-31", r.getBirthDate());
        assertEquals("930000000", r.getPhoneNumber());
        assertTrue(r.getGdpr());
        assertTrue(r.getMarketing());
        assertFalse(r.getThirdParty());
        assertEquals(List.of("Sci-Fi"), r.getInterestList());

        String s = r.toString();
        assertTrue(s.contains("bob@example.com"));
        assertTrue(s.contains("Bob Jones"));
    }

    // ----------------------------
    // MultipartFile with Mockito
    // ----------------------------
    @Test
    void photo_isSettable_withMockitoMock() {
        MultipartFile file = mock(MultipartFile.class);
        Mockito.when(file.getOriginalFilename()).thenReturn("avatar.png");

        var r = valid();
        r.setPhoto(file);

        assertNotNull(r.getPhoto());
        assertEquals("avatar.png", r.getPhoto().getOriginalFilename());
    }

    // ----------------------------
    // equals / hashCode (Lombok)
    // ----------------------------
    @Test
    void equalsHashCode_sameData_equal_and_hashMatch() {
        var a = valid();
        var b = valid(); // mesmos valores
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equals_differentUsername_notEqual() {
        var a = valid();
        var b = valid();
        b.setUsername("other@example.com");
        assertNotEquals(a, b);
    }

    // ----------------------------
    // interestList: null/assigned
    // ----------------------------
    @Test
    void interestList_null_or_assigned_ok() {
        var a = valid();
        a.setInterestList(null);
        assertNull(a.getInterestList());

        var b = valid();
        b.setInterestList(List.of("Drama", "Action"));
        assertEquals(List.of("Drama", "Action"), b.getInterestList());
    }
}
