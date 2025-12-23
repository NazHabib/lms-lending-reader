package pt.psoft.g1.psoftg1.lendingmanagement.services;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.readermanagement.services.ReaderService;

/**
 * Brief guide:
 * <a href="https://www.baeldung.com/mapstruct">https://www.baeldung.com/mapstruct</a>
 * */
@Mapper(componentModel = "spring", uses = {ReaderService.class})
public abstract class LendingMapper {
    
    // Removed BookService from 'uses' as it does not exist in this microservice.
    // The mapping logic for Book (if any) should now rely on strings (ISBN/Title) 
    // or be handled manually if complex logic is needed.
    
    public abstract void update(SetLendingReturnedRequest request, @MappingTarget Lending lending);

}