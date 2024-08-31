package senior.copycoders.project.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;
import senior.copycoders.project.store.enums.TypeOfCredit;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "сущность кредит")
public class CreditDto {
    @NonNull
    @Schema(description = "id кредита")
    Long id;

    @NonNull
    @JsonProperty("initial_payment")
    @Schema(description = "начальный платёж")
    BigDecimal initialPayment;

    @NonNull
    @JsonProperty("credit_amount")
    @Schema(description = "сумма кредита")
    BigDecimal creditAmount;

    @NonNull
    @JsonProperty("percent_rate")
    @Schema(description = "годовая процентая ставка")
    BigDecimal percentRate;

    @NonNull
    @JsonProperty("credit_period")
    @Schema(description = "срок кредитования в месяцах")
    Integer creditPeriod;

    @NonNull
    @Schema(description = "ежемесячный платёж")
    BigDecimal payment;

    @NonNull
    @JsonProperty("type_of_credit")
    @Schema(description = "тип кредита (либо аннуитет, либо дифференцированный)")
    TypeOfCredit typeOfCredit;


}

