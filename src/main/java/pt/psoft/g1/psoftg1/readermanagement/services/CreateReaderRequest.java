package pt.psoft.g1.psoftg1.readermanagement.services;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateReaderRequest {
    private String username;
    private String password;
    private String fullName;
    private String birthDate;
    private String phoneNumber;
    private Boolean gdprConsent;
    private Boolean marketingConsent;
    private Boolean thirdPartySharingConsent;
    private List<String> interestList;
    private String photo;

    // Explicit Getters to match service calls if Lombok naming differs
    public Boolean getGdprConsent() { return gdprConsent; }
    public Boolean getMarketingConsent() { return marketingConsent; }
    public Boolean getThirdPartySharingConsent() { return thirdPartySharingConsent; }
}