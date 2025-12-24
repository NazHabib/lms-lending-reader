package pt.psoft.g1.psoftg1.lendingmanagement.services;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchLendingQuery {
    private String readerNumber;
    private String isbn;
    private Boolean returned;
    private String startDate;
    private String endDate;
}