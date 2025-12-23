package pt.psoft.g1.psoftg1.lendingmanagement.publishers;

import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.lendingmanagement.services.SetLendingReturnedWithRecommendationRequest;

public interface LendingEventsPublisher {
    void sendLendingCreated(Lending lending);
    void sendLendingUpdated(Lending lending, Long currentVersion);

    void sendLendingWithCommentary(Lending updatedLending, long desiredVersion, SetLendingReturnedWithRecommendationRequest resource);
}
