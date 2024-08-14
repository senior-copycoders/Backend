package senior.copycoders.project.api.controllers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Кредиты", description = "Взаимодействие с кредитами")
public class CreditController {

    @PostMapping("/api/credit")
    @Operation(
            summary = "Создания кредита и графика всех платежей по нему"
    )
    public Object createCredit() {
        return null;
    }

    @GetMapping("/api/credit/{credit_id}")
    @Operation(
            summary = "Получение списка платежей по кредиту id, которого передали"
    )
    public Object getCreditById(){
        return null;
    }

    @GetMapping("/api/credit")
    @Operation(
            summary = "Получение списка всех кредитов"
    )
    public Object getAllCredit(){
        return null;
    }


    @DeleteMapping("/api/credit/{credit_id}")
    @Operation(
            summary = "Удаление кредита по id (вместе с его платежами)"
    )
    public Object deleteCreditById(){
        return null;
    }


    @PatchMapping("/api/credit/{credit_id}")
    @Operation(
            summary = "Изменение каких-то параметров кредита по id и перерасчёт всех платежей"
    )
    public Object changeCredit(){
        return null;
    }

}
