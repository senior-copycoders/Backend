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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Component
@Transactional
public class ControllerHelper {
    CreditRepository creditRepository;


    /**
     * Валидация данных для создания платежей по кредиту
     *
     * @param initialPayment начальный платёж
     * @param creditAmount   сумма кредита
     * @param percentRate    годовая процентная ставка
     * @param creditPeriod   срок кредитования в месяцах
     */
    public void validateDataOfCredit(String dateOfFirstPayment, BigDecimal initialPayment, BigDecimal creditAmount, BigDecimal percentRate, Integer creditPeriod) {

        // начальный платёж не может быть меньше нуля
        if (initialPayment.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Invalid value of initial_payment");
        }

        // сумма кредита не может быть меньше или равна нуля
        if (creditAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Invalid value of credit_amount");
        }

        // процентная годовая ставка не может быть меньше или равна нуля
        if (percentRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Invalid value of percent_rate");
        }

        // срок кредитования не может быть меньше или равен нуля
        if (creditPeriod <= 0) {
            throw new BadRequestException("Invalid value of credit_period");
        }

        // начальный платёж должен быть меньше, чем сумма кредита
        if (initialPayment.compareTo(creditAmount) >= 0) {
            throw new BadRequestException("Credit_amount must be more than the initial_payment");
        }

        // валидация даты
        try {
            // Определяем формат даты
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            // Пытаемся преобразовать строку в LocalDate
            LocalDate dateOfPayment = LocalDate.parse(dateOfFirstPayment, formatter);
        } catch (DateTimeParseException e) {
            throw new BadRequestException("Incorrect date format");
        }
    }


    /**
     * Проверка нахождения кредита по id в БД
     *
     * @param creditId id кредита
     */
    public CreditEntity getCreditOrThrowException(Long creditId) {
        return creditRepository.findById(creditId)
                .orElseThrow(() -> new NotFoundException(String.format("Credit with id=%d doesn't exists", creditId)));
    }

    /**
     * Проверка строки на правильный формат даты
     *
     * @param date строка, которая должна из себя представлять дату в формате yyyy-MM-dd
     */
    public LocalDate getDateOrThrowException(String date) {
        try {
            // Определяем формат даты
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            // Пытаемся преобразовать строку в LocalDate
            return LocalDate.parse(date, formatter);
        } catch (DateTimeParseException e) {
            throw new BadRequestException("Incorrect date format");
        }
    }
}
