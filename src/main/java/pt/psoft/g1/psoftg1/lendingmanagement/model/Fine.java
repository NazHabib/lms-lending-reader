package pt.psoft.g1.psoftg1.lendingmanagement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
public class Fine {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long pk;

    @Version
    private Long version;

    @NotNull
    @PositiveOrZero
    private int cents;

    @OneToOne
    @NotNull
    private Lending lending;

    public Fine(Lending lending) {
        this.lending = lending;
        // Logic to calculate cents based on delay
        int daysDelayed = lending.getDaysDelayed();
        int finePerDay = lending.getFineValuePerDayInCents();
        this.cents = Math.max(0, daysDelayed * finePerDay);
    }
    
    // Explicit getter just in case Lombok @Getter didn't cover it properly for the mapper
    public int getCents() {
        return cents;
    }
}