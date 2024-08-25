package senior.copycoders.project.api.controllers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import senior.copycoders.project.api.dto.AckDto;
import senior.copycoders.project.api.dto.CreditDto;
import senior.copycoders.project.api.dto.PaymentWithCreditDto;
import senior.copycoders.project.api.services.CreditService;
import senior.copycoders.project.store.enums.CreditConstants;


import java.math.BigDecimal;
import java.util.List;

@RestController
@Tag(name = "Кредиты", description = "Взаимодействие с кредитами")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CreditController {
    CreditService creditService;

    @PostMapping("/api/credit")
    @Operation(
            summary = "Инициализация кредита и всех платежей к нему"
    )
    public PaymentWithCreditDto createCredit(@RequestParam(name = "dateOfFirstPayment") @Parameter(description = "дата первого платежа (формат yyyy-MM-dd)") String dateOfFirstPayment, @RequestParam(name = "initial_payment") @Parameter(description = "начальный платёж (неотрицательное вещественное число, до двух знаков после запятой)") Double initialPayment, @RequestParam(name = "credit_amount") @Parameter(description = "сумма кредита (положительное вещественное число, до двух знаков после запятой, min = 200_000, max = 30_000_000)") Double creditAmount, @RequestParam(name = "percent_rate") @Parameter(description = "годовая процентная ставка (положительное вещественное число, до двух знаков после запятой, min = 0% (не включительно), max = 18%)")  Double percentRate, @RequestParam(name = "credit_period") @Parameter(description = "срок кредитования в месяцах (положительное целое число, min = 12 месяцев(1 год), max = 360(30 лет))") Integer creditPeriod, @RequestParam(name = "typeOfCredit") @Parameter(description = "тип кредита, false - аннуитет, true - дифференцированный") Boolean type) {

        // Получаем из creditService объект нужного класса
        return creditService.calculateSchedule(dateOfFirstPayment, BigDecimal.valueOf(initialPayment), BigDecimal.valueOf(creditAmount), BigDecimal.valueOf(percentRate), creditPeriod, type);
    }


    @GetMapping("/api/credit")
    @Operation(
            summary = "Получение списка всех кредитов"
    )
    public List<CreditDto> getAllCredit() {

        // получаем список всех кредитов в БД
        return creditService.getAllCredit();
    }


    @DeleteMapping("/api/credit/{credit_id}")
    @Operation(
            summary = "Удаление кредита по id (вместе с его платежами)"
    )
    public AckDto deleteCreditById(@PathVariable(name = "credit_id") @Parameter() Long creditId) {
        creditService.deleteCreditAndPayments(creditId);

        return AckDto.makeDefault(true);
    }

    @GetMapping("/api/credit/min-sum")
    @Operation(
            summary = "Получение минимальной суммы кредита"
    )
    public int getMinSum(){
        return CreditConstants.MIN_CREDIT_AMOUNT.getValue();
    }

    @GetMapping("/api/credit/max-sum")
    @Operation(
            summary = "Получение максимальной суммы кредита"
    )
    public int getMaxSum(){
        return CreditConstants.MAX_CREDIT_AMOUNT.getValue();
    }

    @GetMapping("/api/credit/max-rate")
    @Operation(
            summary = "Получение максимальной процентной ставки"
    )
    public int getMaxInterestRate(){
        return CreditConstants.MAX_INTEREST_RATE.getValue();
    }

    @GetMapping("/api/credit/min-rate")
    @Operation(
            summary = "Получение минимальной процентной ставки"
    )
    public int getMinInterestRate(){
        return CreditConstants.MIN_INTEREST_RATE.getValue();
    }






//    @PatchMapping("/api/credit/{credit_id}")
//    @Operation(
//            summary = "Изменение каких-то параметров кредита по id и перерасчёт всех платежей. Введите id кредита и параметры, которые хотите изменить"
//    )
//    public PaymentWithCreditDto changeCredit(@PathVariable(name = "credit_id") @Parameter(description = "id кредита") Long creditId, @RequestParam(name = "initial_payment", required = false) @Parameter(description = "начальный платёж (неотрицательное вещественное число, до двух знаков после запятой, необязательное поле)") Optional<Double> optionalInitialPayment, @RequestParam(name = "credit_amount", required = false) @Parameter(description = "сумма кредита (положительное вещественное число, до двух знаков после запятой, необязательное поле)") Optional<Double> optionalCreditAmount, @RequestParam(name = "percent_rate", required = false) @Parameter(description = "годовая процентная ставка (положительное вещественное число, до двух знаков после запятой, необязательное поле)") Optional<Double> optionalPercentRate, @RequestParam(name = "credit_period", required = false) @Parameter(description = "срок кредитования в месяцах (положительное целое число, необязательное поле)") Optional<Integer> optionalCreditPeriod) {
//
//
//        return creditService.changeCreditAndPayments(creditId, optionalInitialPayment.map(BigDecimal::valueOf), optionalCreditAmount.map(BigDecimal::valueOf), optionalPercentRate.map(BigDecimal::valueOf), optionalCreditPeriod);
//
//    }

}
