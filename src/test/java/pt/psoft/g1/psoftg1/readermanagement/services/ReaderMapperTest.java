package pt.psoft.g1.psoftg1.readermanagement.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.usermanagement.model.Reader;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SUT: ReaderMapper (MapStruct interface)
 * NOTE: Do NOT check @Mapper/@Mapping via reflection (not visible at runtime).
 * We verify method signatures only.
 */
@DisplayName("ReaderMapper – method signatures")
class ReaderMapperTest {

    @Test
    @DisplayName("createReader signature: Reader createReader(CreateReaderRequest)")
    void createReader_signature() throws Exception {
        Method m = ReaderMapper.class.getMethod("createReader", CreateReaderRequest.class);
        assertEquals(Reader.class, m.getReturnType());
        Parameter[] params = m.getParameters();
        assertEquals(1, params.length);
        assertEquals(CreateReaderRequest.class, params[0].getType());
    }

    @Test
    @DisplayName("createReaderDetails signature: ReaderDetails createReaderDetails(int, Reader, CreateReaderRequest, String, List<Genre>)")
    void createReaderDetails_signature() throws Exception {
        Method m = ReaderMapper.class.getMethod(
                "createReaderDetails",
                int.class, Reader.class, CreateReaderRequest.class, String.class, List.class
        );
        assertEquals(ReaderDetails.class, m.getReturnType());
        Class<?>[] expected = {int.class, Reader.class, CreateReaderRequest.class, String.class, List.class};
        assertArrayEquals(expected,
                Arrays.stream(m.getParameters()).map(Parameter::getType).toArray());
    }
}
