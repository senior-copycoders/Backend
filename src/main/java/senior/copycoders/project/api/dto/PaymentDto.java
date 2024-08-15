package senior.copycoders.project.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;


import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentDto {
    @NonNull
    Long id;

    @NonNull
    @JsonProperty("payment_number")
    Integer paymentNumber;


    @NonNull
    @JsonProperty("payment_date")
    LocalDateTime paymentDate;

    @NonNull
    @JsonProperty("payment_amount")
    Double paymentAmount;


    @NonNull
    Double percent;

    @NonNull
    @JsonProperty("repayment_credit")
    Double repaymentCredit;


    @NonNull
    @JsonProperty("remaining_credit")
    Double remainingCredit;
}
