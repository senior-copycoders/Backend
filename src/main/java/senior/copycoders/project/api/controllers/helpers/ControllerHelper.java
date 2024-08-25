package senior.copycoders.project.api.controllers.helpers;


import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import senior.copycoders.project.api.exceptions.BadRequestException;
import senior.copycoders.project.api.exceptions.NotFoundException;
import senior.copycoders.project.store.entities.CreditEntity;
import senior.copycoders.project.store.enums.CreditConstants;
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
    static BigDecimal MAX_PERCENT_RATE = BigDecimal.valueOf(18);


    /**
     * Валидация данных для создания платежей по кредиту
     *
     * @param initialPayment начальный платёж
     * @param creditAmount   сумма кредита
     * @param percentRate    годовая процентная ставка (именно 10, а не 0.1)
     * @param creditPeriod   срок кредитования в месяцах
     */
    public void validateDataOfCredit(String dateOfFirstPayment, BigDecimal initialPayment, BigDecimal creditAmount, BigDecimal percentRate, Integer creditPeriod) {

        // сначала проверим, что все числа имеют не более двух знаков после запятой
        if (initialPayment.scale() > 2) {
            throw new BadRequestException("Initial_payment contains more than two decimal places");
        }


        if (creditAmount.scale() > 2) {
            throw new BadRequestException("Credit_amount contains more than two decimal places");
        }


        if (percentRate.scale() > 2) {
            throw new BadRequestException("Percent_rate contains more than two decimal places");
        }

        // теперь проверим на максимальную годовую процентную ставку
        if (percentRate.compareTo(MAX_PERCENT_RATE) > 0) {
            throw new BadRequestException("Interest rate cannot be more than 18 percent");
        }
        // процентная годовая ставка не может быть меньше или равна нуля
        if (percentRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Invalid value of percent_rate");
        }


        // начальный платёж не может быть меньше нуля
        if (initialPayment.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Invalid value of initial_payment");
        }


        // сумма кредита не может быть меньше 200_000
        if (creditAmount.compareTo(BigDecimal.valueOf(CreditConstants.MIN_CREDIT_AMOUNT.getValue())) < 0) {
            throw new BadRequestException("Credit amount cannot be less than 200_000.");
        }

        // сумма кредита не может быть больше 30_000_000
        if (creditAmount.compareTo(BigDecimal.valueOf(CreditConstants.MAX_CREDIT_AMOUNT.getValue())) > 0) {
            throw new BadRequestException("Credit amount cannot be more than 30_000_000.");
        }


        // срок кредитования не может быть меньше 12 месяцев (1 год)
        if (creditPeriod < CreditConstants.MIN_CREDIT_PERIOD.getValue()) {
            throw new BadRequestException("Credit term must be at least 12 months");
        }
        // срок кредитования не может быть больше 30*12 месяцев (30 лет)
        if (creditPeriod > CreditConstants.MAX_CREDIT_PERIOD.getValue()) {
            throw new BadRequestException("Credit term must be at least 12 months");
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


        // теперь нужно сделать валидацию, если дата была 30 или 31 февраля (или же 29)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        // Пытаемся преобразовать строку в LocalDate
        LocalDate dateOfPayment = LocalDate.parse(dateOfFirstPayment, formatter);

        if (!dateOfPayment.toString().equals(dateOfFirstPayment)) {
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
            LocalDate.parse(date, formatter);
        } catch (DateTimeParseException e) {
            throw new BadRequestException("Incorrect date format");
        }

        // теперь нужно сделать валидацию, если дата была 30 или 31 февраля (или же 29)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        // Пытаемся преобразовать строку в LocalDate
        LocalDate dateOfPayment = LocalDate.parse(date, formatter);

        if (!dateOfPayment.toString().equals(date)) {
            throw new BadRequestException("Incorrect date format");
        }
        return dateOfPayment;

    }
}
