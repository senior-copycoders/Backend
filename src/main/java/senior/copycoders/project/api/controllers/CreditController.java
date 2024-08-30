package senior.copycoders.project.api.controllers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.web.bind.annotation.*;
import senior.copycoders.project.api.dto.*;
import senior.copycoders.project.api.services.CreditService;



import java.math.BigDecimal;
import java.util.Arrays;
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
    public PaymentWithCreditDto createCredit(@RequestBody CreditRequest creditRequest) {

        // Получаем из creditService объект нужного класса
        return creditService.calculateSchedule(creditRequest.getDateOfFirstPayment(), BigDecimal.valueOf(creditRequest.getInitialPayment()), BigDecimal.valueOf(creditRequest.getCreditAmount()), BigDecimal.valueOf(creditRequest.getPercentRate()), creditRequest.getCreditPeriod(), creditRequest.getTypeOfCredit());
    }

    @SecurityRequirement(name = "Bearer Authentication")
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


    @GetMapping("/api/credit/constants")
    @Operation(
            summary = "Получение параметров для кредита (макс/мин процентная ставка и т.д.)"
    )
    public CreditConstantsDto getCreditConstants() {
        return new CreditConstantsDto();
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
