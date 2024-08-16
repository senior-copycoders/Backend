package senior.copycoders.project.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreditDto {
    @NonNull
    Long id;

    @NonNull
    @JsonProperty("initial_payment")
    BigDecimal initialPayment;

    @NonNull
    @JsonProperty("credit_amount")
    BigDecimal creditAmount;

    @NonNull
    @JsonProperty("percent_rate")
    BigDecimal percentRate;

    @NonNull
    @JsonProperty("credit_period")
    Integer creditPeriod;

    @NonNull
    BigDecimal payment;
}

