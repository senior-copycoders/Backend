package senior.copycoders.project.api.controllers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import senior.copycoders.project.api.dto.*;
import senior.copycoders.project.api.exceptions.ErrorDto;
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
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful response.",
                    content = @Content(schema = @Schema(implementation = PaymentWithCreditDto.class))),

            @ApiResponse(responseCode = "400", description = "Invalid information about credit.",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "403", description = "Missing or invalid token.")

    })
    public PaymentWithCreditDto createCredit(@RequestBody CreditRequest creditRequest) {

        // Получаем из creditService объект нужного класса
        return creditService.calculateSchedule(creditRequest.getDateOfFirstPayment(), BigDecimal.valueOf(creditRequest.getInitialPayment()), BigDecimal.valueOf(creditRequest.getCreditAmount()), BigDecimal.valueOf(creditRequest.getPercentRate()), creditRequest.getCreditPeriod(), creditRequest.getTypeOfCredit());
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/api/credit")
    @Operation(
            summary = "Получение списка всех кредитов"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful response.",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CreditDto.class)))),
            @ApiResponse(responseCode = "403", description = "Missing or invalid token.")
    })
    public List<CreditDto> getAllCredit() {

        // получаем список всех кредитов в БД
        return creditService.getAllCredit();
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/api/credit/{credit_id}")
    @Operation(
            summary = "Удаление кредита по id (вместе с его платежами)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful response.",
                    content = @Content(schema = @Schema(implementation = AckDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid information about credit.",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "403", description = "Missing or invalid token."),
            @ApiResponse(responseCode = "404", description = "Credit with {credit_id} not found.",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class)))
    })
    public AckDto deleteCreditById(@PathVariable(name = "credit_id") @Parameter() Long creditId) {
        creditService.deleteCreditAndPayments(creditId);

        return AckDto.makeDefault(true);
    }


    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/api/credit/constants")
    @Operation(
            summary = "Получение параметров для кредита (макс/мин процентная ставка и т.д.)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful response.",
                    content = @Content(schema = @Schema(implementation = CreditConstantsDto.class))),
            @ApiResponse(responseCode = "403", description = "Missing or invalid token.")
    })
    public CreditConstantsDto getCreditConstants() {
        return new CreditConstantsDto();
    }

}
