package pt.psoft.g1.psoftg1.lendingmanagement.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pt.psoft.g1.psoftg1.lendingmanagement.services.CreateLendingRequest;
import pt.psoft.g1.psoftg1.lendingmanagement.services.LendingService;
import pt.psoft.g1.psoftg1.lendingmanagement.services.SetLendingReturnedRequest;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class LendingEventRabbitmqReceiver {

    private final LendingService lendingService;

    @RabbitListener(queues = "#{autoDeleteQueue_Lending_Created.name}")
    @Transactional
    public void receiveLendingCreated(Message msg) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = new String(msg.getBody(), StandardCharsets.UTF_8);
            LendingViewAMQP view = mapper.readValue(json, LendingViewAMQP.class);

            System.out.println(" [x] Received Lending Created: " + view.getLendingNumber());

            CreateLendingRequest request = new CreateLendingRequest();
            request.setIsbn(view.getIsbn());
            request.setReaderNumber(view.getReaderNumber());
            
            try {
                lendingService.create(request);
            } catch (Exception e) {
                System.out.println(" [!] Lending already exists or error: " + e.getMessage());
            }
        } catch (Exception ex) {
            System.err.println(" [!] Exception receiving lending created: " + ex.getMessage());
        }
    }

    @RabbitListener(queues = "#{autoDeleteQueue_Lending_Updated.name}")
    @Transactional
    public void receiveLendingUpdated(Message msg) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = new String(msg.getBody(), StandardCharsets.UTF_8);
            LendingViewAMQP view = mapper.readValue(json, LendingViewAMQP.class);

            System.out.println(" [x] Received Lending Updated: " + view.getLendingNumber());

            if (view.getReturnedDate() != null) {
                SetLendingReturnedRequest request = new SetLendingReturnedRequest();
                request.setCommentary(view.getCommentary());
                
                // FIXED: Pass version from view (default to 0 or handled inside service if null)
                long version = view.getVersion() != null ? view.getVersion() : 0L;
                lendingService.setReturned(view.getLendingNumber(), request, version);
            }
        } catch (Exception ex) {
            System.err.println(" [!] Exception receiving lending updated: " + ex.getMessage());
        }
    }
}