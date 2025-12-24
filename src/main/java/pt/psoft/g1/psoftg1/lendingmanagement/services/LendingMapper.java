package pt.psoft.g1.psoftg1.lendingmanagement.services;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.readermanagement.services.ReaderService;

@Mapper(componentModel = "spring", uses = {ReaderService.class})
public abstract class LendingMapper {

    @Mapping(target = "photo", ignore = true)
    @Mapping(target = "fine", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "lendingNumber", ignore = true)
    @Mapping(target = "bookIsbn", ignore = true)
    @Mapping(target = "bookTitle", ignore = true)
    @Mapping(target = "readerDetails", ignore = true)
    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "limitDate", ignore = true)
    @Mapping(target = "returnedDate", ignore = true)
    // Removed pk, daysDelayed, daysUntilReturn as they are not settable properties
    public abstract void update(SetLendingReturnedRequest request, @MappingTarget Lending lending);
}