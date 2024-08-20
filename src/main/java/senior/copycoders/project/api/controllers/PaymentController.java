package senior.copycoders.project.api.controllers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import senior.copycoders.project.api.dto.PaymentWithCreditDto;
import senior.copycoders.project.api.services.PaymentService;

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
    

}
