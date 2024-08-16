package senior.copycoders.project.api.factories;


import org.springframework.stereotype.Component;
import senior.copycoders.project.api.dto.PaymentDto;
import senior.copycoders.project.store.entities.PaymentEntity;

@Component
public class PaymentDtoFactory {

    public PaymentDto makePaymentDto(PaymentEntity paymentEntity) {
        return PaymentDto.builder()
                .paymentNumber(paymentEntity.getPaymentNumber())
                .paymentDate(paymentEntity.getPaymentDate())
                .paymentAmount(paymentEntity.getPaymentAmount())
                .percent(paymentEntity.getPercent())
                .repaymentCredit(paymentEntity.getRepaymentCredit())
                .remainingCredit(paymentEntity.getRemainingCredit())
                .build();
    }
}
