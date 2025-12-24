package pt.psoft.g1.psoftg1.lendingmanagement.infrastructure.publishers.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import pt.psoft.g1.psoftg1.lendingmanagement.api.LendingViewAMQP;
import pt.psoft.g1.psoftg1.lendingmanagement.api.LendingViewAMQPMapper;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.lendingmanagement.publishers.LendingEventsPublisher;
import pt.psoft.g1.psoftg1.lendingmanagement.services.SetLendingReturnedWithRecommendationRequest;
import pt.psoft.g1.psoftg1.shared.model.LendingEvents;

@Service
@RequiredArgsConstructor
public class LendingEventsRabbitmqPublisherImpl implements LendingEventsPublisher {

    private final RabbitTemplate template;
    @Qualifier("directExchangeLendings")
    private final DirectExchange direct;
    private final LendingViewAMQPMapper lendingViewAMQPMapper;

    @Override
    public void sendLendingCreated(Lending lending) {
        sendEvent(lending, LendingEvents.LENDING_CREATED, null);
    }

    @Override
    public void sendLendingUpdated(Lending updatedLending, Long version) {
        sendEvent(updatedLending, LendingEvents.LENDING_UPDATED, version);
    }

    @Override
    public void sendLendingWithCommentary(Lending updatedLending, long desiredVersion, SetLendingReturnedWithRecommendationRequest resource) {
        // Implementation for commentary event
        // Note: The resource data might need to be part of the message if the Queue listener expects it.
        // For now, we send the updated lending view.
        sendEvent(updatedLending, LendingEvents.LENDING_UPDATED_WITH_RECOMMENDATION, desiredVersion);
    }

    private void sendEvent(Lending lending, String routingKey, Long version) {
        try {
            LendingViewAMQP view = lendingViewAMQPMapper.toLendingViewAMQP(lending);
            if (version != null) {
                view.setVersion(version);
            }
            // Manually map fields that might be missing in the mapper for flattened objects
            view.setIsbn(lending.getBookIsbn());
            view.setReaderNumber(lending.getReaderDetails().getReaderNumber().toString());
            
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(view);
            
            template.convertAndSend(direct.getName(), routingKey, json);
            System.out.println(" [x] Sent '" + json + "' to " + routingKey);
        } catch (Exception e) {
            System.err.println(" [!] Error sending event: " + e.getMessage());
        }
    }
}