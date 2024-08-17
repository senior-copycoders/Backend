package senior.copycoders.project.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;



@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "сущность платёж")
public class PaymentDto {
    @NonNull
    @JsonProperty("payment_number")
    @Schema(description = "номер платежа")
    Integer paymentNumber;


    @NonNull
    @JsonProperty("payment_date")
    @Schema(description = "дата платежа")
    LocalDate paymentDate;

    @NonNull
    @JsonProperty("payment_amount")
    @Schema(description = "сумма платежа")
    BigDecimal paymentAmount;


    @NonNull
    @Schema(description = "сумма из платежа на погашение процентов на текущий месяц")
    BigDecimal percent;

    @NonNull
    @JsonProperty("repayment_credit")
    @Schema(description = "сумма из платежа на погашение основного долга")
    BigDecimal repaymentCredit;


    @NonNull
    @JsonProperty("remaining_credit")
    @Schema(description = "остаток долга")
    BigDecimal remainingCredit;
}
