package pt.psoft.g1.psoftg1.lendingmanagement.api;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.shared.api.MapperInterface;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class LendingViewAMQPMapper extends MapperInterface {

    @Mapping(target = "lendingNumber", source = "lendingNumber")
    @Mapping(target = "version", source = "version")
    @Mapping(target = "commentary", source = "commentary")
    @Mapping(target = "returnedDate", source = "returnedDate")
    @Mapping(target = "isbn", source = "bookIsbn")
    // Fix: Use toString() for ReaderNumber
    @Mapping(target = "readerNumber", expression = "java(lending.getReaderDetails().getReaderNumber().toString())")
    @Mapping(target = "recommended", ignore = true)
    public abstract LendingViewAMQP toLendingViewAMQP(Lending lending);

    public abstract List<LendingViewAMQP> toLendingViewAMQP(List<Lending> lendings);
}