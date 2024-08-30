package senior.copycoders.project.api.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CreditRequest {

    @NotNull
    @Schema(description = "дата первого платежа (формат yyyy-MM-dd)")
    @JsonProperty("date_of_first_payment")
    private String dateOfFirstPayment;

    @NotNull
    @Schema(description = "начальный платёж (неотрицательное вещественное число, до двух знаков после запятой)")
    @JsonProperty("initial_payment")
    private Double initialPayment;

    @NotNull
    @Schema(description = "сумма кредита (положительное вещественное число, до двух знаков после запятой, min = 200_000, max = 30_000_000)")
    @JsonProperty("credit_amount")
    private Double creditAmount;

    @NotNull
    @Schema(description = "годовая процентная ставка (положительное вещественное число, до двух знаков после запятой, min = 0% (не включительно), max = 18%)")
    @JsonProperty("percent_rate")
    private Double percentRate;

    @NotNull
    @Schema(description = "срок кредитования в месяцах (положительное целое число, min = 12 месяцев(1 год), max = 360(30 лет))")
    @JsonProperty("credit_period")
    private Integer creditPeriod;

    @NotNull
    @Schema(description = "тип кредита, false - аннуитет, true - дифференцированный")
    @JsonProperty("type_of_credit")
    private Boolean typeOfCredit;

    // Геттеры и сеттеры
}