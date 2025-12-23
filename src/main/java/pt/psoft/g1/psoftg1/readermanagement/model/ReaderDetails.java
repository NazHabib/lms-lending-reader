package pt.psoft.g1.psoftg1.readermanagement.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pt.psoft.g1.psoftg1.exceptions.ConflictException;
import pt.psoft.g1.psoftg1.readermanagement.services.UpdateReaderRequest;
import pt.psoft.g1.psoftg1.shared.model.EntityWithPhoto;

import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "READER_DETAILS")
public class ReaderDetails extends EntityWithPhoto {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long pk;

    @Version
    @Getter
    private Long version;

    // Replaced @OneToOne Reader relationship with direct fields
    @Getter
    @Setter
    @Column(nullable = false, unique = true)
    private String username;

    @Getter
    @Setter
    private String fullName;

    @Embedded
    private ReaderNumber readerNumber;

    @Embedded
    @Getter
    private BirthDate birthDate;

    @Embedded
    private PhoneNumber phoneNumber;

    @Setter
    @Getter
    @Basic
    private boolean gdprConsent;

    @Setter
    @Basic
    @Getter
    private boolean marketingConsent;

    @Setter
    @Basic
    @Getter
    private boolean thirdPartySharingConsent;

    // Replaced List<Genre> entity with List<String> to store genre names or codes
    // avoiding cross-service entity dependency
    @ElementCollection
    @CollectionTable(name = "reader_interests", joinColumns = @JoinColumn(name = "reader_pk"))
    @Column(name = "interest")
    @Getter
    @Setter
    private List<String> interestList = new ArrayList<>();

    protected ReaderDetails() {
        // for ORM only
    }

    public ReaderDetails(ReaderNumber readerNumber,
                         BirthDate birthDate,
                         PhoneNumber phoneNumber,
                         String username,
                         String fullName,
                         boolean gdpr,
                         boolean marketing,
                         boolean thirdParty,
                         String photoURI,
                         List<String> interestList) {
        
        if(username == null || phoneNumber == null) {
            throw new IllegalArgumentException("Provided argument resolves to null object");
        }

        if(!gdpr) {
            throw new IllegalArgumentException("Readers must agree with the GDPR rules");
        }

        this.readerNumber = readerNumber;
        this.birthDate = birthDate;
        this.phoneNumber = phoneNumber;
        this.username = username;
        this.fullName = fullName;
        
        //By the client specifications, gdpr can only have the value of true.
        setGdprConsent(true);

        setPhotoInternal(photoURI);
        setMarketingConsent(marketing);
        setThirdPartySharingConsent(thirdParty);
        
        if (interestList != null) {
            this.interestList = interestList;
        }
    }

    // Simplified Constructor for Bootstrapping (matches previous suggestions)
    public ReaderDetails(ReaderNumber readerNumber, BirthDate birthDate, PhoneNumber phoneNumber, boolean gdpr, boolean marketing, boolean thirdParty, String photoFilename) {
        this.readerNumber = readerNumber;
        this.birthDate = birthDate;
        this.phoneNumber = phoneNumber;
        this.gdprConsent = gdpr;
        this.marketingConsent = marketing;
        this.thirdPartySharingConsent = thirdParty;
        setPhotoInternal(photoFilename);
    }

    public void applyPatch(final long currentVersion, final UpdateReaderRequest request, String photoURI, List<String> interestList) {
        if(currentVersion != this.version) {
            throw new ConflictException("Provided version does not match latest version of this object");
        }

        String birthDateStr = request.getBirthDate();
        String phoneNumberStr = request.getPhoneNumber();
        boolean marketing = request.getMarketing();
        boolean thirdParty = request.getThirdParty();
        String fullNameStr = request.getFullName();
        String usernameStr = request.getUsername();
        // Password update is handled by Auth service, ignored here.

        if(usernameStr != null) {
            this.username = usernameStr;
        }

        if(fullNameStr != null) {
            this.fullName = fullNameStr;
        }

        if(birthDateStr != null) {
            this.birthDate = new BirthDate(birthDateStr);
        }

        if(phoneNumberStr != null) {
            this.phoneNumber = new PhoneNumber(phoneNumberStr);
        }

        if(marketing != this.marketingConsent) {
            setMarketingConsent(marketing);
        }

        if(thirdParty != this.thirdPartySharingConsent) {
            setThirdPartySharingConsent(thirdParty);
        }

        if(photoURI != null) {
            try {
                setPhotoInternal(photoURI);
            } catch(InvalidPathException ignored) {}
        }

        if(interestList != null) {
            this.interestList = interestList;
        }
    }

    public void removePhoto(long desiredVersion) {
        if(desiredVersion != this.version) {
            throw new ConflictException("Provided version does not match latest version of this object");
        }
        setPhotoInternal(null);
    }

    public String getReaderNumber(){
        return this.readerNumber.toString();
    }

    public String getPhoneNumber() { 
        return this.phoneNumber.toString();
    }
    
    public Long getId() {
        return this.pk;
    }
}