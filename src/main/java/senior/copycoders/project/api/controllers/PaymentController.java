package senior.copycoders.project.api.controllers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import senior.copycoders.project.api.dto.AckDto;
import senior.copycoders.project.api.dto.PaymentWithCreditDto;
import senior.copycoders.project.api.services.PaymentService;

import java.math.BigDecimal;
import java.time.LocalDate;

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

        // Получаем из creditService список всех платежей
        return paymentService.getAllPaymentsByCreditId(creditId);

    }


    @GetMapping("/api/credit/{credit_id}/makePayment")
    @Operation(
            summary = "Начисления платежа по кредиту"
    )
    public AckDto makePayment(@PathVariable(name = "credit_id") @Parameter(description = "id кредита") Long creditId, @RequestParam(name = "date") @Parameter(description = "дата платежа (формат yyyy-MM-dd)") String date, @RequestParam(name = "payment") @Parameter(description = "сумма платежа") Double currentPayment) {

        return paymentService.makePayment(creditId, date, currentPayment);
    }


}
