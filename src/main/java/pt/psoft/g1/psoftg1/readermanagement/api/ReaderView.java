package pt.psoft.g1.psoftg1.readermanagement.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "A Reader details view")
public class ReaderView extends RepresentationModel<ReaderView> {
    @Schema(description = "The reader number")
    private String readerNumber;

    @Schema(description = "The reader's full name")
    private String fullName;

    @Schema(description = "The reader's username")
    private String username;

    @Schema(description = "The reader's birth date")
    private String birthDate;

    @Schema(description = "The reader's phone number")
    private String phoneNumber;

    @Schema(description = "The reader's photo URL")
    private String photoUrl;
}