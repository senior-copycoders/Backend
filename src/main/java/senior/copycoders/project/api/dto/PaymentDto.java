package senior.copycoders.project.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;
import senior.copycoders.project.store.enums.StatusOfPaymentOrCredit;

import java.math.BigDecimal;
import java.time.LocalDate;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "сущность платёж")
public class PaymentDto implements Comparable<PaymentDto> {
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
    @JsonProperty("after_payment")
    @Schema(description = "долг после платежа")
    BigDecimal afterPayment;

    @NonNull
    @Schema(description = "статус платежа, PAID - платеж по кредиту был успешно произведен, PENDING - платеж по кредиту ожидается")
    StatusOfPaymentOrCredit status;

    @NonNull
    @Schema(description = "долг до платежа")
    @JsonProperty("before_payment")
    BigDecimal beforePayment;

    @Override
    public int compareTo(PaymentDto o) {
        return Integer.compare(paymentNumber, o.getPaymentNumber());
    }
}
