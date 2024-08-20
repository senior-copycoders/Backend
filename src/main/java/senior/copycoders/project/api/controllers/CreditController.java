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


import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

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
    public PaymentWithCreditDto createCredit(@RequestParam(name = "initial_payment") @Parameter(description = "начальный платёж (неотрицательное вещественное число, до двух знаков после запятой)") Double initialPayment, @RequestParam(name = "credit_amount") @Parameter(description = "сумма кредита (положительное вещественное число, до двух знаков после запятой)") Double creditAmount, @RequestParam(name = "percent_rate") @Parameter(description = "годовая процентная ставка (положительное вещественное число, до двух знаков после запятой)") Double percentRate, @RequestParam(name = "credit_period") @Parameter(description = "срок кредитования в месяцах (положительное целое число)") Integer creditPeriod) {

        // Получаем из creditService объекта нужно класса
        return creditService.calculateAndSave(BigDecimal.valueOf(initialPayment), BigDecimal.valueOf(creditAmount), BigDecimal.valueOf(percentRate), creditPeriod);
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
