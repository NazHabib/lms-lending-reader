package pt.psoft.g1.psoftg1.lendingmanagement.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A Lending form to be sent by AMQP")
public class LendingViewAMQP {
    private String lendingNumber;
    private String isbn;
    private String readerNumber;
    private LocalDate returnedDate;
    private String commentary;
    private Long version;
    private boolean recommended; // Needed for mapper 'recommended' target
}