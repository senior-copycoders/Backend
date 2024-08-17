package senior.copycoders.project.api.controllers.helpers;


import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import senior.copycoders.project.api.exceptions.BadRequestException;
import senior.copycoders.project.api.exceptions.NotFoundException;
import senior.copycoders.project.store.entities.CreditEntity;
import senior.copycoders.project.store.repositories.CreditRepository;

import java.math.BigDecimal;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Component
@Transactional
public class ControllerHelper {
    CreditRepository creditRepository;


    /**
     * Валидация данных для создания платежей по кредиту
     * @param initialPayment начальный платёж
     * @param creditAmount сумма кредита
     * @param percentRate годовая процентная ставка
     * @param creditPeriod срок кредитования в месяцах
     */
    public void validateDataOfCredit(BigDecimal initialPayment, BigDecimal creditAmount, BigDecimal percentRate, Integer creditPeriod) {

        if (initialPayment.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Invalid value of initial_payment");
        }

        if (creditAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Invalid value of credit_amount");
        }

        if (percentRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Invalid value of percent_rate");
        }

        if (creditPeriod <= 0) {
            throw new BadRequestException("Invalid value of credit_period");
        }

        if (initialPayment.compareTo(creditAmount) >= 0) {
            throw new BadRequestException("Credit_amount must be more than the initial_payment");
        }
    }


    /**
     * Проверка нахождения кредита по id в БД
     * @param creditId id кредита
     */
    public CreditEntity getCreditOrThrowException(Long creditId) {
        return creditRepository.findById(creditId)
                .orElseThrow(() -> new NotFoundException(String.format("Credit with id=%d doesn't exists", creditId)));
    }
}
