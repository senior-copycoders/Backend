package senior.copycoders.project.api.controllers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import senior.copycoders.project.api.dto.AckDto;
import senior.copycoders.project.api.dto.InitialDataOfCreditDto;
import senior.copycoders.project.api.dto.PaymentWithCreditDto;
import senior.copycoders.project.api.exceptions.ErrorDto;
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
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Получение графика платежей (а также информация про кредит) по id кредита, которого передали"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful response.",
                    content = @Content(schema = @Schema(implementation = PaymentWithCreditDto.class))),
            @ApiResponse(responseCode = "404", description = "Credit with {credit_id} not found.",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "403", description = "Missing or invalid token.")
    })
    public PaymentWithCreditDto getCreditById(@PathVariable(name = "credit_id") @Parameter(description = "id кредита") Long creditId) {

        // Получаем все платежи из paymentService
        return paymentService.getAllPaymentsByCreditId(creditId);

    }


    @SecurityRequirement(name = "Bearer Authentication")
    @PatchMapping("/api/credit/{credit_id}/make-payment")
    @Operation(
            summary = "Начисления платежа по кредиту"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful response.",
                    content = @Content(schema = @Schema(implementation = PaymentWithCreditDto.class))),
            @ApiResponse(responseCode = "404", description = "Credit with {credit_id} not found.",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "403", description = "Missing or invalid token."),
            @ApiResponse(responseCode = "400", description = "Invalid data of payment or date.",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class)))
    })
    public AckDto makePayment(@PathVariable(name = "credit_id") @Parameter(description = "id кредита") Long creditId, @RequestParam(name = "date") @Parameter(description = "дата платежа (формат yyyy-MM-dd)") String date, @RequestParam(name = "payment") @Parameter(description = "сумма платежа (до двух знаков после запятой)") Double currentPayment) {
        return paymentService.makePayment(creditId, date, currentPayment);
    }


    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/api/calculate-payment")
    @Operation(
            summary = "Узнать платёж по кредиту (для дифференцированного - первый платёж)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful response.",
                    content = @Content(schema = @Schema(implementation = InitialDataOfCreditDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid information about credit.",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "403", description = "Missing or invalid token."),
            @ApiResponse(responseCode = "400", description = "Invalid data of payment or date.",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class)))
    })
    public InitialDataOfCreditDto getPayment(@RequestParam(name = "initial_payment") @Parameter(description = "начальный платёж (неотрицательное вещественное число, до двух знаков после запятой)") Double initialPayment, @RequestParam(name = "credit_amount") @Parameter(description = "сумма кредита (положительное вещественное число, до двух знаков после запятой, min = 200_000, max = 30_000_000)") Double creditAmount, @RequestParam(name = "percent_rate") @Parameter(description = "годовая процентная ставка (положительное вещественное число, до двух знаков после запятой, min = 0% (не включительно), max = 18%)") Double percentRate, @RequestParam(name = "credit_period") @Parameter(description = "срок кредитования в месяцах (положительное целое число, min = 12 месяцев(1 год), max = 360(30 лет))") Integer creditPeriod, @RequestParam(name = "typeOfCredit") @Parameter(description = "тип кредита, false - аннуитет, true - дифференцированный") Boolean type) {

        TypeOfCredit typeOfCredit = type ? TypeOfCredit.DIFFERENTIATED : TypeOfCredit.ANNUITY;

        BigDecimal payment = paymentService.findOutThePayment(BigDecimal.valueOf(initialPayment), BigDecimal.valueOf(creditAmount), BigDecimal.valueOf(percentRate), creditPeriod, typeOfCredit);

        // нужно рассчитать налоговый вычет

        BigDecimal taxDeduction = paymentService.calculateTaxDeduction(BigDecimal.valueOf(initialPayment), BigDecimal.valueOf(creditAmount), BigDecimal.valueOf(percentRate), creditPeriod, typeOfCredit, payment);

        return InitialDataOfCreditDto.builder()
                .payment(payment)
                .taxDeduction(taxDeduction)
                .range("22.12-24.45%")
                .build();


    }


}
