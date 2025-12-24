package pt.psoft.g1.psoftg1.configuration;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import pt.psoft.g1.psoftg1.lendingmanagement.api.BookEventRabbitmqReceiver;
import pt.psoft.g1.psoftg1.lendingmanagement.api.LendingEventRabbitmqReceiver;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.BookDetailsRepository;
import pt.psoft.g1.psoftg1.lendingmanagement.services.LendingService;
import pt.psoft.g1.psoftg1.readermanagement.api.ReaderEventRabbitmqReceiver;
import pt.psoft.g1.psoftg1.readermanagement.services.ReaderService;
import pt.psoft.g1.psoftg1.shared.model.BookEvents;
import pt.psoft.g1.psoftg1.shared.model.LendingEvents;
import pt.psoft.g1.psoftg1.shared.model.ReaderEvents;

@Profile("!test")
@Configuration
public class RabbitmqClientConfig {

    @Bean(name = "directExchangeLendings")
    public DirectExchange directLendings() { return new DirectExchange("LMS.lendings"); }

    @Bean(name = "directExchangeUsers")
    public DirectExchange directUsers() { return new DirectExchange("LMS.users"); }

    @Bean(name = "bookDirectExchange")
    public DirectExchange directBooks() { return new DirectExchange("LMS.books"); }

    @Configuration
    static class ReceiverConfig {
        @Bean(name = "autoDeleteQueue_Lending_Created")
        public Queue autoDeleteQueue_Lending_Created() { return new AnonymousQueue(); }
        // ... (other queues defined similarly) ...
        @Bean(name = "autoDeleteQueue_Lending_Updated")
        public Queue autoDeleteQueue_Lending_Updated() { return new AnonymousQueue(); }
        @Bean(name = "autoDeleteQueue_Lending_With_Recommendation")
        public Queue autoDeleteQueue_Lending_With_Recommendation() { return new AnonymousQueue(); }
        @Bean(name = "autoDeleteQueue_Lending_Recommendation_Failed")
        public Queue autoDeleteQueue_Lending_Recommendation_Failed() { return new AnonymousQueue(); }
        @Bean(name = "autoDeleteQueue_Reader_Created")
        public Queue autoDeleteQueue_Reader_Created() { return new AnonymousQueue(); }
        @Bean(name = "autoDeleteQueue_Reader_Updated")
        public Queue autoDeleteQueue_Reader_Updated() { return new AnonymousQueue(); }
        @Bean(name = "autoDeleteQueue_Book_Created")
        public Queue autoDeleteQueue_Book_Created() { return new AnonymousQueue(); }
        @Bean(name = "autoDeleteQueue_Book_Updated")
        public Queue autoDeleteQueue_Book_Updated() { return new AnonymousQueue(); }

        @Bean
        public Binding binding1(@Qualifier("directExchangeLendings") DirectExchange direct, @Qualifier("autoDeleteQueue_Lending_Created") Queue queue) {
            return BindingBuilder.bind(queue).to(direct).with(LendingEvents.LENDING_CREATED);
        }
        // ... (other bindings) ...
        @Bean
        public Binding binding2(@Qualifier("directExchangeLendings") DirectExchange direct, @Qualifier("autoDeleteQueue_Lending_Updated") Queue queue) {
            return BindingBuilder.bind(queue).to(direct).with(LendingEvents.LENDING_UPDATED);
        }
        @Bean
        public Binding binding3(@Qualifier("directExchangeLendings") DirectExchange direct, @Qualifier("autoDeleteQueue_Lending_With_Recommendation") Queue queue) {
            return BindingBuilder.bind(queue).to(direct).with(LendingEvents.LENDING_UPDATED_WITH_RECOMMENDATION);
        }
        @Bean
        public Binding binding10(@Qualifier("directExchangeLendings") DirectExchange direct, @Qualifier("autoDeleteQueue_Lending_Recommendation_Failed") Queue queue) {
            return BindingBuilder.bind(queue).to(direct).with(LendingEvents.LENDING_RECOMMENDATION_FAILED);
        }
        @Bean
        public Binding binding4(@Qualifier("directExchangeUsers") DirectExchange direct, @Qualifier("autoDeleteQueue_Reader_Created") Queue queue) {
            return BindingBuilder.bind(queue).to(direct).with(ReaderEvents.READER_CREATED);
        }
        @Bean
        public Binding binding5(@Qualifier("directExchangeUsers") DirectExchange direct, @Qualifier("autoDeleteQueue_Reader_Updated") Queue queue) {
            return BindingBuilder.bind(queue).to(direct).with(ReaderEvents.READER_UPDATED);
        }
        @Bean
        public Binding binding6(@Qualifier("bookDirectExchange") DirectExchange direct, @Qualifier("autoDeleteQueue_Book_Created") Queue queue) {
            return BindingBuilder.bind(queue).to(direct).with(BookEvents.BOOK_CREATED);
        }
        @Bean
        public Binding binding7(@Qualifier("bookDirectExchange") DirectExchange direct, @Qualifier("autoDeleteQueue_Book_Updated") Queue queue) {
            return BindingBuilder.bind(queue).to(direct).with(BookEvents.BOOK_UPDATED);
        }

        @Bean(name = "LendingEventRabbitmqReceiver")
        public LendingEventRabbitmqReceiver lendingReceiver(LendingService lendingService) {
            return new LendingEventRabbitmqReceiver(lendingService);
        }

        @Bean(name = "ReaderEventRabbitmqReceiver")
        public ReaderEventRabbitmqReceiver readerReceiver(ReaderService readerService) {
            return new ReaderEventRabbitmqReceiver(readerService);
        }

        @Bean(name = "bookReceiver")
        public BookEventRabbitmqReceiver bookReceiver(BookDetailsRepository bookDetailsRepository) {
            return new BookEventRabbitmqReceiver(bookDetailsRepository);
        }
    }
}