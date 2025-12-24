package pt.psoft.g1.psoftg1.readermanagement.services;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import pt.psoft.g1.psoftg1.readermanagement.api.ReaderView;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.shared.api.MapperInterface;

@Mapper(componentModel = "spring")
public abstract class ReaderMapper extends MapperInterface {

    @Mapping(target = "readerNumber", expression = "java(readerDetails.getReaderNumber().toString())")
    @Mapping(target = "birthDate", expression = "java(readerDetails.getBirthDate().toString())")
    @Mapping(target = "phoneNumber", expression = "java(readerDetails.getPhoneNumber().toString())")
    @Mapping(target = "fullName", source = "fullName")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "photoUrl", ignore = true)
    public abstract ReaderView toReaderView(ReaderDetails readerDetails);

    @Mapping(target = "readerNumber", ignore = true)
    @Mapping(target = "birthDate", ignore = true)
    @Mapping(target = "photo", ignore = true)
    // Removed 'pk' ignore as it caused unknown property error
    @Mapping(target = "version", ignore = true)
    public abstract void updateReader(UpdateReaderRequest request, @MappingTarget ReaderDetails readerDetails);
}