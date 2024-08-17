package senior.copycoders.project.api.factories;


import org.springframework.stereotype.Component;
import senior.copycoders.project.api.dto.PaymentDto;
import senior.copycoders.project.api.dto.PaymentWithIdCreditDto;
import senior.copycoders.project.store.entities.PaymentEntity;

import java.util.List;

@Component
public class PaymentDtoWithIdCreditDtoFactory {


    public PaymentWithIdCreditDto makePaymentWithIdCreditDto(Long id, List<PaymentDto> payments) {
        return PaymentWithIdCreditDto.builder()
                .creditId(id)
                .payments(payments)
                .build();
    }


}
