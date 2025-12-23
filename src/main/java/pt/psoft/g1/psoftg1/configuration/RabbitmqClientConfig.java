package pt.psoft.g1.psoftg1.configuration;


import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import pt.psoft.g1.psoftg1.lendingmanagement.api.LendingEventRabbitmqReceiver;
import pt.psoft.g1.psoftg1.shared.model.LendingEvents;
import pt.psoft.g1.psoftg1.lendingmanagement.services.LendingService;
import pt.psoft.g1.psoftg1.readermanagement.api.ReaderEventRabbitmqReceiver;
import pt.psoft.g1.psoftg1.readermanagement.services.ReaderService;
import pt.psoft.g1.psoftg1.shared.model.BookEvents;
import pt.psoft.g1.psoftg1.shared.model.ReaderEvents;

@Profile("!test")
@Configuration
public class RabbitmqClientConfig {
    @Bean(name = "directExchangeLendings")
    public DirectExchange directLendings() {
        return new DirectExchange("LMS.lendings");
    }

    @Bean(name = "directExchangeUsers")
    public DirectExchange directUsers() {
        return new DirectExchange("LMS.users");
    }

    @Bean(name = "bookDirectExchange")
    public DirectExchange directBooks() {
        return new DirectExchange("LMS.books");
    }

    private static class ReceiverConfig {

        @Bean(name = "autoDeleteQueue_Lending_Created")
        public Queue autoDeleteQueue_Lending_Created() {
            System.out.println("autoDeleteQueue_Lending_Created created!");
            return new AnonymousQueue();
        }

        @Bean(name = "autoDeleteQueue_Lending_Updated")
        public Queue autoDeleteQueue_Lending_Updated() {
            System.out.println("autoDeleteQueue_Lending_Updated updated!");
            return new AnonymousQueue();
        }

        @Bean(name = "autoDeleteQueue_Lending_With_Recommendation")
        public Queue autoDeleteQueue_Lending_With_Recommendation() {
            System.out.println("autoDeleteQueue_Lending_With_Recommendation updated!");
            return new AnonymousQueue();
        }

        @Bean(name = "autoDeleteQueue_Lending_Recommendation_Failed")
        public Queue autoDeleteQueue_Lending_Recommendation_Failed() {
            return new AnonymousQueue();
        }


        @Bean
        public Binding binding1(@Qualifier("directExchangeLendings") DirectExchange direct,
                                @Qualifier("autoDeleteQueue_Lending_Created") Queue autoDeleteQueue_Lending_Created) {
            return BindingBuilder.bind(autoDeleteQueue_Lending_Created)
                    .to(direct)
                    .with(LendingEvents.LENDING_CREATED);
        }

        @Bean
        public Binding binding2(@Qualifier("directExchangeLendings") DirectExchange direct,
                                Queue autoDeleteQueue_Lending_Updated) {
            return BindingBuilder.bind(autoDeleteQueue_Lending_Updated)
                    .to(direct)
                    .with(LendingEvents.LENDING_UPDATED);
        }

        @Bean
        public Binding binding3(@Qualifier("directExchangeLendings") DirectExchange direct,
                                Queue autoDeleteQueue_Lending_With_Recommendation) {
            return BindingBuilder.bind(autoDeleteQueue_Lending_With_Recommendation)
                    .to(direct)
                    .with(LendingEvents.LENDING_UPDATED_WITH_RECOMMENDATION);
        }

        @Bean
        public Binding binding10(@Qualifier("directExchangeLendings") DirectExchange direct,
                                 Queue autoDeleteQueue_Lending_Recommendation_Failed) {
            return BindingBuilder.bind(autoDeleteQueue_Lending_Recommendation_Failed)
                    .to(direct)
                    .with(LendingEvents.LENDING_RECOMMENDATION_FAILED);
        }

        @Bean(name = "LendingEventRabbitmqReceiver")
        public LendingEventRabbitmqReceiver receiver(LendingService lendingService, @Qualifier("autoDeleteQueue_Lending_Created") Queue autoDeleteQueue_Lending_Created) {
            return new LendingEventRabbitmqReceiver(lendingService);
        }


        /* ------- READER ------- */

        @Bean(name = "autoDeleteQueue_Reader_Created")
        public Queue autoDeleteQueue_Reader_Created() {
            System.out.println("autoDeleteQueue_Reader_Created created!");
            return new AnonymousQueue();
        }

        @Bean(name = "autoDeleteQueue_Reader_Updated")
        public Queue autoDeleteQueue_Reader_Updated() {
            System.out.println("autoDeleteQueue_Reader_Updated updated!");
            return new AnonymousQueue();
        }

        @Bean
        public Binding binding4(@Qualifier("directExchangeUsers") DirectExchange direct,
                                @Qualifier("autoDeleteQueue_Reader_Created") Queue autoDeleteQueue_Reader_Created) {
            return BindingBuilder.bind(autoDeleteQueue_Reader_Created)
                    .to(direct)
                    .with(ReaderEvents.READER_CREATED);
        }

        @Bean
        public Binding binding5(@Qualifier("directExchangeUsers") DirectExchange direct,
                                @Qualifier("autoDeleteQueue_Reader_Updated") Queue autoDeleteQueue_Reader_Updated) {
            return BindingBuilder.bind(autoDeleteQueue_Reader_Updated)
                    .to(direct)
                    .with(ReaderEvents.READER_UPDATED);
        }

        @Bean(name = "ReaderEventRabbitmqReceiver")
        public ReaderEventRabbitmqReceiver receiver(ReaderService ReaderService, @Qualifier("autoDeleteQueue_Reader_Created") Queue autoDeleteQueue_Reader_Created) {
            return new ReaderEventRabbitmqReceiver(ReaderService);
        }


        /* ------- BOOK ------- */

        @Bean(name = "autoDeleteQueue_Book_Created")
        public Queue autoDeleteQueue_Book_Created() {

            System.out.println("autoDeleteQueue_Book_Created created!");
            return new AnonymousQueue();
        }

        @Bean(name = "autoDeleteQueue_Book_Updated")
        public Queue autoDeleteQueue_Book_Updated() {
            return new AnonymousQueue();
        }

        @Bean
        public Binding binding6(@Qualifier("bookDirectExchange") DirectExchange direct,
                                @Qualifier("autoDeleteQueue_Book_Created") Queue autoDeleteQueue_Book_Created) {
            return BindingBuilder.bind(autoDeleteQueue_Book_Created)
                    .to(direct)
                    .with(BookEvents.BOOK_CREATED);
        }

        @Bean
        public Binding binding7(@Qualifier("bookDirectExchange") DirectExchange direct,
                                @Qualifier("autoDeleteQueue_Book_Updated") Queue autoDeleteQueue_Book_Updated) {
            return BindingBuilder.bind(autoDeleteQueue_Book_Updated)
                    .to(direct)
                    .with(BookEvents.BOOK_UPDATED);
        }

        @Bean(name = "bookReceiver")
        public BookEventRabbitmqReceiver receiver(BookService bookService, @Qualifier("autoDeleteQueue_Book_Created") Queue autoDeleteQueue_Book_Created) {
            return new BookEventRabbitmqReceiver(bookService);
        }
    }
}
