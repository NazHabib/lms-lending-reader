package pt.psoft.g1.psoftg1.lendingmanagement.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Data
@Schema(description = "A Lending from AMQP communication")
public class LendingViewAMQP {

    @NotNull
    private String lendingNumber;

    @NotNull
    private Long version;

    @NotNull
    private String isbn;

    @NotNull
    private String readerNumber;

    private String commentary;

    private LocalDate returnedDate;

    private Boolean isRecommended;

}

/*>
A: a ambos deve ser facultado:
>- lending number
>- book title
>- lending date
>- return date
>- number of days till return date (if applicable)
>- number of days in overdue (if applicable)
>- fine amount (if applicable)
*/
