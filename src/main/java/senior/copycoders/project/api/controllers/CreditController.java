package senior.copycoders.project.api.controllers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import senior.copycoders.project.api.dto.PaymentDto;
import senior.copycoders.project.api.dto.PaymentWithIdCreditDto;
import senior.copycoders.project.api.services.CreditService;


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
    public PaymentWithIdCreditDto createCredit(@RequestParam(name = "initial_payment") @Parameter(description = "начальный платёж (положительное вещественное число, до двух знаков после запятой)") Double initialPayment, @RequestParam(name = "credit_amount") @Parameter(description = "сумма кредита (положительное вещественное число, до двух знаков после запятой)") Double creditAmount, @RequestParam(name = "percent_rate") @Parameter(description = "годовая процентная ставка (положительное вещественное число, до двух знаков после запятой)") Double percentRate, @RequestParam(name = "credit_period") @Parameter(description = "срок кредитования в месяцах (положительное целое число)") Integer creditPeriod) {


        // Получаем из creditService объекта нужно класса
        return creditService.calculateAndSave(BigDecimal.valueOf(initialPayment), BigDecimal.valueOf(creditAmount), BigDecimal.valueOf(percentRate), creditPeriod);
    }

    @GetMapping("/api/credit/{credit_id}")
    @Operation(
            summary = "Получение списка платежей по кредиту id, которого передали"
    )
    public Object getCreditById() {
        return null;
    }

    @GetMapping("/api/credit")
    @Operation(
            summary = "Получение списка всех кредитов"
    )
    public Object getAllCredit() {
        return null;
    }


    @DeleteMapping("/api/credit/{credit_id}")
    @Operation(
            summary = "Удаление кредита по id (вместе с его платежами)"
    )
    public Object deleteCreditById() {
        return null;
    }


    @PatchMapping("/api/credit/{credit_id}")
    @Operation(
            summary = "Изменение каких-то параметров кредита по id и перерасчёт всех платежей"
    )
    public Object changeCredit() {

        return null;
    }

}
