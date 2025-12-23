package pt.psoft.g1.psoftg1.lendingmanagement.infrastructure.publishers.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import pt.psoft.g1.psoftg1.lendingmanagement.api.LendingViewAMQP;
import pt.psoft.g1.psoftg1.lendingmanagement.api.LendingViewAMQPMapper;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.lendingmanagement.services.SetLendingReturnedWithRecommendationRequest;
import pt.psoft.g1.psoftg1.shared.model.LendingEvents;
import pt.psoft.g1.psoftg1.lendingmanagement.publishers.LendingEventsPublisher;

@Service
@RequiredArgsConstructor
public class LendingEventsRabbitmqPublisherImpl implements LendingEventsPublisher {

    @Autowired
    private RabbitTemplate template;
    @Autowired
    @Qualifier("directExchangeLendings")
    private DirectExchange direct;
    private final LendingViewAMQPMapper lendingViewAMQPMapper;

    @Override
    public void sendLendingCreated(Lending lending) {
        sendLendingEvent(lending, lending.getVersion(), LendingEvents.LENDING_CREATED);
    }

    @Override
    public void sendLendingUpdated(Lending lending, Long currentVersion) {
        sendLendingEvent(lending, currentVersion, LendingEvents.LENDING_UPDATED);
    }



    public void sendLendingEvent(Lending lending, Long currentVersion, String lendingEventType) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            LendingViewAMQP lendingViewAMQP = lendingViewAMQPMapper.toLendingViewAMQP(lending);
            lendingViewAMQP.setVersion(currentVersion);
            lendingViewAMQP.setIsbn(lending.getBook().getIsbn());
            lendingViewAMQP.setReaderNumber(lending.getReaderDetails().getReaderNumber());

            String jsonString = objectMapper.writeValueAsString(lendingViewAMQP);

            this.template.convertAndSend(direct.getName(), lendingEventType, jsonString);

            System.out.println(" [x] Sent '" + jsonString + "'");
        }
        catch( Exception ex ) {
            System.out.println(" [x] Exception sending lending event: '" + ex.getMessage() + "'");
        }
    }

    @Override
    public void sendLendingWithCommentary(Lending updatedLending, long desiredVersion, SetLendingReturnedWithRecommendationRequest resource) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            LendingViewAMQP lendingViewAMQP = lendingViewAMQPMapper.toLendingViewAMQP(updatedLending);
            lendingViewAMQP.setVersion(desiredVersion);
            lendingViewAMQP.setIsbn(updatedLending.getBook().getIsbn());
            lendingViewAMQP.setReaderNumber(updatedLending.getReaderDetails().getReaderNumber());
            lendingViewAMQP.setCommentary(resource.getCommentary());
            lendingViewAMQP.setIsRecommended(resource.getIsRecommended());

            String jsonString = objectMapper.writeValueAsString(lendingViewAMQP);

            this.template.convertAndSend(direct.getName(), LendingEvents.LENDING_UPDATED_WITH_RECOMMENDATION, jsonString);

            System.out.println(" [x] Sent '" + jsonString + "'");
        }
        catch( Exception ex ) {
            System.out.println(" [x] Exception sending lending with recommendation event: '" + ex.getMessage() + "'");
        }
    }
}
