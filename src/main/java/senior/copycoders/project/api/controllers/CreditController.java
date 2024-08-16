package senior.copycoders.project.api.controllers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import senior.copycoders.project.api.dto.PaymentDto;
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
    public List<PaymentDto> createCredit(@RequestParam(name = "initial_payment") Double initialPayment, @RequestParam(name = "credit_amount") Double creditAmount, @RequestParam(name = "percent_rate") Double percentRate, @RequestParam(name = "credit_period") Integer creditPeriod) {


        // сохраняем данные для кредита в БД, получаем id кредита, для того, чтобы далее вернуть платежи
        // data [0] - id кредита, который сохранился в БД, data[1] - список всех платежей по нему
        Object[] data = creditService.calculateAndSave(BigDecimal.valueOf(initialPayment), BigDecimal.valueOf(creditAmount), BigDecimal.valueOf(percentRate), creditPeriod);

        List<PaymentDto> paymentDtos = (List<PaymentDto>) data[1];

        return paymentDtos;
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
