package pt.psoft.g1.psoftg1.readermanagement.services;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReaderRequest {
    private String fullName;
    private String username;
    private String password;
    private String phoneNumber;
    private String birthDate;
    private Boolean gdprConsent;
    private Boolean marketingConsent;
    private Boolean thirdPartySharingConsent;
    private List<String> interestList;

    // Helper methods to match Service calls
    public Boolean getMarketing() { return marketingConsent; }
    public Boolean getThirdParty() { return thirdPartySharingConsent; }
    public Boolean isGdprConsent() { return gdprConsent; }
    public Boolean isMarketingConsent() { return marketingConsent; }
    public Boolean isThirdPartyConsent() { return thirdPartySharingConsent; }
}