package senior.copycoders.project.api.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;
import senior.copycoders.project.store.enums.CreditConstants;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "константы для кредита")
public class CreditConstantsDto {

    @NonNull
    @JsonProperty("max_credit_amount")
    @Schema(description = "максимальная сумма кредита")
    Integer maxCreditAmount;

    @NonNull
    @JsonProperty("min_credit_amount")
    @Schema(description = "минимальная сумма кредита")
    Integer minCreditAmount;

    @NonNull
    @JsonProperty("max_interest_rate")
    @Schema(description = "максимальная процентная ставка")
    Integer maxInterestRate;

    @NonNull
    @JsonProperty("min_interest_rate")
    @Schema(description = "минимальная сумма кредита")
    Integer minInterestRate;

    @NonNull
    @JsonProperty("max_credit_period")
    @Schema(description = "максимальный срок кредитования")
    Integer maxCreditPeriod;

    @NonNull
    @JsonProperty("min_credit_period")
    @Schema(description = "максимальный срок кредитования")
    Integer minCreditPeriod;


    public CreditConstantsDto() {
        this.maxCreditAmount = CreditConstants.MAX_CREDIT_AMOUNT.getValue();
        this.minCreditAmount = CreditConstants.MIN_CREDIT_AMOUNT.getValue();
        this.maxInterestRate = CreditConstants.MAX_INTEREST_RATE.getValue();
        this.minInterestRate = CreditConstants.MIN_INTEREST_RATE.getValue();
        this.maxCreditPeriod = CreditConstants.MAX_CREDIT_PERIOD.getValue();
        this.minCreditPeriod = CreditConstants.MIN_CREDIT_PERIOD.getValue();
    }


}
