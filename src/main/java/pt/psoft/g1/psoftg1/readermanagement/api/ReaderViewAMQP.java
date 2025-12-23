package pt.psoft.g1.psoftg1.readermanagement.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NonNull;
import org.springframework.format.annotation.DateTimeFormat;

@Data
@Schema(description = "A Reader form AMQP communication")
public class ReaderViewAMQP {
    @NotBlank
    @NonNull
    private String readerNumber;

    @NotBlank
    @NonNull
    private Long version;

    @NotBlank
    @NonNull
    private Long userId;

    @NotBlank
    @Email
    @NonNull
    private String username;

    @NotBlank
    @NonNull
    private String password;

    @NotBlank
    @NonNull
    private String fullName;

    @NonNull
    @NotBlank
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String birthDate;

    @NonNull
    @NotBlank
    private String phoneNumber;

    @Nullable
    private String photoURI;

    private boolean gdpr;

    private boolean marketing;

    private boolean thirdParty;

//    @Nullable
//    @Getter
//    @Setter
//    private List<String> interestList;

    public ReaderViewAMQP(){

    }
}
