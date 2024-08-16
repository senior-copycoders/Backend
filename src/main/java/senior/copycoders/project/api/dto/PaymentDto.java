package senior.copycoders.project.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentDto {
    @NonNull
    @JsonProperty("payment_number")
    Integer paymentNumber;


    @NonNull
    @JsonProperty("payment_date")
    LocalDate paymentDate;

    @NonNull
    @JsonProperty("payment_amount")
    BigDecimal paymentAmount;


    @NonNull
    BigDecimal percent;

    @NonNull
    @JsonProperty("repayment_credit")
    BigDecimal repaymentCredit;


    @NonNull
    @JsonProperty("remaining_credit")
    BigDecimal remainingCredit;
}
