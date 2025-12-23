//package pt.psoft.g1.psoftg1.readermanagement.services;
//
//import org.mapstruct.Mapper;
//import org.mapstruct.Mapping;
//import org.mapstruct.Named;
//import org.springframework.web.multipart.MultipartFile;
//
//import pt.psoft.g1.psoftg1.readermanagement.api.ReaderViewAMQP;
//import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
////import pt.psoft.g1.psoftg1.shared.model.Photo;
////import pt.psoft.g1.psoftg1.usermanagement.model.Reader;
////import pt.psoft.g1.psoftg1.usermanagement.services.UserService;
//
//import java.nio.file.Paths;
//import java.util.List;
//
///**
// * Brief guide:
// * <a href="https://www.baeldung.com/mapstruct">https://www.baeldung.com/mapstruct</a>
// * */
//@Mapper(componentModel = "spring", uses = {ReaderService.class/*, UserService.class*/})
//public abstract class ReaderMapper {
////
////    @Mapping(target = "username", source = "username")
////    @Mapping(target = "password", source = "password")
////    @Mapping(target = "name", source = "fullName")
//
//
//    @Mapping(target = "photo", source = "photoURI")
//    public abstract ReaderDetails createReaderDetails(int readerNumber, /*Reader reader,*/ CreateReaderRequest request, String photoURI/*, List<Genre> interestList*/);
//
//}
