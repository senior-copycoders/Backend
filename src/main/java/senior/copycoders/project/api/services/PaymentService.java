package senior.copycoders.project.api.services;


import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import senior.copycoders.project.api.controllers.helpers.ControllerHelper;
import senior.copycoders.project.api.dto.PaymentDto;
import senior.copycoders.project.api.dto.PaymentWithCreditDto;
import senior.copycoders.project.api.factories.CreditDtoFactory;
import senior.copycoders.project.api.factories.PaymentDtoFactory;
import senior.copycoders.project.api.factories.PaymentWithCreditDtoFactory;
import senior.copycoders.project.store.entities.CreditEntity;
import senior.copycoders.project.store.entities.PaymentEntity;
import senior.copycoders.project.store.repositories.PaymentRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Transactional
public class PaymentService {
    ControllerHelper controllerHelper;
    PaymentRepository paymentRepository;
    PaymentDtoFactory paymentDtoFactory;
    CreditDtoFactory creditDtoFactory;
    PaymentWithCreditDtoFactory paymentWithCreditDtoFactory;


    /**
     * Метод, который рассчитывает и сохраняет все платежи по данным кредита
     *
     * @param initialPayment первоначальный взнос
     * @param creditAmount   сумма кредита
     * @param percentRate    процентная ставка (именно в процентах, а не в долях, то есть 10, а не 0.1)
     * @param creditPeriod   срок кредитования в месяцах
     * @param saveCredit     сущность кредит, к которому привязаны платежи
     */
    public List<PaymentDto> calculateAndSavePayments(BigDecimal initialPayment, BigDecimal creditAmount, BigDecimal percentRate, Integer creditPeriod, CreditEntity saveCredit) {
        // валидация данных для платежа
        controllerHelper.validateDataOfCredit(initialPayment, creditAmount, percentRate, creditPeriod);

        // Нам важна точность вычислений, поэтому для вычислений будем использовать BigDecimal

        // для начала нужно подсчитать остаток кредита после начального взноса
        // остаток кредита = creditAmount - initialPayment
        BigDecimal ostatokOfCredit = creditAmount.subtract(initialPayment);

        // вычисляем платёж
        BigDecimal payment = calculatePayment(ostatokOfCredit, percentRate, creditPeriod);


        // Теперь нужно сформировать список всех платежей
        List<PaymentEntity> payments = createListOfPayments(ostatokOfCredit, payment, percentRate, creditPeriod, saveCredit);

        // Сохраняем список платежей в БД
        paymentRepository.saveAll(payments);

        // Теперь нужно сформировать список paymentDto
        return createListOfPaymentDto(payments);


    }


    /**
     * Получение списка всех платежей по id кредита
     *
     * @param creditId id кредита
     * @return список платежей
     */
    public PaymentWithCreditDto getAllPaymentsByCreditId(Long creditId) {

        // получаем кредит по creditId
        CreditEntity credit = controllerHelper.getCreditOrThrowException(creditId);

        // формируем список платежей
        List<PaymentDto> payments = createListOfPaymentDto(credit);

        // сортируем список платежей, чтобы они шли по порядку
        Collections.sort(payments);

        return paymentWithCreditDtoFactory.makePaymentWithIdCreditDto(creditDtoFactory.makeCreditDto(credit), payments);

    }

    public List<PaymentDto> createListOfPaymentDto(CreditEntity credit) {
        return new ArrayList<>(credit.getPaymentList().stream()
                .map(paymentDtoFactory::makePaymentDto)
                .toList());
    }

    public List<PaymentDto> createListOfPaymentDto(List<PaymentEntity> payments) {
        return payments.stream()
                .map(paymentDtoFactory::makePaymentDto)
                .toList();
    }

    public void deletePayments(List<PaymentEntity> payments) {
        paymentRepository.deleteAll(payments);
    }


    /**
     * Метод для вычисления платежа
     *
     * @param creditAmount сумма кредита
     * @param percentRate  годовая процентная ставка (именно в процентах, а не в долях, то есть 10, а не 0.1)
     * @param creditPeriod срок кредитования в месяцах
     * @return платёж
     */
    private BigDecimal calculatePayment(BigDecimal creditAmount, BigDecimal percentRate, Integer creditPeriod) {
        // Ежемесячный платеж = (Сумма кредита * Процентная ставка / 12) / (1 - (1 + Процентная ставка / 12)^(-Срок кредита в месяцах))

        // Преобразуем percentRate, разделив его на 100
        percentRate = percentRate.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_EVEN);

        // (ostatokOfCredit * percentRate) / 12
        BigDecimal firstPart = (creditAmount.multiply(percentRate)).divide(BigDecimal.valueOf(12), RoundingMode.HALF_EVEN);

        // percentRate/12
        BigDecimal secondPart = percentRate.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_EVEN);

        // 1 + percentRate/12
        BigDecimal thirdPart = BigDecimal.ONE.add(secondPart);

        // (1 + percentRate/12) ^ creditPeriod
        BigDecimal fourthPart = thirdPart.pow(creditPeriod);

        // 1/ ((1 + percentRate/12) ^ creditPeriod)
        BigDecimal fivePart = BigDecimal.ONE.divide(fourthPart, 10, RoundingMode.HALF_EVEN);

        // 1 - (1 + percentRate/12) ^ (-creditPeriod)
        BigDecimal sixPart = BigDecimal.ONE.subtract(fivePart);

        return firstPart.divide(sixPart, 2, RoundingMode.HALF_EVEN);
    }


    /**
     * Метод для вычисления списка платежей по кредиту по месяцам
     *
     * @param creditAmount сумма кредита
     * @param payment      платёж
     * @param percentRate  годовая процентная ставка (именно в процентах, а не в долях, то есть 10, а не 0.1)
     * @param creditPeriod срок кредитования в месяцах
     * @param saveCredit   кредит, к которому привязаны платежи
     * @return список платежей
     */
    private List<PaymentEntity> createListOfPayments(BigDecimal creditAmount, BigDecimal payment, BigDecimal percentRate, Integer creditPeriod, CreditEntity saveCredit) {
        // Общая сумма денег, которую мы заплатим по итогу, равняется payment * creditPeriod

        // Преобразуем percentRate, разделив его на 100
        percentRate = percentRate.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_EVEN);

        BigDecimal totalSum = payment.multiply(BigDecimal.valueOf(creditPeriod));
        List<PaymentEntity> payments = new ArrayList<>();

        // Первая дата платёжа, это текущая дата + 1 месяц
        LocalDate date = LocalDate.now().plusMonths(1);

        for (int i = 1; i <= creditPeriod; i++) {

            // Посчитаем какая часть платежа уйдёт на оплату процентов
            BigDecimal currentPercent = (creditAmount.multiply(percentRate)).divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_EVEN);

            // Посчитаем какая часть платежа уйдёт на погашение основного долга
            BigDecimal repaymentCredit = payment.subtract(currentPercent);

            totalSum = totalSum.subtract(payment);

            creditAmount = (creditAmount.add(currentPercent)).subtract(payment);

            payments.add(PaymentEntity.builder()
                    .paymentNumber(i)
                    .paymentDate(date)
                    .paymentAmount(payment)
                    .percent(currentPercent)
                    .repaymentCredit(repaymentCredit)
                    .remainingCredit(totalSum)
                    .credit(saveCredit)
                    .build());

            date = date.plusMonths(1);

        }

        return payments;
    }

}
