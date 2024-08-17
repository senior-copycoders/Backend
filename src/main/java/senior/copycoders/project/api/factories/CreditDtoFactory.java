package senior.copycoders.project.api.factories;

import org.springframework.stereotype.Component;
import senior.copycoders.project.api.dto.CreditDto;
import senior.copycoders.project.store.entities.CreditEntity;

@Component
public class CreditDtoFactory {

    public CreditDto makeCreditDto(CreditEntity creditEntity) {

        return CreditDto.builder()
                .id(creditEntity.getId())
                .initialPayment(creditEntity.getInitialPayment())
                .creditAmount(creditEntity.getCreditAmount())
                .percentRate(creditEntity.getPercentRate())
                .creditPeriod(creditEntity.getCreditPeriod())
                .payment(creditEntity.getPayment())
                .build();
    }
}
