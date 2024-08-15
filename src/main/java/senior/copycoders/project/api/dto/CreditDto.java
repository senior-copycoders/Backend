package senior.copycoders.project.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreditDto {
    @NonNull
    Long id;

    @NonNull
    @JsonProperty("credit_amount")
    Double creditAmount;

    @NonNull
    @JsonProperty("percent_rate")
    Double percentRate;

    @NonNull
    @JsonProperty("credit_period")
    Integer creditPeriod;

    @NonNull
    Double payment;
}

