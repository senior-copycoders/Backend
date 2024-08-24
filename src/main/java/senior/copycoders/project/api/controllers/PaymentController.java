package senior.copycoders.project.api.controllers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import senior.copycoders.project.api.dto.AckDto;
import senior.copycoders.project.api.dto.PaymentWithCreditDto;
import senior.copycoders.project.api.services.PaymentService;
import senior.copycoders.project.store.enums.TypeOfCredit;

import java.math.BigDecimal;


@RestController
@Tag(name = "Платежи", description = "Взаимодействие с платежами")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class PaymentController {
    PaymentService paymentService;

    @GetMapping("/api/credit/{credit_id}/schedule")
    @Operation(
            summary = "Получение графика платежей (а также информация про кредит) по id кредита, которого передали"
    )
    public PaymentWithCreditDto getCreditById(@PathVariable(name = "credit_id") @Parameter(description = "id кредита") Long creditId) {

        // Получаем все платежи из paymentService
        return paymentService.getAllPaymentsByCreditId(creditId);

    }


    @PatchMapping("/api/credit/{credit_id}/make-payment")
    @Operation(
            summary = "Начисления платежа по кредиту"
    )
    public AckDto makePayment(@PathVariable(name = "credit_id") @Parameter(description = "id кредита") Long creditId, @RequestParam(name = "date") @Parameter(description = "дата платежа (формат yyyy-MM-dd)") String date, @RequestParam(name = "payment") @Parameter(description = "сумма платежа (до двух знаков после запятой)") Double currentPayment) {
        return paymentService.makePayment(creditId, date, currentPayment);
    }


    @GetMapping("/api/calculate-payment")
    @Operation(
            summary = "Узнать платёж по кредиту (для дифференцированного - первый платёж)"
    )
    public BigDecimal getPayment(@RequestParam(name = "initial_payment") @Parameter(description = "начальный платёж (неотрицательное вещественное число, до двух знаков после запятой)") Double initialPayment, @RequestParam(name = "credit_amount") @Parameter(description = "сумма кредита (положительное вещественное число, до двух знаков после запятой)") Double creditAmount, @RequestParam(name = "percent_rate") @Parameter(description = "годовая процентная ставка (положительное вещественное число, до двух знаков после запятой)") Double percentRate, @RequestParam(name = "credit_period") @Parameter(description = "срок кредитования в месяцах (положительное целое число)") Integer creditPeriod, @RequestParam(name = "typeOfCredit") @Parameter(description = "тип кредита, false - аннуитет, true - дифференцированный") Boolean type) {

        TypeOfCredit typeOfCredit = type ? TypeOfCredit.DIFFERENTIATED : TypeOfCredit.ANNUITY;

        return paymentService.findOutThePayment(BigDecimal.valueOf(initialPayment), BigDecimal.valueOf(creditAmount), BigDecimal.valueOf(percentRate), creditPeriod, typeOfCredit);
    }


}
