package senior.copycoders.project.api.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CreditRequest {

    @NotNull
    @Schema(description = "дата первого платежа (формат yyyy-MM-dd)")
    private String dateOfFirstPayment;

    @NotNull
    @Schema(description = "начальный платёж (неотрицательное вещественное число, до двух знаков после запятой)")
    private Double initialPayment;

    @NotNull
    @Schema(description = "сумма кредита (положительное вещественное число, до двух знаков после запятой, min = 200_000, max = 30_000_000)")
    private Double creditAmount;

    @NotNull
    @Schema(description = "годовая процентная ставка (положительное вещественное число, до двух знаков после запятой, min = 0% (не включительно), max = 18%)")
    private Double percentRate;

    @NotNull
    @Schema(description = "срок кредитования в месяцах (положительное целое число, min = 12 месяцев(1 год), max = 360(30 лет))")
    private Integer creditPeriod;

    @NotNull
    @Schema(description = "тип кредита, false - аннуитет, true - дифференцированный")
    private Boolean typeOfCredit;

    // Геттеры и сеттеры
}