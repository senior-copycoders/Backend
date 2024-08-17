package senior.copycoders.project.api.factories;


import org.springframework.stereotype.Component;
import senior.copycoders.project.api.dto.CreditDto;
import senior.copycoders.project.api.dto.PaymentDto;
import senior.copycoders.project.api.dto.PaymentWithCreditDto;


import java.util.List;

@Component
public class PaymentWithCreditDtoFactory {


    public PaymentWithCreditDto makePaymentWithIdCreditDto(CreditDto credit, List<PaymentDto> payments) {
        return PaymentWithCreditDto.builder()
                .credit(credit)
                .payments(payments)
                .build();
    }


}
