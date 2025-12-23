package pt.psoft.g1.psoftg1.lendingmanagement.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import pt.psoft.g1.psoftg1.lendingmanagement.services.LendingService;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class LendingEventRabbitmqReceiver {

    private final LendingService lendingService;

    @RabbitListener(queues = "#{autoDeleteQueue_Lending_Created.name}")
    public void receiveLendingCreated(Message msg) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            String jsonReceived = new String(msg.getBody(), StandardCharsets.UTF_8);
            LendingViewAMQP lendingViewAMQP = objectMapper.readValue(jsonReceived, LendingViewAMQP.class);

            System.out.println(" [x] Received Lending Created by AMQP: " + msg + ".");
            try {
                lendingService.create(lendingViewAMQP);
                System.out.println(" [x] New lending inserted from AMQP: " + msg + ".");
            } catch (Exception e) {
                System.out.println(" [x] Lending already exists. No need to store it.");
            }
        }
        catch(Exception ex) {
            System.out.println(" [x] Exception receiving lending event from AMQP: '" + ex.getMessage() + "'");
        }
    }

    @RabbitListener(queues = "#{autoDeleteQueue_Lending_Updated.name}")
    public void receiveLendingUpdated(Message msg) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            String jsonReceived = new String(msg.getBody(), StandardCharsets.UTF_8);
            LendingViewAMQP lendingViewAMQP = objectMapper.readValue(jsonReceived, LendingViewAMQP.class);

            System.out.println(" [x] Received Lending updated by AMQP: " + msg + ".");
            try {
                lendingService.setReturned(lendingViewAMQP);
                System.out.println(" [x] Lending updated from AMQP: " + msg + ".");
            } catch (Exception e) {
                System.out.println(" [x] Lending does not exists or wrong version. Nothing stored.");
            }
        }
        catch(Exception ex) {
            System.out.println(" [x] Exception receiving lending event from AMQP: '" + ex.getMessage() + "'");
        }
    }

    @RabbitListener(queues = "#{autoDeleteQueue_Lending_Recommendation_Failed.name}")
    public void receiveLendingRecommendationFailed(Message msg) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            String jsonReceived = new String(msg.getBody(), StandardCharsets.UTF_8);
            LendingViewAMQP lendingViewAMQP = objectMapper.readValue(jsonReceived, LendingViewAMQP.class);

            System.out.println(" [x] Received Recommendation failed to create by AMQP: " + msg + ". Rollback lending.");
            try {
                lendingService.roolbackReturned(lendingViewAMQP);
                System.out.println(" [x] Lending rollback from AMQP: " + msg + ".");
            } catch (Exception e) {
                System.out.println(" [x] Lending does not exists or wrong version. Nothing changed.");
            }
        }
        catch(Exception ex) {
            System.out.println(" [x] Exception receiving lending rollback event from AMQP: '" + ex.getMessage() + "'");
        }
    }
}
