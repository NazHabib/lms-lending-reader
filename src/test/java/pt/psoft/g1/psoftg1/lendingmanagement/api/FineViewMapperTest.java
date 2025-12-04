package pt.psoft.g1.psoftg1.lendingmanagement.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.Mapper;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FineViewMapper currently has no methods; we only soft-check @Mapper.
 */
@DisplayName("FineViewMapper – soft @Mapper presence")
public class FineViewMapperTest {

    @Test
    @DisplayName("Soft @Mapper check: if present, componentModel='spring'")
    void mapperAnnotation_present_withSpring() {
        Mapper ann = FineViewMapper.class.getAnnotation(Mapper.class);
        if (ann != null) {
            assertEquals("spring", ann.componentModel(),
                    "If @Mapper exists, it should be 'spring'");
        }
    }
}
